package keystone.npc;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;

import keystone.npc.commands.NpcCommands;
import keystone.npc.persist.JsonFileStateStore;
import keystone.npc.persist.StateStore;
import keystone.npc.schedule.NpcScheduler;
import keystone.npc.world.MarkerRegistry;

/**
 * MVP A: server-first, 1 NPC (Lumberjack), 3 Marker (bed/door/work), Save/Load, einfacher Tagesablauf.
 */
public class KeystoneNPCPlugin extends JavaPlugin {

    private final MarkerRegistry markerRegistry = new MarkerRegistry();
    private final StateStore stateStore = new JsonFileStateStore("keystone-npc/state.json");
    private final NpcScheduler scheduler = new NpcScheduler(markerRegistry);

    private NpcCommands commands;

    public KeystoneNPCPlugin(JavaPluginInit init) {
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
        var loaded = stateStore.load();
        markerRegistry.restore(loaded.markers(), loaded.activeMarkerIds());
        scheduler.restore(loaded.npcs());

        // Restore saved NPC entities once all worlds are available.
        getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, event -> {
            int queued = scheduler.spawnRestoredNpcs("all-worlds-loaded-event");
            System.out.println("[KeystoneNPC] World-load respawn trigger queued " + queued + " NPC(s).");
        });

        // 2) Register commands
        commands = new NpcCommands(this, markerRegistry, scheduler);
        commands.registerAll();
    }

    public void saveState() {
        stateStore.save(markerRegistry.snapshot(), scheduler.snapshot(), markerRegistry.snapshotActiveMarkerIds());
    }

    /** Called after setup(), when the server is ready and the plugin should start running. */
    @Override
    protected void start() {
        System.out.println("[KeystoneNPC] start...");

        // TODO: echten Server-Tick/Timer anbinden
        scheduler.start();

        int queued = scheduler.spawnRestoredNpcs("plugin-start");
        System.out.println("[KeystoneNPC] Startup respawn trigger queued " + queued + " NPC(s).");

        System.out.println("[KeystoneNPC] started.");
    }

    /** Called during server shutdown / plugin unload. */
    @Override
    protected void shutdown() {
        System.out.println("[KeystoneNPC] shutdown...");

        scheduler.stop();

        // Save state
        saveState();

        System.out.println("[KeystoneNPC] stopped.");
    }
}
