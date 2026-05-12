package keystone.npc;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.npc.AllNPCsLoadedEvent;

import keystone.npc.commands.NpcCommandRegistrar;
import keystone.npc.definition.NpcDefinitionRegistry;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.persistence.JsonFileStateStore;
import keystone.npc.persistence.StateStore;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;
import keystone.npc.routine.NpcTickSystem;
import keystone.npc.skills.SkillChecks;
import keystone.npc.skills.SkillResolver;

/**
 * MVP A: server-first, 1 NPC (Lumberjack), 3 Marker (bed/door/work), Save/Load, einfacher Tagesablauf.
 */
public class KeystoneNpcPlugin extends JavaPlugin {

    private static final String DATA_DIRECTORY = "keystone-npc";
    private static final String STATE_FILE = "state.json";

    private final MarkerRegistry markerRegistry = new MarkerRegistry();
    private final RoleDefinitionRegistry roleDefinitions;
    private final NpcDefinitionRegistry npcDefinitions;
    private final NpcTemplateResolver templateResolver;
    private final SkillChecks skillChecks;
    private final StateStore stateStore;
    private final NpcRoutineRunner scheduler;
    private final Path pluginDataDirectory;
    private boolean initialRespawnQueued;
    private boolean stateSavePathLogged;

    private NpcCommandRegistrar commands;

    public KeystoneNpcPlugin(@Nonnull JavaPluginInit init) {
        super(init);

        this.pluginDataDirectory = resolvePluginDataDirectory();
        this.npcDefinitions = new NpcDefinitionRegistry(pluginDataDirectory, "Server/NPC");
        this.templateResolver = new NpcTemplateResolver(npcDefinitions, new SkillResolver(npcDefinitions));
        this.roleDefinitions = new RoleDefinitionRegistry(templateResolver);
        this.skillChecks = new SkillChecks(templateResolver);
        this.stateStore = new JsonFileStateStore(pluginDataDirectory.resolve(STATE_FILE).toString());
        this.scheduler = new NpcRoutineRunner(markerRegistry, roleDefinitions, skillChecks, templateResolver);
    }

    /**
     * Called once while the plugin is being constructed/registered.
     * Use this for wiring, loading state, and registering commands.
     */
    @Override
    protected void setup() {
        // TODO: Logger aus Hytale-API verwenden
        System.out.println("[KeystoneNPC] setup...");

        scheduler.configureStateSaveCallback(this::saveStateSafely);

        // 1) Load persisted state
        System.out.println("[KeystoneNPC] Data directory: " + pluginDataDirectory);
        npcDefinitions.load();
        templateResolver.reload();
        System.out.println("[KeystoneNPC] Loaded npc definitions: " + String.join(", ", templateResolver.definitionIds()));
        roleDefinitions.load();
        System.out.println("[KeystoneNPC] Loaded role definitions: " + String.join(", ", roleDefinitions.roleIds()));

        var loaded = stateStore.load();
        markerRegistry.restore(loaded.markers(), loaded.activeMarkerIds());
        scheduler.restore(loaded.npcs());
        saveState();

        // Run scheduler in the native ECS tick pipeline.
        getEntityStoreRegistry().registerSystem(new NpcTickSystem(scheduler));

        // Restore saved NPC entities once all worlds are available.
        getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, event -> {
            queueInitialRespawnIfNeeded("all-worlds-loaded-event");
        });
        getEventRegistry().registerGlobal(AllNPCsLoadedEvent.class, event -> {
            queueInitialRespawnIfNeeded("all-npcs-loaded-event");
        });

        // 2) Register commands
        commands = new NpcCommandRegistrar(this, markerRegistry, roleDefinitions, templateResolver, scheduler);
        commands.registerAll();
    }

    public void saveState() {
        saveStateSafely();
    }

    public boolean saveStateSafely() {
        try {
            logStateSavePathOnce();
            stateStore.save(markerRegistry.snapshot(), scheduler.snapshot(), markerRegistry.snapshotActiveMarkerIds());
            return true;
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][PLUGIN_SAVE_RUNTIME_ERROR] Failed to persist plugin state: "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][PLUGIN_SAVE_LINKAGE_ERROR] Persist skipped due to classloader/linkage issue: "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    /** Called after setup(), when the server is ready and the plugin should start running. */
    @Override
    protected void start() {
        System.out.println("[KeystoneNPC] start...");

        queueInitialRespawnIfNeeded("plugin-start");

        System.out.println("[KeystoneNPC] started.");
    }

    private synchronized void queueInitialRespawnIfNeeded(String trigger) {
        if (initialRespawnQueued) {
            System.out.println("[KeystoneNPC] Initial respawn already queued; skipping duplicate trigger " + trigger + ".");
            return;
        }

        initialRespawnQueued = true;
        int queued = scheduler.spawnRestoredNpcs(trigger);
        System.out.println("[KeystoneNPC] Initial respawn trigger=" + trigger + " queued " + queued + " NPC(s).");
    }

    /** Called during server shutdown / plugin unload. */
    @Override
    protected void shutdown() {
        System.out.println("[KeystoneNPC] shutdown...");

        // Save state
        try {
            saveState();
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][PLUGIN_SHUTDOWN_RUNTIME_ERROR] Unexpected runtime error during shutdown save: "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][PLUGIN_SHUTDOWN_LINKAGE_ERROR] Unexpected linkage error during shutdown save: "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println("[KeystoneNPC] stopped.");
    }

    private void logStateSavePathOnce() {
        if (stateSavePathLogged) {
            return;
        }

        Path stateFilePath = pluginDataDirectory.resolve(STATE_FILE).toAbsolutePath().normalize();
        Path parent = stateFilePath.getParent();
        boolean parentExists = parent != null && Files.exists(parent);

        System.out.println("[KeystoneNPC] State file target: " + stateFilePath
            + " (parentExists=" + parentExists + ")");
        stateSavePathLogged = true;
    }

    private Path resolvePluginDataDirectory() {
        Path baseDirectory = resolveServerPluginBaseDirectory();
        return baseDirectory.resolve(DATA_DIRECTORY).toAbsolutePath().normalize();
    }

    private Path resolveServerPluginBaseDirectory() {
        Path dataFolderPath = invokePathMethod("getDataFolder");
        if (dataFolderPath != null) {
            return normalizeExistingFolderCandidate(dataFolderPath);
        }

        Path filePath = invokePathMethod("getFile");
        if (filePath != null) {
            Path normalized = filePath.toAbsolutePath().normalize();
            if (Files.isRegularFile(normalized) && normalized.getParent() != null) {
                return normalized.getParent();
            }
            return normalized;
        }

        return Paths.get(".").toAbsolutePath().normalize();
    }

    private Path normalizeExistingFolderCandidate(Path candidate) {
        Path normalized = candidate.toAbsolutePath().normalize();
        if (Files.isRegularFile(normalized) && normalized.getParent() != null) {
            return normalized.getParent();
        }
        return normalized;
    }

    private Path invokePathMethod(String methodName) {
        try {
            Method method = getClass().getMethod(methodName);
            Object value = method.invoke(this);
            return toPath(value);
        } catch (ReflectiveOperationException | SecurityException ex) {
            return null;
        }
    }

    private Path toPath(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Path pathValue) {
            return pathValue;
        }
        if (value instanceof File fileValue) {
            return fileValue.toPath();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Paths.get(stringValue);
        }
        return null;
    }
}
