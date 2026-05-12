package keystone.npc.doorway;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.universe.world.World;

import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.routine.marker.MarkerResolver;

public final class DoorwayFlow {

    private static final long DOOR_MARKER_SKIP_LOG_COOLDOWN_MS = 10_000L;

    @FunctionalInterface
    public interface DoorLogSink {
        void log(NpcRecord npc, String eventKey, String message);
    }

    @FunctionalInterface
    public interface DoorCapabilityGate {
        boolean canOpenDoors(NpcRecord npc);
    }

    private final MarkerResolver markerResolver;
    private final DoorwayScanner doorSupport;
    private final DoorPassTracker doorPassTracker;
    private final RequiredMarkerResolver requiredMarkerResolver;
    private final Map<String, Long> nextDoorActionAtMs;
    private final Map<String, Long> nextDoorCloseActionAtMs;
    private final Map<String, Long> nextDoorMarkerSkipLogAtMs;
    private final Map<String, PendingDoorAttempt> pendingDoorAttempts;
    private final Map<String, PendingDoorAttempt> pendingDoorCloseAttempts;
    private final double doorTriggerDistanceSq;
    private final double doorRouteMaxDistanceSq;
    private final long doorActionCooldownMs;
    private final long doorChainTimeoutMs;
    private final double doorCloseMinDistanceSq;
    private final DoorLogSink doorLogSink;
    private final DoorCapabilityGate doorCapabilityGate;

    public DoorwayFlow(
        MarkerResolver markerResolver,
        DoorwayScanner doorSupport,
        DoorPassTracker doorPassTracker,
        RequiredMarkerResolver requiredMarkerResolver,
        Map<String, Long> nextDoorActionAtMs,
        Map<String, Long> nextDoorCloseActionAtMs,
        Map<String, Long> nextDoorMarkerSkipLogAtMs,
        Map<String, PendingDoorAttempt> pendingDoorAttempts,
        Map<String, PendingDoorAttempt> pendingDoorCloseAttempts,
        double doorTriggerDistanceSq,
        double doorRouteMaxDistanceSq,
        long doorActionCooldownMs,
        long doorChainTimeoutMs,
        double doorCloseMinDistanceSq,
        DoorLogSink doorLogSink,
        DoorCapabilityGate doorCapabilityGate
    ) {
        this.markerResolver = markerResolver;
        this.doorSupport = doorSupport;
        this.doorPassTracker = doorPassTracker;
        this.requiredMarkerResolver = Objects.requireNonNull(requiredMarkerResolver);
        this.nextDoorActionAtMs = nextDoorActionAtMs;
        this.nextDoorCloseActionAtMs = nextDoorCloseActionAtMs;
        this.nextDoorMarkerSkipLogAtMs = nextDoorMarkerSkipLogAtMs;
        this.pendingDoorAttempts = pendingDoorAttempts;
        this.pendingDoorCloseAttempts = pendingDoorCloseAttempts;
        this.doorTriggerDistanceSq = doorTriggerDistanceSq;
        this.doorRouteMaxDistanceSq = doorRouteMaxDistanceSq;
        this.doorActionCooldownMs = doorActionCooldownMs;
        this.doorChainTimeoutMs = doorChainTimeoutMs;
        this.doorCloseMinDistanceSq = doorCloseMinDistanceSq;
        this.doorLogSink = doorLogSink;
        this.doorCapabilityGate = doorCapabilityGate;
    }

    public void maybeHandleDoorNavigation(World world, NpcRecord npc, NavigationTarget navState, Vec3 currentPos) {
        if (npc.state() != NpcState.WALKING_TO_BED && npc.state() != NpcState.WALKING_TO_WORK) {
            return;
        }

        if (!isDoorRequiredForRole(npc)) {
            pendingDoorAttempts.remove(npc.npcId());
            pendingDoorCloseAttempts.remove(npc.npcId());
            while (doorPassTracker.peekActiveDoorPass(npc.npcId()) != null) {
                doorPassTracker.removeTrackedDoorPass(npc.npcId(), null);
            }
            return;
        }

        if (!doorCapabilityGate.canOpenDoors(npc)) {
            return;
        }

        Vec3 targetPos = navState.getTargetPosition();
        if (targetPos == null || currentPos == null) {
            return;
        }

        maybeHandleDoorCloseAfterPass(world, npc, currentPos, targetPos);

        PendingDoorAttempt pending = pendingDoorAttempts.get(npc.npcId());
        if (pending != null) {
            maybeFinalizePendingDoorAttempt(world, npc, pending, targetPos);
            if (pendingDoorAttempts.containsKey(npc.npcId())) {
                return;
            }
        }

        Optional<MarkerRecord> doorMarker = markerResolver.resolveMarkerInNpcWorld(npc, MarkerType.DOOR, npc.doorMarkerId());
        BlockPosition markerDoorBlock = null;
        if (doorMarker.isPresent()) {
            Vec3 doorPos = doorMarker.get().position();
            if (distanceSq(currentPos, doorPos) <= doorTriggerDistanceSq
                && doorSupport.distanceSqToSegment(doorPos, currentPos, targetPos) <= doorRouteMaxDistanceSq) {
                markerDoorBlock = doorSupport.resolveDoorBlock(world, doorPos);
            }
        }

        BlockPosition doorBlock = doorSupport.resolveApproachDoorBlock(world, currentPos, targetPos, markerDoorBlock);
        if (doorBlock == null) {
            maybeLogDoorMarkerNotSet(npc, doorMarker.isEmpty());
            return;
        }

        String doorMarkerId = doorMarker.isPresent() && doorSupport.sameBlock(doorBlock, markerDoorBlock)
            ? doorMarker.get().markerId()
            : "local-route-door";

        if (doorSupport.isDoorOpened(world, doorBlock)) {
            pendingDoorAttempts.remove(npc.npcId());
            registerOpenedDoorForClose(npc, doorBlock, doorMarkerId, targetPos);
            return;
        }

        long now = System.currentTimeMillis();
        long nextAllowedAt = nextDoorActionAtMs.getOrDefault(npc.npcId(), 0L);
        if (now < nextAllowedAt) {
            return;
        }

        boolean chainStarted = doorSupport.tryQueueDoorInteractionChain(world, npc, doorBlock);
        if (chainStarted) {
            pendingDoorAttempts.put(npc.npcId(), new PendingDoorAttempt(doorBlock, doorMarkerId, now));
            nextDoorActionAtMs.put(npc.npcId(), now + doorActionCooldownMs);
            logDoorInfo(npc, "DOOR_OPEN_CHAIN_REQUESTED", "Queued interaction-chain door open attempt: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerId=" + nullToDash(doorMarkerId)
                + " block=" + doorSupport.formatBlockPosition(doorBlock)
                + " cooldownMs=" + doorActionCooldownMs);
            return;
        }

        boolean fallbackSuccess = doorSupport.tryOpenDoorFallback(world, doorBlock);
        nextDoorActionAtMs.put(npc.npcId(), now + doorActionCooldownMs);
        if (fallbackSuccess) {
            registerOpenedDoorForClose(npc, doorBlock, doorMarkerId, targetPos);
        }
        logDoorInfo(npc, "DOOR_OPEN_DIRECT_FALLBACK_USED", "Interaction-chain unavailable, used direct fallback: "
            + "npcId=" + npc.npcId()
            + " npcName=" + quote(npc.npcName())
            + " markerId=" + nullToDash(doorMarkerId)
            + " block=" + doorSupport.formatBlockPosition(doorBlock)
            + " finalResult=" + (fallbackSuccess ? "OPENED" : "FAILED"));
    }

    public void maybeHandleDoorCloseAfterPass(World world, NpcRecord npc, Vec3 currentPos, Vec3 targetPos) {
        ActiveDoorPass activeDoor = doorPassTracker.peekActiveDoorPass(npc.npcId());
        if (activeDoor == null) {
            return;
        }

        if (!doorSupport.isDoorOpened(world, activeDoor.doorBlock())) {
            logDoorInfo(npc, "DOOR_CLOSE_CHAIN_SUCCEEDED", "Door already closed; close action completed: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerId=" + nullToDash(activeDoor.doorMarkerId())
                + " block=" + doorSupport.formatBlockPosition(activeDoor.doorBlock())
                + " result=ALREADY_CLOSED");
            pendingDoorCloseAttempts.remove(npc.npcId());
            doorPassTracker.removeTrackedDoorPass(npc.npcId(), activeDoor.doorBlock());
            return;
        }

        PendingDoorAttempt pendingClose = pendingDoorCloseAttempts.get(npc.npcId());
        if (pendingClose != null) {
            maybeFinalizePendingDoorCloseAttempt(world, npc, pendingClose);
            if (pendingDoorCloseAttempts.containsKey(npc.npcId())) {
                return;
            }

            activeDoor = doorPassTracker.peekActiveDoorPass(npc.npcId());
            if (activeDoor == null) {
                return;
            }
        }

        Vec3 closeTarget = activeDoor.targetPosition() != null ? activeDoor.targetPosition() : targetPos;
        if (closeTarget == null) {
            return;
        }

        Vec3 doorCenter = doorSupport.toBlockCenter(activeDoor.doorBlock());
        if (distanceSq(currentPos, doorCenter) < doorCloseMinDistanceSq) {
            return;
        }

        if (distanceSq(currentPos, closeTarget) >= distanceSq(doorCenter, closeTarget)) {
            return;
        }

        long now = System.currentTimeMillis();
        long nextAllowedAt = nextDoorCloseActionAtMs.getOrDefault(npc.npcId(), 0L);
        if (now < nextAllowedAt) {
            return;
        }

        boolean chainStarted = doorSupport.tryQueueDoorInteractionChain(world, npc, activeDoor.doorBlock());
        if (chainStarted) {
            pendingDoorCloseAttempts.put(npc.npcId(), new PendingDoorAttempt(activeDoor.doorBlock(), activeDoor.doorMarkerId(), now));
            nextDoorCloseActionAtMs.put(npc.npcId(), now + doorActionCooldownMs);
            logDoorInfo(npc, "DOOR_CLOSE_CHAIN_REQUESTED", "Queued interaction-chain close-after-pass attempt: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerId=" + nullToDash(activeDoor.doorMarkerId())
                + " block=" + doorSupport.formatBlockPosition(activeDoor.doorBlock()));
            return;
        }

        boolean fallbackSuccess = doorSupport.tryCloseDoorFallback(world, activeDoor.doorBlock());
        nextDoorCloseActionAtMs.put(npc.npcId(), now + doorActionCooldownMs);
        if (fallbackSuccess) {
            pendingDoorCloseAttempts.remove(npc.npcId());
            doorPassTracker.removeTrackedDoorPass(npc.npcId(), activeDoor.doorBlock());
        }

        logDoorInfo(npc, "DOOR_CLOSE_DIRECT_FALLBACK_USED", "Close-after-pass chain unavailable, used fallback: "
            + "npcId=" + npc.npcId()
            + " npcName=" + quote(npc.npcName())
            + " markerId=" + nullToDash(activeDoor.doorMarkerId())
            + " block=" + doorSupport.formatBlockPosition(activeDoor.doorBlock())
            + " finalResult=" + (fallbackSuccess ? "CLOSED" : "FAILED"));
    }

    public void closeTrackedDoorAfterNavigation(World world, NpcRecord npc) {
        ActiveDoorPass activeDoor = doorPassTracker.peekActiveDoorPass(npc.npcId());
        if (activeDoor == null) {
            return;
        }

        if (!doorSupport.isDoorOpened(world, activeDoor.doorBlock())) {
            logDoorInfo(npc, "DOOR_CLOSE_CHAIN_SUCCEEDED", "Door already closed at navigation end; close action completed: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerId=" + nullToDash(activeDoor.doorMarkerId())
                + " block=" + doorSupport.formatBlockPosition(activeDoor.doorBlock())
                + " result=ALREADY_CLOSED");
            pendingDoorCloseAttempts.remove(npc.npcId());
            doorPassTracker.removeTrackedDoorPass(npc.npcId(), activeDoor.doorBlock());
            return;
        }

        long now = System.currentTimeMillis();
        long nextAllowedAt = nextDoorCloseActionAtMs.getOrDefault(npc.npcId(), 0L);
        if (now < nextAllowedAt) {
            return;
        }

        boolean chainStarted = doorSupport.tryQueueDoorInteractionChain(world, npc, activeDoor.doorBlock());
        if (chainStarted) {
            pendingDoorCloseAttempts.put(npc.npcId(), new PendingDoorAttempt(activeDoor.doorBlock(), activeDoor.doorMarkerId(), now));
            nextDoorCloseActionAtMs.put(npc.npcId(), now + doorActionCooldownMs);
            logDoorInfo(npc, "DOOR_CLOSE_ON_ARRIVAL", "Queued door close attempt at navigation finish: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerId=" + nullToDash(activeDoor.doorMarkerId())
                + " block=" + doorSupport.formatBlockPosition(activeDoor.doorBlock()));
            return;
        }

        boolean fallbackSuccess = doorSupport.tryCloseDoorFallback(world, activeDoor.doorBlock());
        nextDoorCloseActionAtMs.put(npc.npcId(), now + doorActionCooldownMs);
        if (fallbackSuccess) {
            pendingDoorCloseAttempts.remove(npc.npcId());
            doorPassTracker.removeTrackedDoorPass(npc.npcId(), activeDoor.doorBlock());
        }

        logDoorInfo(npc, "DOOR_CLOSE_ON_ARRIVAL_FALLBACK", "Close on arrival used fallback: "
            + "npcId=" + npc.npcId()
            + " npcName=" + quote(npc.npcName())
            + " markerId=" + nullToDash(activeDoor.doorMarkerId())
            + " block=" + doorSupport.formatBlockPosition(activeDoor.doorBlock())
            + " finalResult=" + (fallbackSuccess ? "CLOSED" : "FAILED"));
    }

    private void maybeFinalizePendingDoorAttempt(World world, NpcRecord npc, PendingDoorAttempt pending, Vec3 targetPos) {
        if (doorSupport.isDoorOpened(world, pending.doorBlock())) {
            pendingDoorAttempts.remove(npc.npcId());
            registerOpenedDoorForClose(npc, pending.doorBlock(), pending.doorMarkerId(), targetPos);
            logDoorInfo(npc, "DOOR_OPEN_CHAIN_SUCCEEDED", "Interaction-chain opened door successfully: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerId=" + nullToDash(pending.doorMarkerId())
                + " block=" + doorSupport.formatBlockPosition(pending.doorBlock()));
            return;
        }

        long elapsedMs = System.currentTimeMillis() - pending.startedAtMs();
        if (elapsedMs < doorChainTimeoutMs) {
            return;
        }

        boolean fallbackSuccess = doorSupport.tryOpenDoorFallback(world, pending.doorBlock());
        pendingDoorAttempts.remove(npc.npcId());
        if (fallbackSuccess) {
            registerOpenedDoorForClose(npc, pending.doorBlock(), pending.doorMarkerId(), targetPos);
        }

        logDoorInfo(npc, "DOOR_OPEN_CHAIN_TIMED_OUT_FALLBACK_USED", "Interaction-chain timed out, fallback executed: "
            + "npcId=" + npc.npcId()
            + " npcName=" + quote(npc.npcName())
            + " markerId=" + nullToDash(pending.doorMarkerId())
            + " block=" + doorSupport.formatBlockPosition(pending.doorBlock())
            + " timeoutMs=" + doorChainTimeoutMs
            + " finalResult=" + (fallbackSuccess ? "OPENED" : "FAILED"));
    }

    private void registerOpenedDoorForClose(NpcRecord npc, BlockPosition doorBlock, String doorMarkerId, Vec3 targetPos) {
        doorPassTracker.registerOpenedDoorForClose(npc.npcId(), doorBlock, doorMarkerId, targetPos);
    }

    private void maybeFinalizePendingDoorCloseAttempt(World world, NpcRecord npc, PendingDoorAttempt pendingClose) {
        if (!doorSupport.isDoorOpened(world, pendingClose.doorBlock())) {
            pendingDoorCloseAttempts.remove(npc.npcId());
            doorPassTracker.removeTrackedDoorPass(npc.npcId(), pendingClose.doorBlock());
            logDoorInfo(npc, "DOOR_CLOSE_CHAIN_SUCCEEDED", "Interaction-chain closed door successfully: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerId=" + nullToDash(pendingClose.doorMarkerId())
                + " block=" + doorSupport.formatBlockPosition(pendingClose.doorBlock()));
            return;
        }

        long elapsedMs = System.currentTimeMillis() - pendingClose.startedAtMs();
        if (elapsedMs < doorChainTimeoutMs) {
            return;
        }

        boolean fallbackSuccess = doorSupport.tryCloseDoorFallback(world, pendingClose.doorBlock());
        pendingDoorCloseAttempts.remove(npc.npcId());
        if (fallbackSuccess) {
            doorPassTracker.removeTrackedDoorPass(npc.npcId(), pendingClose.doorBlock());
        }

        logDoorInfo(npc, "DOOR_CLOSE_CHAIN_TIMED_OUT_FALLBACK_USED", "Close chain timed out, fallback executed: "
            + "npcId=" + npc.npcId()
            + " npcName=" + quote(npc.npcName())
            + " markerId=" + nullToDash(pendingClose.doorMarkerId())
            + " block=" + doorSupport.formatBlockPosition(pendingClose.doorBlock())
            + " timeoutMs=" + doorChainTimeoutMs
            + " finalResult=" + (fallbackSuccess ? "CLOSED" : "FAILED"));
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

    private void logDoorInfo(NpcRecord npc, String eventKey, String message) {
        doorLogSink.log(npc, eventKey, message);
    }

    private boolean isDoorRequiredForRole(NpcRecord npc) {
        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(npc.roleId())) {
            if (requirement.markerType() == MarkerType.DOOR) {
                return true;
            }
        }
        return false;
    }

    private void maybeLogDoorMarkerNotSet(NpcRecord npc, boolean doorMarkerMissing) {
        if (!doorMarkerMissing) {
            return;
        }

        long now = System.currentTimeMillis();
        long nextAllowedAt = nextDoorMarkerSkipLogAtMs.getOrDefault(npc.npcId(), 0L);
        if (now < nextAllowedAt) {
            return;
        }

        nextDoorMarkerSkipLogAtMs.put(npc.npcId(), now + DOOR_MARKER_SKIP_LOG_COOLDOWN_MS);
        logDoorInfo(
            npc,
            "DOOR_MARKER_NOT_SET_ROUTING_SKIPPED",
            "Door marker not set. Doorway routing skipped for this NPC."
        );
    }
}
