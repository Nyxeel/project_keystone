package keystone.npc.markers;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class MarkerRingTraversal {
    List<MarkerRecord> getCandidates(
        Map<String, MarkerRecord> byId,
        EnumMap<MarkerType, LinkedHashSet<String>> orderedIdsByType,
        MarkerType type,
        WorldId worldId
    ) {
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

    Optional<MarkerRecord> getNextAvailable(
        Map<String, MarkerRecord> byId,
        EnumMap<MarkerType, LinkedHashSet<String>> orderedIdsByType,
        MarkerType type,
        String currentMarkerId,
        WorldId worldId
    ) {
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
}
