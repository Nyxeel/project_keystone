package keystone.npc.commands;

import keystone.npc.schedule.NpcScheduler;
import keystone.npc.world.*;

import java.util.Objects;
import java.util.UUID;

/**
 * MVP A Commands (Platzhalter).
 *
 * Intended commands:
 * - /npcmarker set bed|door|work
 * - /npc spawn lumberjack <name?>
 * - /npc debug
 */
public final class NpcCommands {

    private final MarkerRegistry markerRegistry;
    private final NpcScheduler scheduler;

    public NpcCommands(MarkerRegistry markerRegistry, NpcScheduler scheduler) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    public void registerAll() {
        // TODO: hook into Hytale command system.
        // For now: no-op.
    }

    // --- Marker ---

    public void npcmarkerSet(MarkerType type, WorldId worldId, Vec3 position) {
        String markerId = UUID.randomUUID().toString();
        markerRegistry.upsert(new MarkerRecord(markerId, worldId, position, type));
        System.out.println("[KeystoneNPC] marker set " + type + " id=" + markerId + " pos=" + position);
    }

    // --- NPC ---

    public void npcSpawnLumberjack(String name, WorldId worldId) {
        var id = UUID.randomUUID().toString();
        var npc = scheduler.spawnLumberjack(id, name == null ? "Lumberjack" : name, worldId);
        System.out.println("[KeystoneNPC] spawned lumberjack id=" + npc.npcId() + " name=" + npc.npcName());
    }

    public void npcDebug() {
        // TODO: print scheduler snapshot + active markers
    }
}
