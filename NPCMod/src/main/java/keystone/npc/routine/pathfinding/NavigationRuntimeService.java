package keystone.npc.routine.pathfinding;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import keystone.npc.domain.TargetRole;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.navigation.EngineNavigationController;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.doorway.PendingDoorAttempt;
import keystone.npc.doorway.DoorwayFlow;
import keystone.npc.markers.Vec3;

public final class NavigationRuntimeService {
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
        if (!engineNavigation.setTarget(entityRef, routeTarget)) {
            return false;
        }

        Vec3 currentPos = engineNavigation.readCurrentPosition(entityRef);
        if (currentPos != null) {
            npc.currentPosition(currentPos);
            doorWorkflowService.maybeHandleDoorNavigation(world, npc, navState, currentPos);
        }

        if (currentPos != null && distanceSq(currentPos, routeTarget) <= engineNavigationArrivalDistanceSq) {
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

        System.out.println("[KeystoneNPC] Navigation reached: npc=" + npc.npcName()
            + " reachedTarget=" + targetTypeForState(targetState)
            + " newState=" + npc.state());
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

    private String targetTypeForState(NpcState state) {
        if (state == null) {
            return "UNKNOWN";
        }

        TargetRole markerRole = state.markerRole();
        if (markerRole == null || markerRole == TargetRole.NONE) {
            return "UNKNOWN";
        }

        return switch (markerRole) {
            case BED -> "BED";
            case WORK -> "WORK";
            case DOOR -> "DOOR";
            case NONE -> "UNKNOWN";
        };
    }

    private double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
