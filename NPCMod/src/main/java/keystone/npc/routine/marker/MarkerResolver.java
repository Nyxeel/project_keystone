package keystone.npc.routine.marker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import keystone.npc.domain.MarkerAssignment;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.domain.TargetRole;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;

public final class MarkerResolver {
    private final MarkerRegistry markerRegistry;

    public MarkerResolver(MarkerRegistry markerRegistry) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
    }

    public String markerIdForType(NpcRecord npc, MarkerType markerType) {
        return markerIdFromAssignments(npc, markerType);
    }

    private String markerIdFromAssignments(NpcRecord npc, MarkerType markerType) {
        if (npc == null || markerType == null) {
            return null;
        }

        String logicalKey = logicalKeyForType(markerType);
        MarkerAssignment assignment = npc.markerAssignments().get(logicalKey);
        if (assignment == null) {
            return null;
        }

        if (assignment.markerType() != markerType) {
            return null;
        }

        String markerId = assignment.markerId();
        if (markerId == null || markerId.isBlank()) {
            return null;
        }

        return markerId.trim();
    }

    private String logicalKeyForType(MarkerType markerType) {
        return switch (markerType) {
            case BED -> "bed";
            case DOOR -> "door";
            case CHEST -> "chest";
            case FOOD -> "food";
            case WORK -> "work";
            case CHILL -> "chill";
        };
    }

    public void setMarkerIdForType(NpcRecord npc, MarkerType markerType, String markerId) {
        if (npc == null || markerType == null) {
            return;
        }

        Map<String, MarkerAssignment> assignments = new LinkedHashMap<>(npc.markerAssignments());
        String logicalKey = logicalKeyForType(markerType);
        if (markerId == null || markerId.isBlank()) {
            assignments.remove(logicalKey);
        } else {
            assignments.put(logicalKey, new MarkerAssignment(markerId.trim(), markerType));
        }

        npc.markerAssignments(assignments);
    }

    public Optional<MarkerRecord> resolveMarkerInNpcWorld(NpcRecord npc, MarkerType markerType, String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return Optional.empty();
        }

        Optional<MarkerRecord> marker = markerRegistry.getById(markerId);
        if (marker.isEmpty()) {
            return Optional.empty();
        }

        if (marker.get().type() != markerType) {
            return Optional.empty();
        }

        if (!marker.get().worldId().equals(npc.worldId())) {
            return Optional.empty();
        }

        return marker;
    }

    public Optional<MarkerRecord> resolveRequiredMarkerReadOnly(NpcRecord npc, MarkerType markerType) {
        String assignedMarkerId = markerIdForType(npc, markerType);
        return resolveMarkerInNpcWorld(npc, markerType, assignedMarkerId);
    }

    public Optional<MarkerType> resolveMarkerTypeForRole(TargetRole role) {
        if (role == null || role == TargetRole.NONE) {
            return Optional.empty();
        }
        return switch (role) {
            case BED -> Optional.of(MarkerType.BED);
            case WORK -> Optional.of(MarkerType.WORK);
            case DOOR -> Optional.of(MarkerType.DOOR);
            case CHEST -> Optional.of(MarkerType.CHEST);
            case FOOD -> Optional.of(MarkerType.FOOD);
            case CHILL -> Optional.of(MarkerType.CHILL);
            case NONE -> Optional.empty();
        };
    }

    public Optional<MarkerType> resolveAuthoritativeMarkerType(NpcState state) {
        if (state == null || !state.isIdle()) {
            return Optional.empty();
        }
        return resolveMarkerTypeForRole(state.markerRole());
    }

    public boolean hasAuthoritativeIdleMarker(NpcState state) {
        return resolveAuthoritativeMarkerType(state).isPresent();
    }

}
