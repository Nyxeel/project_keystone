package keystone.npc.persistence;

import java.util.EnumMap;
import java.util.Map;

import keystone.npc.markers.MarkerType;

final class ActiveMarkerIdMapper {
    Map<String, String> toPersistedActiveMarkerIds(Map<MarkerType, String> activeMarkerIds) {
        Map<String, String> persisted = new java.util.LinkedHashMap<>();
        if (activeMarkerIds == null) {
            return persisted;
        }

        for (var entry : activeMarkerIds.entrySet()) {
            MarkerType type = entry.getKey();
            String markerId = entry.getValue();
            if (type == null || markerId == null || markerId.isBlank()) {
                continue;
            }
            persisted.put(type.name(), markerId);
        }

        return persisted;
    }

    Map<MarkerType, String> toActiveMarkerIds(Map<String, String> persistedActiveMarkerIds) {
        if (persistedActiveMarkerIds == null) {
            return null;
        }

        Map<MarkerType, String> activeMarkerIds = new EnumMap<>(MarkerType.class);

        for (var entry : persistedActiveMarkerIds.entrySet()) {
            String rawType = entry.getKey();
            String markerId = entry.getValue();
            if (rawType == null || markerId == null || markerId.isBlank()) {
                continue;
            }

            try {
                MarkerType type = MarkerType.valueOf(rawType);
                activeMarkerIds.put(type, markerId);
            } catch (IllegalArgumentException ex) {
                System.err.println("[KeystoneNPC] Ignoring unknown active marker type in state: " + rawType);
            }
        }

        return activeMarkerIds;
    }
}
