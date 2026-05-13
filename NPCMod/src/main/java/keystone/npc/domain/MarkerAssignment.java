package keystone.npc.domain;

import java.util.Objects;

import keystone.npc.markers.MarkerType;

public record MarkerAssignment(String markerId, MarkerType markerType) {

    public MarkerAssignment {
        if (markerId == null || markerId.isBlank()) {
            throw new IllegalArgumentException("marker-assignment-marker-id-missing");
        }
        markerId = markerId.trim();
        markerType = Objects.requireNonNull(markerType, "marker-assignment-marker-type-missing");
    }
}