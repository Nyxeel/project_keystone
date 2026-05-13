package keystone.npc.markers;

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
    private final MarkerRingTraversal ringTraversal = new MarkerRingTraversal();

    /** Convenience: last set marker per type (MVP A UX). */
    private final EnumMap<MarkerType, String> lastByType = new EnumMap<>(MarkerType.class);

    public synchronized void upsert(MarkerRecord marker) {
        upsertInternal(marker, true);
    }

    /**
     * Insert or update a marker record without changing active marker selection.
     *
     * Use this when marker records must exist for persistence/validation,
     * but should not become the active marker by type.
     */
    public synchronized void upsertRecordOnly(MarkerRecord marker) {
        upsertInternal(marker, false);
    }

    private void upsertInternal(MarkerRecord marker, boolean updateLastByType) {
        MarkerRecord previous = byId.put(marker.markerId(), marker);
        if (previous != null && previous.type() != marker.type()) {
            orderedIdsByType.computeIfAbsent(previous.type(), key -> new LinkedHashSet<>()).remove(marker.markerId());
        }

        LinkedHashSet<String> orderedIds = orderedIdsByType.computeIfAbsent(marker.type(), key -> new LinkedHashSet<>());
        // Reinsert to keep "latest set" order deterministic for fallback ring traversal.
        orderedIds.remove(marker.markerId());
        orderedIds.add(marker.markerId());
        if (updateLastByType) {
            lastByType.put(marker.type(), marker.markerId());
        }
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

    public synchronized boolean removeById(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return false;
        }

        MarkerRecord removed = byId.remove(markerId);
        if (removed == null) {
            return false;
        }

        LinkedHashSet<String> orderedIds = orderedIdsByType.get(removed.type());
        if (orderedIds != null) {
            orderedIds.remove(markerId);
            if (orderedIds.isEmpty()) {
                orderedIdsByType.remove(removed.type());
            }
        }

        lastByType.entrySet().removeIf(entry -> markerId.equals(entry.getValue()));
        return true;
    }

    /** Returns markers of a type in deterministic set order (oldest -> newest). */
    public synchronized List<MarkerRecord> getCandidates(MarkerType type) {
        return getCandidates(type, null);
    }

    /** Returns markers of a type in deterministic set order, optionally filtered to one world. */
    public synchronized List<MarkerRecord> getCandidates(MarkerType type, WorldId worldId) {
        return ringTraversal.getCandidates(byId, orderedIdsByType, type, worldId);
    }

    /**
     * Resolve the next valid marker after the given marker id, with wrap-around.
     * If currentMarkerId is unknown, search starts from the ring head.
     *
     * Safety contract: never use this method in restore/tick/relink/respawn-policy
     * paths to auto-replace persisted marker assignments.
     */
    @Deprecated
    public synchronized Optional<MarkerRecord> getNextAvailable(MarkerType type, String currentMarkerId, WorldId worldId) {
        return ringTraversal.getNextAvailable(byId, orderedIdsByType, type, currentMarkerId, worldId);
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
        }

        // Safety: do not auto-revive active markers from plain marker lists.
        if (activeMarkerIds == null || activeMarkerIds.isEmpty()) {
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
