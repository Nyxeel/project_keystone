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

    public void upsert(MarkerRecord marker) {
        byId.put(marker.markerId(), marker);
        lastByType.put(marker.type(), marker.markerId());
    }

    public Optional<MarkerRecord> getById(String markerId) {
        return Optional.ofNullable(byId.get(markerId));
    }

    /** MVP A: use "last set marker" as active marker. */
    public Optional<MarkerRecord> getActive(MarkerType type) {
        var id = lastByType.get(type);
        if (id == null) return Optional.empty();
        return getById(id);
    }

    public List<MarkerRecord> snapshot() {
        return byId.values().stream().toList();
    }

    public void restore(List<MarkerRecord> markers) {
        byId.clear();
        lastByType.clear();
        for (var m : markers) {
            upsert(m);
        }
    }
}
