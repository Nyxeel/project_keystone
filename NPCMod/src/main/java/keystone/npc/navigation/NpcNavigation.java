package keystone.npc.navigation;

import keystone.npc.marker.MarkerAssignment;

public final class NpcNavigation {

    private final MarkerAssignment markerAssignment;

    public NpcNavigation(MarkerAssignment markerAssignment) {
        this.markerAssignment = markerAssignment;
    }

    public void startNavigation(String npcId, String markerName) {
        // TODO: Navigation starten.
    }

    public void stopNavigation(String npcId) {
        // TODO: Navigation stoppen.
    }

    public void updateNavigation(String npcId) {
        // TODO: Navigation aktualisieren.
    }

    public void clearRuntimeNavigation() {
        // TODO: Runtime-Navigation leeren.
    }
}