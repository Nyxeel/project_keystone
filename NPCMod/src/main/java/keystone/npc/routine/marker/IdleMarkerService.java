package keystone.npc.routine.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;
import keystone.npc.routine.entity.EntitySyncService;

public final class IdleMarkerService {
    private final MarkerResolver markerResolver;
    private final EntitySyncService entitySync;
    private final double engineNavigationArrivalDistanceSq;
    private final double idlePositionEpsilonSq;
    private final double roleAnchorRelinkRadiusSq;
    private final BiConsumer<String, String> logInfoSink;

    public IdleMarkerService(
        MarkerResolver markerResolver,
        EntitySyncService entitySync,
        double engineNavigationArrivalDistanceSq,
        double idlePositionEpsilonSq,
        double roleAnchorRelinkRadiusSq,
        BiConsumer<String, String> logInfoSink
    ) {
        this.markerResolver = Objects.requireNonNull(markerResolver);
        this.entitySync = Objects.requireNonNull(entitySync);
        this.engineNavigationArrivalDistanceSq = engineNavigationArrivalDistanceSq;
        this.idlePositionEpsilonSq = idlePositionEpsilonSq;
        this.roleAnchorRelinkRadiusSq = roleAnchorRelinkRadiusSq;
        this.logInfoSink = Objects.requireNonNull(logInfoSink);
    }

    public void normalizeRestorePosition(NpcRecord npc) {
        if (npc.state() != null && npc.state().isWalking()) {
            return;
        }

        Optional<MarkerType> authoritativeMarkerType = markerResolver.resolveAuthoritativeMarkerType(npc.state());
        if (authoritativeMarkerType.isPresent()) {
            enforceAuthoritativeIdlePosition(npc, "restore", false);
            return;
        }

        Vec3 current = npc.currentPosition();
        MarkerRecord anchor = resolveRestoreAnchor(npc, current);
        if (anchor == null) {
            return;
        }

        Vec3 anchorPos = anchor.position();
        if (current == null || distanceSq(current, anchorPos) > roleAnchorRelinkRadiusSq) {
            npc.currentPosition(anchorPos);
            logInfo("RESTORE_POSITION_SNAP", "Adjusted NPC position to marker anchor during restore: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " state=" + npc.state().name()
                + " markerType=" + anchor.type().name()
                + " markerId=" + anchor.markerId()
                + " newPos=" + formatPosition(anchorPos));
        }
    }

    public MarkerRecord resolveRestoreAnchor(NpcRecord npc, Vec3 current) {
        Optional<MarkerRecord> preferred = resolveStatePreferredMarker(npc);
        if (preferred.isPresent()) {
            return preferred.get();
        }

        List<MarkerRecord> candidates = collectRestoreMarkerCandidates(npc);
        if (candidates.isEmpty()) {
            return null;
        }

        if (current == null) {
            return candidates.get(0);
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

        return closest;
    }

    public Optional<MarkerRecord> resolveStatePreferredMarker(NpcRecord npc) {
        Optional<MarkerType> authoritativeMarkerType = markerResolver.resolveAuthoritativeMarkerType(npc.state());
        if (authoritativeMarkerType.isPresent()) {
            return markerResolver.resolveRequiredMarkerWithFallback(npc, authoritativeMarkerType.get());
        }

        if (npc.state().isWalking()) {
            Optional<MarkerType> walkingMarkerType = markerResolver.resolveMarkerTypeForRole(npc.state().markerRole());
            if (walkingMarkerType.isPresent()) {
                return markerResolver.resolveRequiredMarkerWithFallback(npc, walkingMarkerType.get());
            }
        }

        return Optional.empty();
    }

    public boolean enforceAuthoritativeIdlePosition(NpcRecord npc, String reason, boolean alignEntity) {
        Optional<MarkerType> markerType = markerResolver.resolveAuthoritativeMarkerType(npc.state());
        if (markerType.isEmpty()) {
            return false;
        }

        Optional<MarkerRecord> marker = markerResolver.resolveRequiredMarkerWithFallback(npc, markerType.get());
        if (marker.isEmpty()) {
            return false;
        }

        Vec3 authoritativePos = marker.get().position();
        Vec3 currentPos = npc.currentPosition();
        double allowedDriftSq = isSoftIdleAlignmentReason(reason)
            ? engineNavigationArrivalDistanceSq
            : idlePositionEpsilonSq;
        boolean changed = currentPos == null || distanceSq(currentPos, authoritativePos) > allowedDriftSq;

        if (changed) {
            npc.currentPosition(authoritativePos);
            logInfo("IDLE_POSITION_ENFORCED", "Authoritative idle marker position applied: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " state=" + npc.state().name()
                + " markerType=" + markerType.get().name()
                + " reason=" + quote(reason)
                + " newPos=" + formatPosition(authoritativePos));
        }

        if (alignEntity) {
            alignEntityToNpcPosition(npc, reason);
        }

        return changed;
    }

    public void alignEntityToNpcPosition(NpcRecord npc, String reason) {
        if (npc.entityRef() == null || !npc.entityRef().isValid()) {
            return;
        }

        Vec3 authoritativePos = npc.currentPosition();
        if (authoritativePos == null) {
            return;
        }

        Vec3 livePos = entitySync.readPosition(npc.entityRef());
        if (livePos == null || distanceSq(livePos, authoritativePos) > idlePositionEpsilonSq) {
            entitySync.updateEntityPosition(npc, authoritativePos);
            logInfo("ENTITY_ALIGN_TO_STATE", "Aligned entity transform to authoritative NPC position: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " reason=" + quote(reason)
                + " entityPos=" + formatPosition(livePos)
                + " targetPos=" + formatPosition(authoritativePos));
        }
    }

    private List<MarkerRecord> collectRestoreMarkerCandidates(NpcRecord npc) {
        List<MarkerRecord> candidates = new ArrayList<>(3);
        addRestoreMarkerCandidate(candidates, npc, MarkerType.BED);
        addRestoreMarkerCandidate(candidates, npc, MarkerType.WORK);
        addRestoreMarkerCandidate(candidates, npc, MarkerType.DOOR);
        return candidates;
    }

    private void addRestoreMarkerCandidate(List<MarkerRecord> candidates, NpcRecord npc, MarkerType markerType) {
        Optional<MarkerRecord> marker = markerResolver.resolveRequiredMarkerWithFallback(npc, markerType);
        marker.ifPresent(candidates::add);
    }

    private boolean isSoftIdleAlignmentReason(String reason) {
        return "idle-state-check".equals(reason)
            || "idle-marker-authority".equals(reason)
            || "startup-idle-guard".equals(reason);
    }

    private double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private String quote(String value) {
        return value == null ? "-" : value;
    }

    private String formatPosition(Vec3 pos) {
        if (pos == null) {
            return "-";
        }
        return String.format(java.util.Locale.ROOT, "%.3f,%.3f,%.3f", pos.x(), pos.y(), pos.z());
    }

    private void logInfo(String eventKey, String message) {
        logInfoSink.accept(eventKey, message);
    }
}
