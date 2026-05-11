package keystone.npc.routine.state;

import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.navigation.EngineNavigationController;
import keystone.npc.navigation.NpcNavigation;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.doorway.ActiveDoorPass;
import keystone.npc.doorway.PendingDoorAttempt;
import keystone.npc.routine.marker.MarkerResolver;
import keystone.npc.routine.pathfinding.PathfindingSupport;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;

public final class StateTargetingService {
    private final MarkerResolver markerResolver;
    private final PathfindingSupport pathfindingSupport;
    private final EngineNavigationController engineNavigation;
    private final Map<String, Deque<ActiveDoorPass>> activeDoorPasses;
    private final Map<String, PendingDoorAttempt> pendingDoorAttempts;
    private final Map<String, PendingDoorAttempt> pendingDoorCloseAttempts;
    private final boolean engineNavigationEnabled;

    public StateTargetingService(
        MarkerResolver markerResolver,
        PathfindingSupport pathfindingSupport,
        EngineNavigationController engineNavigation,
        Map<String, Deque<ActiveDoorPass>> activeDoorPasses,
        Map<String, PendingDoorAttempt> pendingDoorAttempts,
        Map<String, PendingDoorAttempt> pendingDoorCloseAttempts,
        boolean engineNavigationEnabled
    ) {
        this.markerResolver = markerResolver;
        this.pathfindingSupport = pathfindingSupport;
        this.engineNavigation = engineNavigation;
        this.activeDoorPasses = activeDoorPasses;
        this.pendingDoorAttempts = pendingDoorAttempts;
        this.pendingDoorCloseAttempts = pendingDoorCloseAttempts;
        this.engineNavigationEnabled = engineNavigationEnabled;
    }

    public boolean startNavigationToBed(NpcRecord npc) {
        Optional<MarkerRecord> bedMarker = markerResolver.resolveRequiredMarkerWithFallback(npc, MarkerType.BED);
        if (bedMarker.isEmpty()) {
            System.err.println("[KeystoneNPC] Missing bed marker for NPC '" + npc.npcName() + "' (" + npc.npcId() + ")"
                + " markerId=" + npc.bedMarkerId());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return false;
        }

        Vec3 bedPos = bedMarker.get().position();
        Vec3 startPos = pathfindingSupport.resolveNavigationStartPosition(npc, bedPos);
        long durationMs = NpcNavigation.calculateDurationMs(startPos, bedPos);

        npc.navigationState().startNavigation(startPos, bedPos, durationMs, NpcState.SLEEPING);
        npc.state(NpcState.WALKING_TO_BED);
        clearDoorTracking(npc.npcId());

        if (engineNavigationEnabled) {
            engineNavigation.setTarget(npc.entityRef(), bedPos);
        }

        System.out.println("[KeystoneNPC] Navigation start: npc=" + npc.npcName()
            + " start=" + startPos
            + " target=" + bedPos
            + " targetType=BED");
        return true;
    }

    public boolean startNavigationToWork(NpcRecord npc) {
        Optional<MarkerRecord> workMarker = markerResolver.resolveRequiredMarkerWithFallback(npc, MarkerType.WORK);
        if (workMarker.isEmpty()) {
            System.err.println("[KeystoneNPC] Missing work marker for NPC '" + npc.npcName() + "' (" + npc.npcId() + ")"
                + " markerId=" + npc.workMarkerId());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return false;
        }

        Vec3 workPos = workMarker.get().position();
        Vec3 startPos = pathfindingSupport.resolveNavigationStartPosition(npc, workPos);
        long durationMs = NpcNavigation.calculateDurationMs(startPos, workPos);

        npc.navigationState().startNavigation(startPos, workPos, durationMs, NpcState.WORKING);
        npc.state(NpcState.WALKING_TO_WORK);
        clearDoorTracking(npc.npcId());

        if (engineNavigationEnabled) {
            engineNavigation.setTarget(npc.entityRef(), workPos);
        }

        System.out.println("[KeystoneNPC] Navigation start: npc=" + npc.npcName()
            + " start=" + startPos
            + " target=" + workPos
            + " targetType=WORK");
        return true;
    }

    public NpcState resolveDesiredTargetState(World world, NpcRecord npc, RoleDefinition roleDefinition) {
        try {
            WorldTimeResource worldTimeResource = world.getEntityStore().getStore()
                .getResource(WorldTimeResource.getResourceType());
            int currentHour = worldTimeResource.getCurrentHour();
            return roleDefinition.schedule().isSleepingHour(currentHour) ? NpcState.SLEEPING : NpcState.WORKING;
        } catch (Exception e) {
            System.err.println("[KeystoneNPC] Error getting world time for NPC '" + npc.npcName() + "': " + e.getMessage());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return null;
        }
    }

    public boolean hasRequiredMarkers(NpcRecord npc, RoleDefinition roleDefinition) {
        for (MarkerType markerType : roleDefinition.requiredMarkers()) {
            if (markerResolver.resolveRequiredMarkerWithFallback(npc, markerType).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String missingRequiredMarkers(NpcRecord npc, RoleDefinition roleDefinition) {
        List<String> missing = new java.util.ArrayList<>();
        for (MarkerType markerType : roleDefinition.requiredMarkers()) {
            if (markerResolver.resolveRequiredMarkerWithFallback(npc, markerType).isEmpty()) {
                missing.add(markerType.name().toLowerCase(Locale.ROOT));
            }
        }
        return String.join(",", missing);
    }

    private void clearDoorTracking(String npcId) {
        activeDoorPasses.remove(npcId);
        pendingDoorAttempts.remove(npcId);
        pendingDoorCloseAttempts.remove(npcId);
    }
}
