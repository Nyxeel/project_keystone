package keystone.npc;

import keystone.npc.commands.NpcCommands;
import keystone.npc.persist.StateStore;
import keystone.npc.persist.JsonFileStateStore;
import keystone.npc.schedule.NpcScheduler;
import keystone.npc.world.MarkerRegistry;

/**
 * MVP A: server-first, 1 NPC (Lumberjack), 3 Marker (bed/door/work), Save/Load, einfacher Tagesablauf.
 *
 * NOTE: Das ist ein Skeleton. Die echten Hytale-Plugin Hooks/APIs müssen hier integriert werden.
 */
public final class KeystoneNPCPlugin {

    private final MarkerRegistry markerRegistry = new MarkerRegistry();
    private final StateStore stateStore = new JsonFileStateStore("keystone-npc/state.json");
    private final NpcScheduler scheduler = new NpcScheduler(markerRegistry);

    private final NpcCommands commands = new NpcCommands(markerRegistry, scheduler);

    /** Plugin enable hook (Hytale-API ersetzen). */
    public void onEnable() {
        // TODO: Logger aus Hytale-API verwenden
        System.out.println("[KeystoneNPC] enabling...");

        // 1) Load persisted state
        // TODO: Pfad in server/mod data dir auflösen
        var loaded = stateStore.load();
        markerRegistry.restore(loaded.markers());
        scheduler.restore(loaded.npcs());

        // 2) Register commands
        // TODO: echte Command-Registrierung (Hytale)
        commands.registerAll();

        // 3) Start scheduler tick
        // TODO: echten Server-Tick/Timer anbinden
        scheduler.start();

        System.out.println("[KeystoneNPC] enabled.");
    }

    /** Plugin disable hook (Hytale-API ersetzen). */
    public void onDisable() {
        System.out.println("[KeystoneNPC] disabling...");

        scheduler.stop();

        // Save state
        stateStore.save(markerRegistry.snapshot(), scheduler.snapshot());

        System.out.println("[KeystoneNPC] disabled.");
    }
}
