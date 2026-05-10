package keystone.npc.world;

import java.util.*;

/**
 * MVP A: simple Registry.
 *
 * Responsibilities:
 * - Store marker positions
 * - Provide lookup by type for the current "active" setup
 * - Snapshot/restore for persistence
 */
public final class MarkerRegistry {

    private final Map<String, MarkerRecord> byId = new HashMap<>();

    /** Convenience: last set marker per type (MVP A UX). */
    private final EnumMap<MarkerType, String> lastByType = new EnumMap<>(MarkerType.class);

    public synchronized void upsert(MarkerRecord marker) {
        byId.put(marker.markerId(), marker);
        lastByType.put(marker.type(), marker.markerId());
    }

    /** MVP A helper: set active marker for a type (creates a new record). */
    public synchronized void setActive(MarkerType type, WorldId worldId, Vec3 position) {
        String markerId = java.util.UUID.randomUUID().toString();
        upsert(new MarkerRecord(markerId, worldId, position, type));
    }

    /** MVP A helper: clear all markers. */
    public synchronized void clear() {
        byId.clear();
        lastByType.clear();
    }

    /** MVP A helper: clear only active marker selection while keeping marker records. */
    public synchronized void clearActive() {
        lastByType.clear();
    }

    public synchronized Optional<MarkerRecord> getById(String markerId) {
        return Optional.ofNullable(byId.get(markerId));
    }

    /** MVP A: use "last set marker" as active marker. */
    public synchronized Optional<MarkerRecord> getActive(MarkerType type) {
        var id = lastByType.get(type);
        if (id == null) {
            return Optional.empty();
        }

        MarkerRecord marker = byId.get(id);
        if (marker == null || marker.type() != type) {
            lastByType.remove(type);
            return Optional.empty();
        }

        return Optional.of(marker);
    }

    public synchronized List<MarkerRecord> snapshot() {
        return new ArrayList<>(byId.values());
    }

    public synchronized Map<MarkerType, String> snapshotActiveMarkerIds() {
        return new EnumMap<>(lastByType);
    }

    public synchronized void restore(List<MarkerRecord> markers) {
        restore(markers, Map.of());
    }

    public synchronized void restore(List<MarkerRecord> markers, Map<MarkerType, String> activeMarkerIds) {
        byId.clear();
        lastByType.clear();

        for (var m : markers) {
            byId.put(m.markerId(), m);
            // Compatibility fallback for older saves without explicit active marker map.
            lastByType.put(m.type(), m.markerId());
        }

        // Compatibility path: old saves without active marker map keep fallback behavior.
        if (activeMarkerIds == null) {
            return;
        }

        // Explicit map (including empty) overrides fallback behavior.
        lastByType.clear();
        if (activeMarkerIds.isEmpty()) {
            return;
        }

        for (var entry : activeMarkerIds.entrySet()) {
            MarkerType type = entry.getKey();
            String markerId = entry.getValue();
            if (type == null || markerId == null || markerId.isBlank()) {
                continue;
            }

            MarkerRecord marker = byId.get(markerId);
            if (marker == null) {
                System.err.println("[KeystoneNPC] Ignoring active marker for " + type + ": marker not found " + markerId);
                continue;
            }

            if (marker.type() != type) {
                System.err.println("[KeystoneNPC] Ignoring active marker for " + type
                    + ": marker type mismatch (" + marker.type() + ")");
                continue;
            }

            lastByType.put(type, markerId);
        }
    }
}
