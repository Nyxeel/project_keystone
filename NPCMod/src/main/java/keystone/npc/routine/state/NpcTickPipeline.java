package keystone.npc.routine.state;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.hypixel.hytale.server.core.universe.world.World;

import keystone.npc.domain.NpcEntityStatus;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.domain.TargetRole;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.entity.EntitySyncService;
import keystone.npc.routine.marker.IdleMarkerService;
import keystone.npc.routine.marker.MarkerResolver;
import keystone.npc.routine.pathfinding.NavigationRuntimeService;

public final class NpcTickPipeline {
    private static final double TARGET_POSITION_REROUTE_EPSILON_SQ = 0.01d;
    private static final double IDLE_TARGET_HORIZONTAL_EPSILON_SQ = 0.25d;
    private static final double IDLE_TARGET_Y_TOLERANCE = 1.25d;

    private final RoleDefinitionRegistry roleDefinitions;
    private final StateTargetingService stateTargetingService;
    private final NavigationRuntimeService navigationRuntimeService;
    private final IdleMarkerService idleMarkerService;
    private final MarkerResolver markerResolver;
    private final EntitySyncService entitySync;
    private final boolean engineNavigationEnabled;
    private final BiConsumer<NpcRecord, String> missingMarkerWarningSink;

    public NpcTickPipeline(
        RoleDefinitionRegistry roleDefinitions,
        StateTargetingService stateTargetingService,
        NavigationRuntimeService navigationRuntimeService,
        IdleMarkerService idleMarkerService,
        MarkerResolver markerResolver,
        EntitySyncService entitySync,
        boolean engineNavigationEnabled,
        BiConsumer<NpcRecord, String> missingMarkerWarningSink
    ) {
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.stateTargetingService = Objects.requireNonNull(stateTargetingService);
        this.navigationRuntimeService = Objects.requireNonNull(navigationRuntimeService);
        this.idleMarkerService = Objects.requireNonNull(idleMarkerService);
        this.markerResolver = Objects.requireNonNull(markerResolver);
        this.entitySync = Objects.requireNonNull(entitySync);
        this.engineNavigationEnabled = engineNavigationEnabled;
        this.missingMarkerWarningSink = Objects.requireNonNull(missingMarkerWarningSink);
    }

    public void updateNpc(NpcRecord npc, World world) {
        if (npc.entityStatus() == NpcEntityStatus.DISABLED) {
            return;
        }

        if (npc.entityRef() == null || !npc.entityRef().isValid()) {
            return;
        }

        // Runtime role definitions are loaded from JSON and must resolve before targeting/navigation.
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KNPC][Warning] " + npc.npcName()
                    + " has no role definition for roleId=" + npc.roleId() + ".");
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        if (!stateTargetingService.hasRequiredMarkers(npc, roleDefinition.get())) {
            String missingMarkers = stateTargetingService.missingRequiredMarkers(npc, roleDefinition.get());
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KNPC][Warning] " + npc.npcName()
                    + " is missing required marker assignments: "
                    + missingMarkers + ".");
                missingMarkerWarningSink.accept(npc, missingMarkers);
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        StateTargetingService.DesiredTarget desiredTarget = stateTargetingService.resolveDesiredTarget(world, npc, roleDefinition.get());
        if (desiredTarget == null) {
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KNPC][Warning] " + npc.npcName() + " has no valid navigation target.");
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        NpcState desiredTargetState = desiredTarget.targetState();

        NavigationTarget navState = npc.navigationState();
        navigationRuntimeService.maybeTickDoorCloseMaintenance(world, npc);

        if (navigationRuntimeService.hasActiveNavigation(navState)) {
            String rerouteReason = resolveActiveNavigationRerouteReason(navState, desiredTarget);
            if (rerouteReason != null) {
                System.out.println("[KeystoneNPC] Navigation reroute: npc=" + npc.npcName()
                    + " activeTarget=" + targetTypeForState(navState.getTargetState())
                    + " newTarget=" + targetTypeForState(desiredTargetState)
                    + " reason=" + rerouteReason
                    + " currentPos=" + npc.currentPosition());

                syncNpcPositionFromLiveEntity(npc);
                navState.clear();

                if (!hasValidEntityRef(npc)) {
                    return;
                }

                stopActionForTargetChange(npc, desiredTarget.targetActionId());
                npc.pendingActionId(desiredTarget.targetActionId());
                stateTargetingService.startNavigationToTarget(npc, desiredTarget);
                return;
            }

            if (!Objects.equals(npc.pendingActionId(), desiredTarget.targetActionId())) {
                npc.pendingActionId(desiredTarget.targetActionId());
            }

            if (engineNavigationEnabled) {
                if (navigationRuntimeService.tickEngineNavigation(world, npc, navState)) {
                    if (!navigationRuntimeService.hasActiveNavigation(navState)) {
                        return;
                    }
                    return;
                }

                if (!navigationRuntimeService.hasActiveNavigation(navState)) {
                    return;
                }
            }

            if (navState.isComplete()) {
                navigationRuntimeService.finishNavigation(world, npc, navState);
                return;
            }

            Vec3 currentPos = navState.getCurrentPosition();
            if (currentPos != null) {
                npc.currentPosition(currentPos);
                entitySync.updateEntityPosition(npc, currentPos);
            }

            if (navState.isComplete()) {
                navigationRuntimeService.finishNavigation(world, npc, navState);
            }
            return;
        }

        if (npc.state() != desiredTargetState || shouldStartNavigationFromIdleSameState(npc, desiredTarget)) {
            if (!hasValidEntityRef(npc)) {
                return;
            }

            stopActionForTargetChange(npc, desiredTarget.targetActionId());
            npc.pendingActionId(desiredTarget.targetActionId());
            stateTargetingService.startNavigationToTarget(npc, desiredTarget);
            return;
        }

        idleMarkerService.enforceAuthoritativeIdlePosition(npc, "idle-state-check", true);
        updateActionState(npc, desiredTarget.targetActionId());
    }

    private void updateActionState(NpcRecord npc, String desiredActionId) {
        if (desiredActionId == null || desiredActionId.isBlank()) {
            if (npc.activeActionId() != null) {
                System.out.println("[KNPC][Action] " + npc.npcName()
                    + " stop action=" + npc.activeActionId()
                    + " reason=no_desired_action");
                npc.activeActionId(null);
                npc.lastActionNoRestartLog(null);
            }
            return;
        }

        if (desiredActionId.equals(npc.activeActionId())) {
            if (!desiredActionId.equals(npc.lastActionNoRestartLog())) {
                System.out.println("[KNPC][Action] " + npc.npcName()
                    + " keep action=" + desiredActionId
                    + " reason=already_active");
                npc.lastActionNoRestartLog(desiredActionId);
            }
            return;
        }

        if (!desiredActionId.equals(npc.activeActionId())) {
            npc.activeActionId(desiredActionId);
            npc.lastActionNoRestartLog(null);
            System.out.println("[KNPC][Action] " + npc.npcName()
                + " start action=" + desiredActionId
                + " state=" + npc.state().name()
                + " marker=" + markerLabel(npc.activeRoutineMarker()));
        }
    }

    private void stopActionForTargetChange(NpcRecord npc, String nextActionId) {
        String activeAction = npc.activeActionId();
        if (activeAction == null || activeAction.isBlank()) {
            return;
        }

        if (Objects.equals(activeAction, nextActionId)) {
            return;
        }

        System.out.println("[KNPC][Action] " + npc.npcName()
            + " stop action=" + activeAction
            + " reason=target_changed");
        npc.activeActionId(null);
        npc.lastActionNoRestartLog(null);
    }

    private String resolveActiveNavigationRerouteReason(
        NavigationTarget navState,
        StateTargetingService.DesiredTarget desiredTarget
    ) {
        NpcState activeTargetState = navState.getTargetState();
        if (!Objects.equals(activeTargetState, desiredTarget.targetState())) {
            return "state";
        }

        if (!Objects.equals(navState.getTargetMarkerName(), desiredTarget.targetMarkerName())) {
            return "marker-name";
        }

        if (!Objects.equals(navState.getTargetMarkerType(), desiredTarget.markerType())) {
            return "marker-type";
        }

        if (!Objects.equals(navState.getTargetMarkerId(), desiredTarget.markerId())) {
            return "marker-id";
        }

        if (!Objects.equals(navState.getTargetActionId(), desiredTarget.targetActionId())) {
            return "target-action";
        }

        if (!sameTargetPosition(navState.getTargetPosition(), desiredTarget.targetPosition(), TARGET_POSITION_REROUTE_EPSILON_SQ)) {
            return "target-position";
        }

        return null;
    }

    private boolean shouldStartNavigationFromIdleSameState(
        NpcRecord npc,
        StateTargetingService.DesiredTarget desiredTarget
    ) {
        if (npc.state() != desiredTarget.targetState()) {
            return false;
        }

        if (!npc.state().isIdle()) {
            return false;
        }

        MarkerType stateMarkerType = markerTypeForState(npc.state());
        if (stateMarkerType == null) {
            return false;
        }

        if (!Objects.equals(stateMarkerType, desiredTarget.markerType())) {
            return true;
        }

        String currentMarkerId = markerIdForType(npc, stateMarkerType);
        if (!Objects.equals(currentMarkerId, desiredTarget.markerId())) {
            return true;
        }

        Vec3 desiredTargetPosition = desiredTarget.targetPosition();
        if (desiredTargetPosition == null) {
            return false;
        }

        Vec3 livePosition = entitySync.readPosition(npc.entityRef());
        Vec3 currentPosition = livePosition != null ? livePosition : npc.currentPosition();
        if (livePosition != null) {
            npc.currentPosition(livePosition);
        }

        if (currentPosition == null) {
            return true;
        }

        return !isAtIdleTarget(currentPosition, desiredTargetPosition);
    }

    private boolean hasValidEntityRef(NpcRecord npc) {
        return npc != null && npc.entityRef() != null && npc.entityRef().isValid();
    }

    private MarkerType markerTypeForState(NpcState state) {
        if (state == null) {
            return null;
        }

        return switch (state.markerRole()) {
            case BED -> MarkerType.BED;
            case WORK -> MarkerType.WORK;
            case DOOR -> MarkerType.DOOR;
            case CHEST -> MarkerType.CHEST;
            case FOOD -> MarkerType.FOOD;
            case CHILL -> MarkerType.CHILL;
            case NONE -> null;
        };
    }

    private String markerIdForType(NpcRecord npc, MarkerType markerType) {
        if (markerType == null) {
            return null;
        }

        return markerResolver.markerIdForType(npc, markerType);
    }

    private boolean sameTargetPosition(Vec3 left, Vec3 right, double epsilonSq) {
        if (left == null || right == null) {
            return left == right;
        }
        return distanceSq(left, right) <= epsilonSq;
    }

    private double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private boolean isAtIdleTarget(Vec3 currentPosition, Vec3 targetPosition) {
        double dx = currentPosition.x() - targetPosition.x();
        double dz = currentPosition.z() - targetPosition.z();
        double horizontalDistanceSq = dx * dx + dz * dz;
        if (horizontalDistanceSq > IDLE_TARGET_HORIZONTAL_EPSILON_SQ) {
            return false;
        }

        return Math.abs(currentPosition.y() - targetPosition.y()) <= IDLE_TARGET_Y_TOLERANCE;
    }

    private void syncNpcPositionFromLiveEntity(NpcRecord npc) {
        Vec3 livePosition = entitySync.readPosition(npc.entityRef());
        if (livePosition != null) {
            npc.currentPosition(livePosition);
        }
    }

    private String markerLabel(String marker) {
        if (marker == null || marker.isBlank()) {
            return "-";
        }
        return marker;
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
            case CHEST -> "CHEST";
            case FOOD -> "FOOD";
            case CHILL -> "CHILL";
            case NONE -> "UNKNOWN";
        };
    }
}
