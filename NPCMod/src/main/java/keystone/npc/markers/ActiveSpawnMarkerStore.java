package keystone.npc.markers;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Runtime-only role-scoped spawn staging store.
 *
 * Key shape:
 * roleId -> markerName -> staged marker
 */
public final class ActiveSpawnMarkerStore {

    private final Map<String, Map<String, StagedSpawnMarker>> activeSpawnMarkersByRole = new LinkedHashMap<>();

    public synchronized void put(
        String roleId,
        String markerName,
        String markerId,
        MarkerType markerType,
        WorldId worldId,
        Vec3 position
    ) {
        String normalizedRoleId = normalizeRoleId(roleId);
        String normalizedMarkerName = normalizeMarkerName(markerName);
        String normalizedMarkerId = normalizeMarkerId(markerId);

        Objects.requireNonNull(markerType, "markerType");
        Objects.requireNonNull(worldId, "worldId");
        Objects.requireNonNull(position, "position");

        activeSpawnMarkersByRole
            .computeIfAbsent(normalizedRoleId, ignored -> new LinkedHashMap<>())
            .put(normalizedMarkerName, new StagedSpawnMarker(
                normalizedRoleId,
                normalizedMarkerName,
                normalizedMarkerId,
                markerType,
                worldId,
                position,
                System.currentTimeMillis()
            ));
    }

    public synchronized Optional<StagedSpawnMarker> get(String roleId, String markerName) {
        String normalizedRoleId = normalizeRoleId(roleId);
        String normalizedMarkerName = normalizeMarkerName(markerName);

        Map<String, StagedSpawnMarker> markersByName = activeSpawnMarkersByRole.get(normalizedRoleId);
        if (markersByName == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(markersByName.get(normalizedMarkerName));
    }

    public synchronized Map<String, StagedSpawnMarker> snapshotRole(String roleId) {
        String normalizedRoleId = normalizeRoleId(roleId);
        Map<String, StagedSpawnMarker> markersByName = activeSpawnMarkersByRole.get(normalizedRoleId);
        if (markersByName == null || markersByName.isEmpty()) {
            return Map.of();
        }

        return Map.copyOf(markersByName);
    }

    public synchronized boolean remove(String roleId, String markerName) {
        String normalizedRoleId = normalizeRoleId(roleId);
        String normalizedMarkerName = normalizeMarkerName(markerName);

        Map<String, StagedSpawnMarker> markersByName = activeSpawnMarkersByRole.get(normalizedRoleId);
        if (markersByName == null) {
            return false;
        }

        StagedSpawnMarker removed = markersByName.remove(normalizedMarkerName);
        if (markersByName.isEmpty()) {
            activeSpawnMarkersByRole.remove(normalizedRoleId);
        }

        return removed != null;
    }

    public synchronized void clearRole(String roleId) {
        activeSpawnMarkersByRole.remove(normalizeRoleId(roleId));
    }

    public synchronized void clearAll() {
        activeSpawnMarkersByRole.clear();
    }

    private static String normalizeRoleId(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }
        return roleId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeMarkerName(String markerName) {
        if (markerName == null || markerName.isBlank()) {
            throw new IllegalArgumentException("markerName must not be blank");
        }
        return markerName.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeMarkerId(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            throw new IllegalArgumentException("markerId must not be blank");
        }
        return markerId.trim();
    }

    public record StagedSpawnMarker(
        String roleId,
        String markerName,
        String markerId,
        MarkerType markerType,
        WorldId worldId,
        Vec3 position,
        long stagedAtEpochMs
    ) {
        public StagedSpawnMarker {
            Objects.requireNonNull(roleId, "roleId");
            Objects.requireNonNull(markerName, "markerName");
            Objects.requireNonNull(markerId, "markerId");
            Objects.requireNonNull(markerType, "markerType");
            Objects.requireNonNull(worldId, "worldId");
            Objects.requireNonNull(position, "position");
        }
    }
}