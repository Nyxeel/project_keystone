package keystone.npc.routine.marker;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.domain.TargetRole;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;

public final class MarkerResolver {
    private final MarkerRegistry markerRegistry;
    private final BiConsumer<String, String> logInfoSink;

    public MarkerResolver(MarkerRegistry markerRegistry, BiConsumer<String, String> logInfoSink) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.logInfoSink = Objects.requireNonNull(logInfoSink);
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

    // Compatibility entrypoint: keeps historical behavior and therefore mutates assignments.
    // Use resolveRequiredMarkerReadOnly in restore/tick/validation/diagnostic paths.
    public Optional<MarkerRecord> resolveRequiredMarkerWithFallback(NpcRecord npc, MarkerType markerType) {
        return resolveRequiredMarkerWithFallbackAssigning(npc, markerType);
    }

    public Optional<MarkerRecord> resolveRequiredMarkerReadOnly(NpcRecord npc, MarkerType markerType) {
        String assignedMarkerId = markerIdForType(npc, markerType);
        return resolveMarkerInNpcWorld(npc, markerType, assignedMarkerId);
    }

    // Explicit mutating variant for assignment contexts (e.g., spawn/admin assignment flows).
    public Optional<MarkerRecord> resolveRequiredMarkerWithFallbackAssigning(NpcRecord npc, MarkerType markerType) {
        String assignedMarkerId = markerIdForType(npc, markerType);
        Optional<MarkerRecord> direct = resolveMarkerInNpcWorld(npc, markerType, assignedMarkerId);
        if (direct.isPresent()) {
            return direct;
        }

        String ringAnchorMarkerId = resolveRingFallbackAnchorMarkerId(npc, markerType, assignedMarkerId);
        Optional<MarkerRecord> fallback = markerRegistry.getNextAvailable(markerType, ringAnchorMarkerId, npc.worldId());
        if (fallback.isPresent()) {
            String oldMarkerId = assignedMarkerId;
            String newMarkerId = fallback.get().markerId();
            setMarkerIdForType(npc, markerType, newMarkerId);
            logInfo("MARKER_FALLBACK_SELECTED", "Resolved missing marker via ring fallback: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerType=" + markerType.name()
                + " oldMarkerId=" + nullToDash(oldMarkerId)
                + " ringAnchorMarkerId=" + nullToDash(ringAnchorMarkerId)
                + " newMarkerId=" + newMarkerId);
            return fallback;
        }

        return Optional.empty();
    }

    public String resolveRingFallbackAnchorMarkerId(NpcRecord npc, MarkerType markerType, String assignedMarkerId) {
        if (assignedMarkerId != null && !assignedMarkerId.isBlank()) {
            for (MarkerRecord candidate : markerRegistry.getCandidates(markerType, npc.worldId())) {
                if (assignedMarkerId.equals(candidate.markerId())) {
                    return assignedMarkerId;
                }
            }
        }

        Vec3 current = npc.currentPosition();
        if (current == null) {
            return assignedMarkerId;
        }

        List<MarkerRecord> candidates = markerRegistry.getCandidates(markerType, npc.worldId());
        if (candidates.isEmpty()) {
            return assignedMarkerId;
        }

        MarkerRecord closest = candidates.get(0);
        double closestDistanceSq = distanceSq(current, closest.position());
        for (int i = 1; i < candidates.size(); i++) {
            MarkerRecord candidate = candidates.get(i);
            double candidateDistanceSq = distanceSq(current, candidate.position());
            if (candidateDistanceSq < closestDistanceSq) {
                closest = candidate;
                closestDistanceSq = candidateDistanceSq;
            }
        }

        return closest.markerId();
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

    private double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String quote(String value) {
        return value == null ? "-" : value;
    }

    private void logInfo(String eventKey, String message) {
        logInfoSink.accept(eventKey, message);
    }
}
