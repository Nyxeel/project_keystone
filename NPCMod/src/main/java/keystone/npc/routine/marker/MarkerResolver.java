package keystone.npc.routine.marker;

import java.util.Objects;
import java.util.Optional;

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
        return switch (markerType) {
            case BED -> npc.bedMarkerId();
            case DOOR -> npc.doorMarkerId();
            case CHEST -> npc.chestMarkerId();
            case FOOD -> npc.foodMarkerId();
            case WORK -> npc.workMarkerId();
            case CHILL -> npc.chillMarkerId();
        };
    }

    public void setMarkerIdForType(NpcRecord npc, MarkerType markerType, String markerId) {
        switch (markerType) {
            case BED -> npc.bedMarkerId(markerId);
            case DOOR -> npc.doorMarkerId(markerId);
            case CHEST -> npc.chestMarkerId(markerId);
            case FOOD -> npc.foodMarkerId(markerId);
            case WORK -> npc.workMarkerId(markerId);
            case CHILL -> npc.chillMarkerId(markerId);
        }
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
