package keystone.npc.lifecycle;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.marker.MarkerAssignment;
import keystone.npc.state.NpcStateStore;

public final class NpcSpawn {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;
    private final MarkerAssignment markerAssignment;
    private final NpcRemoval removal;

    public NpcSpawn(
            NpcStateStore stateStore,
            NpcDefinition definition,
            MarkerAssignment markerAssignment,
            NpcRemoval removal
    ) {
        this.stateStore = stateStore;
        this.definition = definition;
        this.markerAssignment = markerAssignment;
        this.removal = removal;
    }

    public Object spawnNpc(String roleId, String npcName) {
        // TODO: NPC kontrolliert erzeugen.
        return null;
    }

    public boolean validateSpawnRequest(String roleId, String npcName) {
        // TODO: Spawn-Anfrage prüfen.
        return false;
    }
}