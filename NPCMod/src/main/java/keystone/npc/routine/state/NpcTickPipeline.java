package keystone.npc.routine.state;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.hypixel.hytale.server.core.universe.world.World;

import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.domain.TargetRole;
import keystone.npc.markers.Vec3;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.entity.EntitySyncService;
import keystone.npc.routine.marker.IdleMarkerService;
import keystone.npc.routine.pathfinding.NavigationRuntimeService;

public final class NpcTickPipeline {
    private final RoleDefinitionRegistry roleDefinitions;
    private final StateTargetingService stateTargetingService;
    private final NavigationRuntimeService navigationRuntimeService;
    private final IdleMarkerService idleMarkerService;
    private final EntitySyncService entitySync;
    private final boolean engineNavigationEnabled;
    private final BiConsumer<NpcRecord, String> missingMarkerWarningSink;

    public NpcTickPipeline(
        RoleDefinitionRegistry roleDefinitions,
        StateTargetingService stateTargetingService,
        NavigationRuntimeService navigationRuntimeService,
        IdleMarkerService idleMarkerService,
        EntitySyncService entitySync,
        boolean engineNavigationEnabled,
        BiConsumer<NpcRecord, String> missingMarkerWarningSink
    ) {
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.stateTargetingService = Objects.requireNonNull(stateTargetingService);
        this.navigationRuntimeService = Objects.requireNonNull(navigationRuntimeService);
        this.idleMarkerService = Objects.requireNonNull(idleMarkerService);
        this.entitySync = Objects.requireNonNull(entitySync);
        this.engineNavigationEnabled = engineNavigationEnabled;
        this.missingMarkerWarningSink = Objects.requireNonNull(missingMarkerWarningSink);
    }

    public void updateNpc(NpcRecord npc, World world) {
        // Keep legacy RoleDefinition lookup as safety fallback while JSON-first definitions are rolled out.
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
            NpcState activeTargetState = navState.getTargetState();
            if (activeTargetState != null && activeTargetState != desiredTargetState) {
                System.out.println("[KeystoneNPC] Navigation reroute: npc=" + npc.npcName()
                    + " activeTarget=" + targetTypeForState(activeTargetState)
                    + " newTarget=" + targetTypeForState(desiredTargetState)
                    + " currentPos=" + npc.currentPosition());
                navState.clear();

                stopActionForTargetChange(npc, desiredTarget.actionId());
                npc.pendingActionId(desiredTarget.actionId());
                stateTargetingService.startNavigationToMarker(npc, desiredTarget.markerType(), desiredTargetState);
                return;
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

        if (npc.state() != desiredTargetState) {
            stopActionForTargetChange(npc, desiredTarget.actionId());
            npc.pendingActionId(desiredTarget.actionId());
            stateTargetingService.startNavigationToMarker(npc, desiredTarget.markerType(), desiredTargetState);
            return;
        }

        idleMarkerService.enforceAuthoritativeIdlePosition(npc, "idle-state-check", true);
        updateActionState(npc, desiredTarget.actionId());
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
