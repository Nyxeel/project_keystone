package keystone.npc.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MarkerRegistry {

    private final Map<String, MarkerRecord> markersById = new LinkedHashMap<>();

    public boolean register(MarkerRecord marker) {
        if (!isValidMarker(marker)) {
            return false;
        }

        markersById.put(marker.markerId(), marker);
        return true;
    }

    public Optional<MarkerRecord> findById(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(markersById.get(markerId));
    }

    public boolean exists(String markerId) {
        return findById(markerId).isPresent();
    }

    public boolean remove(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return false;
        }

        return markersById.remove(markerId) != null;
    }

    public List<MarkerRecord> findByWorld(String worldId) {
        if (worldId == null || worldId.isBlank()) {
            return List.of();
        }

        List<MarkerRecord> result = new ArrayList<>();

        for (MarkerRecord marker : markersById.values()) {
            if (worldId.equals(marker.worldId())) {
                result.add(marker);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public List<MarkerRecord> findByWorldAndType(String worldId, MarkerType markerType) {
        if (worldId == null || worldId.isBlank() || markerType == null) {
            return List.of();
        }

        List<MarkerRecord> result = new ArrayList<>();

        for (MarkerRecord marker : markersById.values()) {
            if (worldId.equals(marker.worldId()) && markerType == marker.markerType()) {
                result.add(marker);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public Collection<MarkerRecord> snapshot() {
        return Collections.unmodifiableCollection(new ArrayList<>(markersById.values()));
    }

    public void restore(Collection<MarkerRecord> markers) {
        markersById.clear();

        if (markers == null) {
            return;
        }

        for (MarkerRecord marker : markers) {
            register(marker);
        }
    }

    public void clear() {
        markersById.clear();
    }

    private boolean isValidMarker(MarkerRecord marker) {
        if (marker == null) {
            return false;
        }

        if (marker.markerId() == null || marker.markerId().isBlank()) {
            return false;
        }

        if (marker.markerName() == null || marker.markerName().isBlank()) {
            return false;
        }

        if (marker.markerType() == null) {
            return false;
        }

        if (marker.worldId() == null || marker.worldId().isBlank()) {
            return false;
        }

        return marker.position() != null && marker.position().isFinite();
    }

    public enum MarkerType {
        BED,
        DOOR,
        WORK,
        FOOD,
        CHEST,
        CHILL,
        GUARD,
        PATROL,
        SPAWN
    }

    public record MarkerRecord(
            String markerId,
            String markerName,
            MarkerType markerType,
            String worldId,
            MarkerPosition position
    ) {
        public MarkerRecord {
            Objects.requireNonNull(markerId, "markerId");
            Objects.requireNonNull(markerName, "markerName");
            Objects.requireNonNull(markerType, "markerType");
            Objects.requireNonNull(worldId, "worldId");
            Objects.requireNonNull(position, "position");
        }
    }

    public record MarkerPosition(
            double x,
            double y,
            double z
    ) {
        public boolean isFinite() {
            return Double.isFinite(x)
                    && Double.isFinite(y)
                    && Double.isFinite(z);
        }
    }
}