package keystone.npc.marker;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.state.NpcStateStore;

public final class MarkerAssignment {

    private final MarkerRegistry markerRegistry;
    private final NpcStateStore stateStore;
    private final NpcDefinition definition;

    public MarkerAssignment(
		MarkerRegistry markerRegistry, NpcStateStore stateStore, NpcDefinition definition )
	{
        this.markerRegistry = markerRegistry;
        this.stateStore = stateStore;
        this.definition = definition;
    }

    public boolean assignMarkerToNpc(String npcId, String markerName, String markerId) {
        // TODO: Marker sicher zuweisen.
        return false;
    }

    public void clearMarkerAssignment(String npcId, String markerName) {
        // TODO: Marker-Zuweisung entfernen.
    }

    public Object resolveMarkerReadOnly(String npcId, String markerName) {
        // TODO: Marker nur lesen, niemals state.json verändern.
        return null;
    }
}