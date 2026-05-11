package keystone.npc.routine.state;

import com.hypixel.hytale.server.core.universe.world.World;
import java.util.Objects;
import java.util.Optional;
import keystone.npc.domain.TargetRole;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.entity.EntitySyncService;
import keystone.npc.routine.marker.IdleMarkerService;
import keystone.npc.routine.pathfinding.NavigationRuntimeService;
import keystone.npc.markers.Vec3;

public final class NpcTickPipeline {
    private final RoleDefinitionRegistry roleDefinitions;
    private final StateTargetingService stateTargetingService;
    private final NavigationRuntimeService navigationRuntimeService;
    private final IdleMarkerService idleMarkerService;
    private final EntitySyncService entitySync;
    private final boolean engineNavigationEnabled;

    public NpcTickPipeline(
        RoleDefinitionRegistry roleDefinitions,
        StateTargetingService stateTargetingService,
        NavigationRuntimeService navigationRuntimeService,
        IdleMarkerService idleMarkerService,
        EntitySyncService entitySync,
        boolean engineNavigationEnabled
    ) {
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.stateTargetingService = Objects.requireNonNull(stateTargetingService);
        this.navigationRuntimeService = Objects.requireNonNull(navigationRuntimeService);
        this.idleMarkerService = Objects.requireNonNull(idleMarkerService);
        this.entitySync = Objects.requireNonNull(entitySync);
        this.engineNavigationEnabled = engineNavigationEnabled;
    }

    public void updateNpc(NpcRecord npc, World world) {
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KeystoneNPC] Missing role definition for NPC '" + npc.npcName()
                    + "' (" + npc.npcId() + "): roleId=" + npc.roleId());
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        if (!stateTargetingService.hasRequiredMarkers(npc, roleDefinition.get())) {
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KeystoneNPC] Missing marker assignment for NPC '" + npc.npcName() + "' ("
                    + npc.npcId() + "): required=" + stateTargetingService.missingRequiredMarkers(npc, roleDefinition.get()));
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        NpcState desiredTargetState = stateTargetingService.resolveDesiredTargetState(world, npc, roleDefinition.get());
        if (desiredTargetState == null) {
            return;
        }

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

                if (desiredTargetState == NpcState.SLEEPING) {
                    stateTargetingService.startNavigationToBed(npc);
                } else if (desiredTargetState == NpcState.WORKING) {
                    stateTargetingService.startNavigationToWork(npc);
                }
                return;
            }

            boolean engineNavigationTicked = false;
            if (engineNavigationEnabled) {
                engineNavigationTicked = navigationRuntimeService.tickEngineNavigation(world, npc, navState);

                if (!navigationRuntimeService.hasActiveNavigation(navState)) {
                    return;
                }

                if (engineNavigationTicked) {
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

        if (desiredTargetState == NpcState.SLEEPING) {
            if (npc.state() != NpcState.SLEEPING) {
                stateTargetingService.startNavigationToBed(npc);
            } else {
                idleMarkerService.enforceAuthoritativeIdlePosition(npc, "idle-state-check", true);
            }
        } else if (desiredTargetState == NpcState.WORKING) {
            if (npc.state() != NpcState.WORKING) {
                stateTargetingService.startNavigationToWork(npc);
            } else {
                idleMarkerService.enforceAuthoritativeIdlePosition(npc, "idle-state-check", true);
            }
        }
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
}
