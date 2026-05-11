package keystone.npc.routine.pathfinding;

import java.util.Locale;
import java.util.Map;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.doorway.DoorwayFlow;
import keystone.npc.doorway.PendingDoorAttempt;
import keystone.npc.markers.Vec3;
import keystone.npc.navigation.EngineNavigationController;
import keystone.npc.navigation.NavigationTarget;

public final class NavigationRuntimeService {
    private static final double ARRIVAL_Y_TOLERANCE = 1.25d;

    private final EngineNavigationController engineNavigation;
    private final PathfindingSupport pathfindingSupport;
    private final DoorwayFlow doorWorkflowService;
    private final double engineNavigationArrivalDistanceSq;
    private final Map<String, PendingDoorAttempt> pendingDoorAttempts;

    public NavigationRuntimeService(
        EngineNavigationController engineNavigation,
        PathfindingSupport pathfindingSupport,
        DoorwayFlow doorWorkflowService,
        double engineNavigationArrivalDistanceSq,
        Map<String, PendingDoorAttempt> pendingDoorAttempts
    ) {
        this.engineNavigation = engineNavigation;
        this.pathfindingSupport = pathfindingSupport;
        this.doorWorkflowService = doorWorkflowService;
        this.engineNavigationArrivalDistanceSq = engineNavigationArrivalDistanceSq;
        this.pendingDoorAttempts = pendingDoorAttempts;
    }

    public boolean hasActiveNavigation(NavigationTarget navState) {
        return pathfindingSupport.hasActiveNavigation(navState);
    }

    public boolean tickEngineNavigation(World world, NpcRecord npc, NavigationTarget navState) {
        if (!hasLiveEntity(npc)) {
            return false;
        }

        Vec3 routeTarget = navState.getTargetPosition();
        if (routeTarget == null) {
            return false;
        }

        Ref<EntityStore> entityRef = npc.entityRef();
        if (!engineNavigation.setTarget(entityRef, routeTarget, npc.motionControllerType())) {
            return false;
        }

        Vec3 currentPos = engineNavigation.readCurrentPosition(entityRef);
        if (currentPos != null) {
            npc.currentPosition(currentPos);
            doorWorkflowService.maybeHandleDoorNavigation(world, npc, navState, currentPos);
        }

        if (currentPos != null && hasReachedNavigationTarget(npc, currentPos, routeTarget)) {
            finishNavigation(world, npc, navState);
        }

        return true;
    }

    public void finishNavigation(World world, NpcRecord npc, NavigationTarget navState) {
        doorWorkflowService.closeTrackedDoorAfterNavigation(world, npc);

        Vec3 targetPos = navState.getTargetPosition();
        Vec3 livePos = engineNavigation.readCurrentPosition(npc.entityRef());
        if (livePos != null) {
            npc.currentPosition(livePos);
        } else if (targetPos != null) {
            npc.currentPosition(targetPos);
        }

        NpcState targetState = navState.getTargetState();
        if (targetState != null) {
            npc.state(targetState);
        }

        String pendingActionId = npc.pendingActionId();
        double arrivalDistance = targetPos == null || npc.currentPosition() == null
            ? 0.0d
            : Math.sqrt(distanceSq(npc.currentPosition(), targetPos));
        double stopDistance = effectiveStopDistance(npc);

        if (pendingActionId != null && !pendingActionId.isBlank()) {
            if (!pendingActionId.equals(npc.activeActionId())) {
                npc.activeActionId(pendingActionId);
                npc.lastActionNoRestartLog(null);
                System.out.println("[KNPC][Action] " + npc.npcName()
                    + " start action=" + pendingActionId
                    + " state=" + npc.state().name()
                    + " marker=" + markerLabel(npc.activeRoutineMarker()));
            } else if (!pendingActionId.equals(npc.lastActionNoRestartLog())) {
                System.out.println("[KNPC][Action] " + npc.npcName()
                    + " keep action=" + pendingActionId
                    + " reason=already_active");
                npc.lastActionNoRestartLog(pendingActionId);
            }
        } else if (npc.activeActionId() != null) {
            System.out.println("[KNPC][Action] " + npc.npcName()
                + " stop action=" + npc.activeActionId()
                + " reason=target_changed");
            npc.activeActionId(null);
            npc.lastActionNoRestartLog(null);
        }

        System.out.println("[KNPC][Navigation] " + npc.npcName()
            + " reached marker=" + markerLabel(npc.activeRoutineMarker())
            + " distance=" + String.format(Locale.ROOT, "%.2f", arrivalDistance)
            + " stopDistance=" + String.format(Locale.ROOT, "%.2f", stopDistance)
            + " nextAction=" + nullToDash(pendingActionId));

        npc.pendingActionId(null);

        pendingDoorAttempts.remove(npc.npcId());
        navState.clear();
    }

    public void maybeTickDoorCloseMaintenance(World world, NpcRecord npc) {
        Vec3 currentPos = engineNavigation.readCurrentPosition(npc.entityRef());
        if (currentPos != null) {
            npc.currentPosition(currentPos);
        } else {
            currentPos = npc.currentPosition();
        }

        if (currentPos != null) {
            doorWorkflowService.maybeHandleDoorCloseAfterPass(world, npc, currentPos, null);
        }
    }

    private boolean hasLiveEntity(NpcRecord npc) {
        Ref<EntityStore> entityRef = npc.entityRef();
        return entityRef != null && entityRef.isValid();
    }

    private double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private double horizontalDistanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dz = a.z() - b.z();
        return dx * dx + dz * dz;
    }

    private boolean hasReachedNavigationTarget(NpcRecord npc, Vec3 currentPos, Vec3 routeTarget) {
        double stopDistance = effectiveStopDistance(npc);
        if (horizontalDistanceSq(currentPos, routeTarget) > stopDistance * stopDistance) {
            return false;
        }
        return Math.abs(currentPos.y() - routeTarget.y()) <= ARRIVAL_Y_TOLERANCE;
    }

    private double effectiveStopDistance(NpcRecord npc) {
        Double stopDistance = npc.stopDistance();
        if (stopDistance == null || !Double.isFinite(stopDistance) || stopDistance <= 0.0d) {
            return Math.sqrt(engineNavigationArrivalDistanceSq);
        }
        return stopDistance;
    }

    private String markerLabel(String marker) {
        if (marker == null || marker.isBlank()) {
            return "-";
        }
        return marker;
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
