package keystone.npc.lifecycle;

import keystone.npc.marker.MarkerAssignment;
import keystone.npc.state.NpcStateStore;

public final class NpcRemoval {

    private final NpcStateStore stateStore;
    private final MarkerAssignment markerAssignment;

    public NpcRemoval(
            NpcStateStore stateStore,
            MarkerAssignment markerAssignment
    ) {
        this.stateStore = stateStore;
        this.markerAssignment = markerAssignment;
    }

    public boolean removeNpc(String npcId) {
        // TODO: Einfacher Remove-Pfad.
        return false;
    }

    public Object removeNpcDetailed(String npcId) {
        // TODO: Sicherer detaillierter Remove-Pfad.
        return null;
    }

    public boolean validateRemoval(String npcId) {
        // TODO: Prüfen, ob Löschen sicher ist.
        return false;
    }
}