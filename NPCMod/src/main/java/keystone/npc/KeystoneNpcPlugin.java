package keystone.npc;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.npc.AllNPCsLoadedEvent;

import keystone.npc.commands.NpcCommandRegistrar;
import keystone.npc.persistence.JsonFileStateStore;
import keystone.npc.persistence.StateStore;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;
import keystone.npc.routine.NpcTickSystem;
import keystone.npc.markers.MarkerRegistry;

/**
 * MVP A: server-first, 1 NPC (Lumberjack), 3 Marker (bed/door/work), Save/Load, einfacher Tagesablauf.
 */
public class KeystoneNpcPlugin extends JavaPlugin {

    private final MarkerRegistry markerRegistry = new MarkerRegistry();
    private final RoleDefinitionRegistry roleDefinitions = new RoleDefinitionRegistry("keystone-npc/roles.json");
    private final StateStore stateStore = new JsonFileStateStore("keystone-npc/state.json");
    private final NpcRoutineRunner scheduler = new NpcRoutineRunner(markerRegistry, roleDefinitions);
    private boolean initialRespawnQueued;

    private NpcCommandRegistrar commands;

    public KeystoneNpcPlugin(JavaPluginInit init) {
        super(init);
    }

    /**
     * Called once while the plugin is being constructed/registered.
     * Use this for wiring, loading state, and registering commands.
     */
    @Override
    protected void setup() {
        // TODO: Logger aus Hytale-API verwenden
        System.out.println("[KeystoneNPC] setup...");

        // 1) Load persisted state
        // TODO: Pfad in server/mod data dir auflösen (z.B. über getFile()/server data dir)
        roleDefinitions.ensureExampleFileExists();
        roleDefinitions.load();
        System.out.println("[KeystoneNPC] Loaded role definitions: " + String.join(", ", roleDefinitions.roleIds()));

        var loaded = stateStore.load();
        markerRegistry.restore(loaded.markers(), loaded.activeMarkerIds());
        scheduler.restore(loaded.npcs());

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
        commands = new NpcCommandRegistrar(this, markerRegistry, roleDefinitions, scheduler);
        commands.registerAll();
    }

    public void saveState() {
        try {
            stateStore.save(markerRegistry.snapshot(), scheduler.snapshot(), markerRegistry.snapshotActiveMarkerIds());
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][PLUGIN_SAVE_RUNTIME_ERROR] Failed to persist plugin state: "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][PLUGIN_SAVE_LINKAGE_ERROR] Persist skipped due to classloader/linkage issue: "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
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
}
