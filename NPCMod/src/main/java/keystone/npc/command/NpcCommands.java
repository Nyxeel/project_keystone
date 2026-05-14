package keystone.npc.command;

import keystone.npc.lifecycle.NpcRemoval;
import keystone.npc.lifecycle.NpcRespawn;
import keystone.npc.lifecycle.NpcSpawn;
import keystone.npc.marker.MarkerAssignment;
import keystone.npc.state.NpcStateStore;

public final class NpcCommands {

    private final NpcSpawn spawn;
    private final NpcRemoval removal;
    private final MarkerAssignment markerAssignment;
    private final NpcRespawn respawn;
    private final NpcStateStore stateStore;

    public NpcCommands(
            NpcSpawn spawn,
            NpcRemoval removal,
            MarkerAssignment markerAssignment,
            NpcRespawn respawn,
            NpcStateStore stateStore
    ) {
        this.spawn = spawn;
        this.removal = removal;
        this.markerAssignment = markerAssignment;
        this.respawn = respawn;
        this.stateStore = stateStore;
    }

    public void registerCommands() {
        // TODO: Commands registrieren.
    }

    public void handleSpawnCommand(String roleId, String npcName) {
        spawn.spawnNpc(roleId, npcName);
    }

    public void handleRemoveCommand(String npcId) {
        removal.removeNpcDetailed(npcId);
    }

    public void handleMarkerSetCommand(String npcId, String markerName, String markerId) {
        markerAssignment.assignMarkerToNpc(npcId, markerName, markerId);
    }

    public void handleRespawnMissingCommand(boolean dryRun, boolean force) {
        respawn.respawnMissingNpcs(dryRun, force);
    }
}