package keystone.npc.world;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MVP A: simple Registry.
 *
 * Responsibilities:
 * - Store marker positions
 * - Provide lookup by type for the current "active" setup
 * - Snapshot/restore for persistence
 */
public final class MarkerRegistry {

    private final Map<String, MarkerRecord> byId = new LinkedHashMap<>();
    private final EnumMap<MarkerType, LinkedHashSet<String>> orderedIdsByType = new EnumMap<>(MarkerType.class);

    /** Convenience: last set marker per type (MVP A UX). */
    private final EnumMap<MarkerType, String> lastByType = new EnumMap<>(MarkerType.class);

    public synchronized void upsert(MarkerRecord marker) {
        MarkerRecord previous = byId.put(marker.markerId(), marker);
        if (previous != null && previous.type() != marker.type()) {
            orderedIdsByType.computeIfAbsent(previous.type(), key -> new LinkedHashSet<>()).remove(marker.markerId());
        }

        LinkedHashSet<String> orderedIds = orderedIdsByType.computeIfAbsent(marker.type(), key -> new LinkedHashSet<>());
        // Reinsert to keep "latest set" order deterministic for fallback ring traversal.
        orderedIds.remove(marker.markerId());
        orderedIds.add(marker.markerId());
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
        orderedIdsByType.clear();
        lastByType.clear();
    }

    /** MVP A helper: clear only active marker selection while keeping marker records. */
    public synchronized void clearActive() {
        lastByType.clear();
    }

    public synchronized Optional<MarkerRecord> getById(String markerId) {
        return Optional.ofNullable(byId.get(markerId));
    }

    /** Returns markers of a type in deterministic set order (oldest -> newest). */
    public synchronized List<MarkerRecord> getCandidates(MarkerType type) {
        return getCandidates(type, null);
    }

    /** Returns markers of a type in deterministic set order, optionally filtered to one world. */
    public synchronized List<MarkerRecord> getCandidates(MarkerType type, WorldId worldId) {
        LinkedHashSet<String> orderedIds = orderedIdsByType.get(type);
        if (orderedIds == null || orderedIds.isEmpty()) {
            return List.of();
        }

        List<MarkerRecord> candidates = new ArrayList<>(orderedIds.size());
        for (String markerId : orderedIds) {
            MarkerRecord marker = byId.get(markerId);
            if (marker == null || marker.type() != type) {
                continue;
            }
            if (worldId != null && !worldId.equals(marker.worldId())) {
                continue;
            }
            candidates.add(marker);
        }
        return candidates;
    }

    /**
     * Resolve the next valid marker after the given marker id, with wrap-around.
     * If currentMarkerId is unknown, search starts from the ring head.
     */
    public synchronized Optional<MarkerRecord> getNextAvailable(MarkerType type, String currentMarkerId, WorldId worldId) {
        LinkedHashSet<String> orderedIds = orderedIdsByType.get(type);
        if (orderedIds == null || orderedIds.isEmpty()) {
            return Optional.empty();
        }

        List<String> ring = new ArrayList<>(orderedIds);
        int size = ring.size();
        int startIndex = (currentMarkerId == null || currentMarkerId.isBlank()) ? -1 : ring.indexOf(currentMarkerId);

        for (int offset = 1; offset <= size; offset++) {
            int index = startIndex >= 0 ? (startIndex + offset) % size : offset - 1;
            String candidateId = ring.get(index);
            MarkerRecord marker = byId.get(candidateId);
            if (marker == null || marker.type() != type) {
                continue;
            }
            if (worldId != null && !worldId.equals(marker.worldId())) {
                continue;
            }
            return Optional.of(marker);
        }

        return Optional.empty();
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
        orderedIdsByType.clear();
        lastByType.clear();

        for (var m : markers) {
            byId.put(m.markerId(), m);
            orderedIdsByType.computeIfAbsent(m.type(), key -> new LinkedHashSet<>()).add(m.markerId());
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
