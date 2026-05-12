package keystone.npc.routine;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import keystone.npc.capabilities.CapabilityChecks;
import keystone.npc.capabilities.NpcCapability;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcEntityStatus;
import keystone.npc.domain.NpcState;
import keystone.npc.domain.TargetRole;
import keystone.npc.doorway.ActiveDoorPass;
import keystone.npc.doorway.DoorPassTracker;
import keystone.npc.doorway.DoorwayConfig;
import keystone.npc.doorway.DoorwayFlow;
import keystone.npc.doorway.DoorwayScanner;
import keystone.npc.doorway.PendingDoorAttempt;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.navigation.EngineNavigationController;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.recovery.RespawnRecoveryService;
import keystone.npc.recovery.RespawnPolicyConfig;
import keystone.npc.relink.RelinkSupport;
import keystone.npc.relink.RelinkWorkflowService;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.entity.EntitySyncService;
import keystone.npc.routine.marker.IdleMarkerService;
import keystone.npc.routine.marker.MarkerResolver;
import keystone.npc.routine.pathfinding.NavigationRuntimeService;
import keystone.npc.routine.pathfinding.PathfindingSupport;
import keystone.npc.routine.state.NpcTickPipeline;
import keystone.npc.routine.state.StateTargetingService;

/**
 * MVP A: minimaler Routine-Runner.
 * - verwaltet 1..N NPCs (MVP A: 1)
 * - tickt periodisch
 * - wählt Ziel (work vs bed) anhand Tageszeit
 * - zwingt Routing via door_marker
 */
public final class NpcRoutineRunner {
    private static final long RESPAWN_RETRY_BASE_MS = 500L;
    private static final long RESPAWN_RETRY_MAX_MS = 10_000L;
    private static final int RESPAWN_MAX_FAILURES = 12;
    private static final long IDLE_SYNC_GRACE_AFTER_RESTORE_MS = 2_000L;
    private static final double IDLE_POSITION_EPSILON_SQ = 0.01;
    private static final double ROLEID_DEDUPE_RADIUS = 3.5;
    private static final double ROLEID_DEDUPE_RADIUS_SQ = ROLEID_DEDUPE_RADIUS * ROLEID_DEDUPE_RADIUS;
    private static final double ROLEID_ANCHOR_RELINK_RADIUS = 2.5;
    private static final double ROLEID_ANCHOR_RELINK_RADIUS_SQ = ROLEID_ANCHOR_RELINK_RADIUS * ROLEID_ANCHOR_RELINK_RADIUS;
    private static final boolean ENGINE_NAVIGATION_ENABLED = true;
    private static final double ENGINE_NAVIGATION_ARRIVAL_DISTANCE = 0.6;
    private static final double ENGINE_NAVIGATION_ARRIVAL_DISTANCE_SQ = ENGINE_NAVIGATION_ARRIVAL_DISTANCE * ENGINE_NAVIGATION_ARRIVAL_DISTANCE;
    private static final double DOOR_TRIGGER_DISTANCE = 1.00;
    private static final double DOOR_TRIGGER_DISTANCE_SQ = DOOR_TRIGGER_DISTANCE * DOOR_TRIGGER_DISTANCE;
    private static final double DOOR_ROUTE_MAX_DISTANCE = 2.0;
    private static final double DOOR_ROUTE_MAX_DISTANCE_SQ = DOOR_ROUTE_MAX_DISTANCE * DOOR_ROUTE_MAX_DISTANCE;
    private static final long DOOR_ACTION_COOLDOWN_MS = 1_500L;
    private static final long DOOR_CHAIN_TIMEOUT_MS = 500L;
    private static final int CLEANUP_ORPHAN_MAX_RADIUS_BLOCKS = 256;
    private static final String OPEN_DOOR_IN = "OpenDoorIn";
    private static final String OPEN_DOOR_OUT = "OpenDoorOut";
    private static final String CLOSE_DOOR_IN = "CloseDoorIn";
    private static final String CLOSE_DOOR_OUT = "CloseDoorOut";
    private static final double DOOR_CLOSE_MIN_DISTANCE = 1.5;
    private static final double DOOR_CLOSE_MIN_DISTANCE_SQ = DOOR_CLOSE_MIN_DISTANCE * DOOR_CLOSE_MIN_DISTANCE;
    private static final double DOOR_DIRECTION_DOT_EPSILON = 0.05;
    private static final double DOOR_LOCAL_SEARCH_DISTANCE = 3.0;
    private static final double DOOR_LOCAL_SEARCH_DISTANCE_SQ = DOOR_LOCAL_SEARCH_DISTANCE * DOOR_LOCAL_SEARCH_DISTANCE;
    private static final int DOOR_LOCAL_SEARCH_RADIUS_BLOCKS = 3;

    private final MarkerRegistry markerRegistry;
    private final RoleDefinitionRegistry roleDefinitions;
    private final CapabilityChecks capabilityChecks;
    private final NpcTemplateResolver templateResolver;
    private final RequiredMarkerResolver requiredMarkerResolver;
    private final MarkerResolver markerResolver;
    private final EntitySyncService entitySync = new EntitySyncService();
    private final IdleMarkerService idleMarkerService;
    private final RelinkSupport relinkSupport = new RelinkSupport(entitySync);
    private final RelinkWorkflowService relinkWorkflowService;
    private final RespawnRecoveryService respawnRecoveryService;
    private final EngineNavigationController engineNavigation = new EngineNavigationController();
    private final PathfindingSupport pathfindingSupport = new PathfindingSupport(engineNavigation);
    private final NavigationRuntimeService navigationRuntimeService;
    private final StateTargetingService stateTargetingService;
    private final NpcTickPipeline npcUpdateWorkflowService;
    private final RoutineRunner routineRunner = new RoutineRunner();
    private final RespawnPolicyConfig respawnPolicyConfig = RespawnPolicyConfig.loadFromSystemProperties();
    private final DoorwayScanner doorSupport = new DoorwayScanner(
        pathfindingSupport,
        new DoorwayConfig(
            DOOR_ROUTE_MAX_DISTANCE_SQ,
            DOOR_DIRECTION_DOT_EPSILON,
            DOOR_LOCAL_SEARCH_DISTANCE_SQ,
            DOOR_LOCAL_SEARCH_RADIUS_BLOCKS,
            OPEN_DOOR_IN,
            OPEN_DOOR_OUT,
            CLOSE_DOOR_IN,
            CLOSE_DOOR_OUT
        )
    );
    private final Map<String, NpcRecord> npcs = new ConcurrentHashMap<>();
    private final Set<String> spawnRequestsInFlight = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> respawnRetryAtMs = new ConcurrentHashMap<>();
    private final Map<String, Integer> respawnFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> uuidRelinkMissCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> uuidRelinkFirstMissAtMs = new ConcurrentHashMap<>();
    private final Map<String, Long> nextDoorActionAtMs = new ConcurrentHashMap<>();
    private final Map<String, Long> nextDoorCloseActionAtMs = new ConcurrentHashMap<>();
    private final Map<String, Long> nextDoorMarkerSkipLogAtMs = new ConcurrentHashMap<>();
    private final Map<String, PendingDoorAttempt> pendingDoorAttempts = new ConcurrentHashMap<>();
    private final Map<String, PendingDoorAttempt> pendingDoorCloseAttempts = new ConcurrentHashMap<>();
    private final Map<String, Deque<ActiveDoorPass>> activeDoorPasses = new ConcurrentHashMap<>();
    private final DoorPassTracker doorPassTracker = new DoorPassTracker(activeDoorPasses);
    private final DoorwayFlow doorWorkflowService;
    private volatile long lastRestoreAtMs = 0L;

    public NpcRoutineRunner(
        MarkerRegistry markerRegistry,
        RoleDefinitionRegistry roleDefinitions,
        CapabilityChecks capabilityChecks,
        NpcTemplateResolver templateResolver
    ) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.capabilityChecks = Objects.requireNonNull(capabilityChecks);
        this.templateResolver = Objects.requireNonNull(templateResolver);
        this.requiredMarkerResolver = new RequiredMarkerResolver(this.templateResolver, this.roleDefinitions);
        this.markerResolver = new MarkerResolver(this.markerRegistry, this::logInfo);
        this.idleMarkerService = new IdleMarkerService(
            this.markerResolver,
            this.entitySync,
            ENGINE_NAVIGATION_ARRIVAL_DISTANCE_SQ,
            IDLE_POSITION_EPSILON_SQ,
            ROLEID_ANCHOR_RELINK_RADIUS_SQ,
            this::logInfo
        );
        this.relinkWorkflowService = new RelinkWorkflowService(
            this.roleDefinitions,
            this.markerResolver,
            this.entitySync,
            this.idleMarkerService,
            this.relinkSupport,
            this.uuidRelinkMissCounts,
            this.uuidRelinkFirstMissAtMs,
            this.respawnPolicyConfig.relinkRetryCount(),
            this.respawnPolicyConfig.relinkRetryDelayMs(),
            ROLEID_DEDUPE_RADIUS,
            ROLEID_DEDUPE_RADIUS_SQ,
            ROLEID_ANCHOR_RELINK_RADIUS,
            ROLEID_ANCHOR_RELINK_RADIUS_SQ,
            this::logInfo,
            this::logSevere,
            this::spawnContext
        );
        this.respawnRecoveryService = new RespawnRecoveryService(
            this.npcs,
            this.spawnRequestsInFlight,
            this.respawnRetryAtMs,
            this.respawnFailureCounts,
            this.uuidRelinkMissCounts,
            this.uuidRelinkFirstMissAtMs,
            this.roleDefinitions,
            this.markerResolver,
            RESPAWN_RETRY_BASE_MS,
            RESPAWN_RETRY_MAX_MS,
            RESPAWN_MAX_FAILURES,
            this.requiredMarkerResolver,
            this::logSevere,
            this::spawnContext
        );
        this.doorWorkflowService = new DoorwayFlow(
            this.markerResolver,
            this.doorSupport,
            this.doorPassTracker,
            this.requiredMarkerResolver,
            this.nextDoorActionAtMs,
            this.nextDoorCloseActionAtMs,
            this.nextDoorMarkerSkipLogAtMs,
            this.pendingDoorAttempts,
            this.pendingDoorCloseAttempts,
            DOOR_TRIGGER_DISTANCE_SQ,
            DOOR_ROUTE_MAX_DISTANCE_SQ,
            DOOR_ACTION_COOLDOWN_MS,
            DOOR_CHAIN_TIMEOUT_MS,
            DOOR_CLOSE_MIN_DISTANCE_SQ,
            this::logDoorInfo,
            this::canOpenDoorsWithDebug
        );
        this.navigationRuntimeService = new NavigationRuntimeService(
            this.engineNavigation,
            this.pathfindingSupport,
            this.doorWorkflowService,
            ENGINE_NAVIGATION_ARRIVAL_DISTANCE_SQ,
            this.pendingDoorAttempts
        );
        this.stateTargetingService = new StateTargetingService(
            this.markerResolver,
            this.pathfindingSupport,
            this.engineNavigation,
            this.activeDoorPasses,
            this.pendingDoorAttempts,
            this.pendingDoorCloseAttempts,
            this.templateResolver,
            this.requiredMarkerResolver,
            this.routineRunner,
            ENGINE_NAVIGATION_ENABLED,
            this::isRoutineLoggingEnabled,
            this::emitRoutineChatMessage
        );
        this.npcUpdateWorkflowService = new NpcTickPipeline(
            this.roleDefinitions,
            this.stateTargetingService,
            this.navigationRuntimeService,
            this.idleMarkerService,
            this.entitySync,
            ENGINE_NAVIGATION_ENABLED,
            this::emitMissingMarkerWarnings
        );
    }

    public void restore(List<NpcRecord> loaded) {
        npcs.clear();
        spawnRequestsInFlight.clear();
        respawnRetryAtMs.clear();
        respawnFailureCounts.clear();
        uuidRelinkMissCounts.clear();
        uuidRelinkFirstMissAtMs.clear();
        nextDoorActionAtMs.clear();
        nextDoorCloseActionAtMs.clear();
        nextDoorMarkerSkipLogAtMs.clear();
        pendingDoorAttempts.clear();
        pendingDoorCloseAttempts.clear();
        activeDoorPasses.clear();
        lastRestoreAtMs = System.currentTimeMillis();

        int restoredDisabled = 0;
        int restoredNeedsRelink = 0;
        int restoredMissingEntity = 0;
        int keptInvalid = 0;
        for (var npc : loaded) {
            reconcilePersistedMarkerAssignments(npc);

            String staleReason = staleReasonForRestore(npc);
            if (staleReason != null) {
                keptInvalid++;
                npc.entityStatus(NpcEntityStatus.DISABLED);
                npc.lastValidationWarningKey("restore-invalid:" + staleReason);
                logSevere("RESTORE_RECORD_KEPT_INVALID", "Keeping invalid persisted NPC record for manual recovery: "
                    + spawnContext(npc, "restore", null, null, null)
                    + " reason=" + staleReason);
                logSevere("RESTORE_RECORD_DISABLED", "Restored NPC record as DISABLED due to invalid restore state: "
                    + spawnContext(npc, "restore", null, null, null)
                    + " reason=" + staleReason);
            }

            // Entity references are runtime-only and always invalid after restart.
            npc.entityRef(null);
            npc.entityId(0);
            if (npc.entityStatus() != NpcEntityStatus.DISABLED) {
                if (npc.entityUuid() == null || npc.entityUuid().isBlank()) {
                    npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
                    restoredMissingEntity++;
                    logInfo("RESTORE_RECORD_MISSING_ENTITY", "Restored NPC record as MISSING_ENTITY: "
                        + spawnContext(npc, "restore", null, null, null));
                } else {
                    npc.entityStatus(NpcEntityStatus.NEEDS_RELINK);
                    restoredNeedsRelink++;
                    logInfo("RESTORE_RECORD_NEEDS_RELINK", "Restored NPC record as NEEDS_RELINK: "
                        + spawnContext(npc, "restore", null, null, null));
                }
            } else {
                restoredDisabled++;
            }
            normalizeRestorePosition(npc);

            NavigationTarget navigationState = npc.navigationState();
            logInfo("RESTORE_NAV_DEBUG", "Restore snapshot: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " state=" + npc.state().name()
                + " currentPos=" + formatPosition(npc.currentPosition())
                + " targetPos=" + formatPosition(navigationState.getTargetPosition())
                + " targetState=" + (navigationState.getTargetState() == null ? "-" : navigationState.getTargetState().name())
                + " remainingMs=" + navigationState.getRemainingTimeMs());

            npcs.put(npc.npcId(), npc);
        }

        logInfo("RESTORE_STATUS_SUMMARY", "Restore status summary: disabled=" + restoredDisabled
            + " needsRelink=" + restoredNeedsRelink
            + " missingEntity=" + restoredMissingEntity
            + " keptInvalid=" + keptInvalid);
    }

    public List<NpcRecord> snapshot() {
        return npcs.values().stream().toList();
    }

    private void reconcilePersistedMarkerAssignments(NpcRecord npc) {
        List<MarkerType> requiredTypes = requiredMarkerResolver.resolveSupportedRequiredMarkerTypes(npc.roleId());
        Set<MarkerType> requiredSet = Set.copyOf(requiredTypes);

        for (MarkerType markerType : MarkerType.values()) {
            if (requiredSet.contains(markerType)) {
                continue;
            }

            if (markerResolver.markerIdForType(npc, markerType) != null) {
                markerResolver.setMarkerIdForType(npc, markerType, null);
            }
        }

        for (MarkerType markerType : requiredTypes) {
            markerResolver.resolveRequiredMarkerWithFallback(npc, markerType);
        }
    }

    private void bindActiveMarkersByRole(NpcRecord npc) {
        Set<MarkerType> requiredSet = Set.copyOf(requiredMarkerResolver.resolveSupportedRequiredMarkerTypes(npc.roleId()));
        for (MarkerType markerType : MarkerType.values()) {
            String markerId = requiredSet.contains(markerType)
                ? markerRegistry.getActive(markerType).map(MarkerRecord::markerId).orElse(null)
                : null;
            markerResolver.setMarkerIdForType(npc, markerType, markerId);
        }
    }

    public List<NpcRecord> snapshotIndexed() {
        List<NpcRecord> npcList = new ArrayList<>(npcs.values());
        npcList.sort((left, right) -> {
            int byName = safeLower(left.npcName()).compareTo(safeLower(right.npcName()));
            if (byName != 0) {
                return byName;
            }

            int byRole = safeLower(left.roleId()).compareTo(safeLower(right.roleId()));
            if (byRole != 0) {
                return byRole;
            }

            return safeLower(left.npcId()).compareTo(safeLower(right.npcId()));
        });
        return npcList;
    }

    public NpcRecord getNpcByIndex(int index) {
        List<NpcRecord> npcList = snapshotIndexed();
        if (index < 0 || index >= npcList.size()) {
            return null;
        }
        return npcList.get(index);
    }

    public int indexOfNpc(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return -1;
        }

        List<NpcRecord> npcList = snapshotIndexed();
        for (int i = 0; i < npcList.size(); i++) {
            if (npcId.equalsIgnoreCase(npcList.get(i).npcId())) {
                return i;
            }
        }
        return -1;
    }

    public Optional<NpcRecord> findNpcByNameOrId(String query) {
        List<NpcRecord> matches = findNpcMatchesByNameOrId(query);
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        }
        return Optional.empty();
    }

    public List<NpcRecord> findNpcMatchesByNameOrId(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        List<NpcRecord> exact = new ArrayList<>();
        List<NpcRecord> partial = new ArrayList<>();

        for (NpcRecord npc : snapshotIndexed()) {
            String name = safeLower(npc.npcName());
            String id = safeLower(npc.npcId());

            if (name.equals(normalized) || id.equals(normalized)) {
                exact.add(npc);
                continue;
            }

            if (name.contains(normalized) || id.contains(normalized)) {
                partial.add(npc);
            }
        }

        if (!exact.isEmpty()) {
            return exact;
        }
        return partial;
    }

    public NpcRecord getNpc(String npcId) {
        return npcs.get(npcId);
    }

    public boolean assignMarkerToNpc(NpcRecord npc, MarkerType markerType, String markerId) {
        if (npc == null || markerType == null || markerId == null || markerId.isBlank()) {
            return false;
        }

        if (!isMarkerAllowedForRole(npc.roleId(), markerType)) {
            logSevere("MARKER_ASSIGN_BLOCKED_INVALID_FOR_ROLE", "Skipped marker assignment because marker is not valid for role: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " roleId=" + npc.roleId()
                + " markerType=" + markerType.name());
            return false;
        }

        String previousMarkerId = markerResolver.markerIdForType(npc, markerType);
        MarkerRecord previousMarker = markerResolver.resolveMarkerInNpcWorld(npc, markerType, previousMarkerId).orElse(null);

        markerResolver.setMarkerIdForType(npc, markerType, markerId);

        if (!shouldStartRetargetWalkFromCurrentMarker(npc, markerType, previousMarker)) {
            return false;
        }

        NpcState targetState = idleStateForMarker(markerType);
        if (targetState == null) {
            return false;
        }

        resetNavigationForRetarget(npc);
        boolean started = stateTargetingService.startNavigationToMarker(npc, markerType, targetState);
        if (started) {
            logInfo("MARKER_RETARGET_REROUTE", "Started immediate reroute after marker retarget: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerType=" + markerType.name()
                + " oldMarkerId=" + nullToDash(previousMarkerId)
                + " newMarkerId=" + markerId);
        }
        return started;
    }

    public boolean removeNpc(String npcId) {
        NpcRecord npc = npcs.remove(npcId);
        if (npc == null) {
            return false;
        }

        spawnRequestsInFlight.remove(npc.npcId());
        clearRespawnFailureState(npc.npcId());
        nextDoorActionAtMs.remove(npc.npcId());
        nextDoorCloseActionAtMs.remove(npc.npcId());
        pendingDoorAttempts.remove(npc.npcId());
        pendingDoorCloseAttempts.remove(npc.npcId());
        activeDoorPasses.remove(npc.npcId());
        removeLiveEntity(npc);
        return true;
    }

    public boolean removeNpcByIndex(int index) {
        var npcList = snapshotIndexed();
        if (index < 0 || index >= npcList.size()) {
            return false;
        }
        NpcRecord npc = npcList.get(index);
        return removeNpc(npc.npcId());
    }

    public int clearNpcs() {
        List<String> npcIds = new ArrayList<>(npcs.keySet());
        for (String npcId : npcIds) {
            removeNpc(npcId);
        }
        return npcIds.size();
    }

    public record RespawnMissingResult(
        int checked,
        int relinked,
        int respawned,
        int skipped,
        int ambiguous,
        int forceRequired,
        int wouldSpawn,
        int wouldRelink,
        int wouldSkip,
        int skippedNotMissing,
        int failed,
        boolean stateChanged
    ) {
    }

    public RespawnMissingResult respawnMissingNpcsInWorld(World world, boolean forceSpawn, String trigger) {
        return respawnMissingNpcsInWorld(world, forceSpawn, false, trigger);
    }

    public RespawnMissingResult respawnMissingNpcsInWorld(World world, boolean forceSpawn, boolean dryRun, String trigger) {
        if (world == null) {
            return new RespawnMissingResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        }

        int checked = 0;
        int relinked = 0;
        int respawned = 0;
        int skipped = 0;
        int ambiguous = 0;
        int forceRequired = 0;
        int wouldSpawn = 0;
        int wouldRelink = 0;
        int wouldSkip = 0;
        int skippedNotMissing = 0;
        int failed = 0;
        boolean stateChanged = false;
        Map<String, String> plannedClaimedEntityUuids = new HashMap<>();
        List<RelinkWorkflowService.OwnedEntityRefClaim> plannedClaimedEntityRefs = new ArrayList<>();

        List<NpcRecord> snapshot = new ArrayList<>(npcs.values());
        for (NpcRecord npc : snapshot) {
            if (!world.getName().equals(npc.worldId().value())) {
                continue;
            }

            NpcEntityStatus status = npc.entityStatus();
            if (status != NpcEntityStatus.MISSING_ENTITY || status == NpcEntityStatus.DISABLED) {
                if (dryRun) {
                    checked++;
                    skipped++;
                    wouldSkip++;
                    skippedNotMissing++;
                    logInfo("RESPAWN_DRY_RUN_NOT_MISSING", "Dry-run skipping NPC because status is not missing: "
                        + spawnContext(npc, trigger, world, null, null)
                        + " status=" + status);
                    logInfo("RESPAWN_DRY_RUN_WOULD_SKIP", "Dry-run no action for NPC with non-missing status: "
                        + spawnContext(npc, trigger, world, null, null)
                        + " status=" + status);
                }
                continue;
            }

            checked++;

            OwnershipSnapshot ownership = buildOwnershipSnapshot(world.getName());

            RelinkWorkflowService.RelinkEvaluationOutcome relinkEvaluation = dryRun
                ? evaluateRelinkEntityRefDetailed(world, npc, ownership)
                : null;
            RelinkWorkflowService.RelinkOutcome relinkOutcome = dryRun
                ? relinkEvaluation.outcome()
                : tryRelinkEntityRef(world, npc, trigger, ownership);
            if (relinkOutcome == RelinkWorkflowService.RelinkOutcome.SUCCESS) {
                if (dryRun) {
                    if (relinkEvaluation == null) {
                        skipped++;
                        wouldSkip++;
                        ambiguous++;
                        logSevere("RESPAWN_DRY_RUN_AMBIGUOUS", "Dry-run relink evaluation missing candidate details: "
                            + spawnContext(npc, trigger, world, null, null));
                        continue;
                    }

                    if (!registerDryRunPlannedClaim(
                        plannedClaimedEntityUuids,
                        plannedClaimedEntityRefs,
                        relinkEvaluation.candidateRef(),
                        relinkEvaluation.candidateUuid(),
                        npc.npcId()
                    )) {
                        skipped++;
                        wouldSkip++;
                        ambiguous++;
                        logSevere("RESPAWN_DRY_RUN_SKIPPED_PLANNED_CLAIMED", "Dry-run relink candidate already planned for another record: "
                            + spawnContext(npc, trigger, world, null, null));
                        continue;
                    }

                    skipped++;
                    wouldSkip++;
                    wouldRelink++;
                    logInfo("RESPAWN_DRY_RUN_WOULD_RELINK", "Dry-run would relink missing NPC to existing entity: "
                        + spawnContext(npc, trigger, world, null, null));
                } else {
                    logInfo("RESPAWN_MATCH_FOUND_EXISTING_ENTITY", "Found existing entity before replacement spawn: "
                        + spawnContext(npc, trigger, world, null, null));
                    relinked++;
                    stateChanged = true;
                    clearRespawnFailureState(npc.npcId());
                }
                continue;
            }

            RelinkWorkflowService.AnchorRelinkEvaluationOutcome anchorEvaluation = dryRun
                ? evaluateAnchorRelinkEntityRefDetailed(world, npc, ownership)
                : null;
            RelinkWorkflowService.AnchorRelinkOutcome anchorOutcome = dryRun
                ? anchorEvaluation.outcome()
                : tryAnchorRelinkEntityRef(world, npc, trigger, ownership);
            if (anchorOutcome == RelinkWorkflowService.AnchorRelinkOutcome.SUCCESS) {
                if (dryRun) {
                    if (anchorEvaluation == null) {
                        skipped++;
                        wouldSkip++;
                        ambiguous++;
                        logSevere("RESPAWN_DRY_RUN_AMBIGUOUS", "Dry-run anchor evaluation missing candidate details: "
                            + spawnContext(npc, trigger, world, null, null));
                        continue;
                    }

                    if (!registerDryRunPlannedClaim(
                        plannedClaimedEntityUuids,
                        plannedClaimedEntityRefs,
                        anchorEvaluation.candidateRef(),
                        anchorEvaluation.candidateUuid(),
                        npc.npcId()
                    )) {
                        skipped++;
                        wouldSkip++;
                        ambiguous++;
                        logSevere("RESPAWN_DRY_RUN_SKIPPED_PLANNED_CLAIMED", "Dry-run anchor relink candidate already planned for another record: "
                            + spawnContext(npc, trigger, world, null, null));
                        continue;
                    }

                    skipped++;
                    wouldSkip++;
                    wouldRelink++;
                    logInfo("RESPAWN_DRY_RUN_WOULD_RELINK", "Dry-run would relink missing NPC via anchor match: "
                        + spawnContext(npc, trigger, world, null, null));
                } else {
                    logInfo("RESPAWN_MATCH_FOUND_EXISTING_ENTITY", "Found existing entity before replacement spawn: "
                        + spawnContext(npc, trigger, world, null, null));
                    relinked++;
                    stateChanged = true;
                    clearRespawnFailureState(npc.npcId());
                }
                continue;
            }
            if (anchorOutcome == RelinkWorkflowService.AnchorRelinkOutcome.AMBIGUOUS) {
                skipped++;
                ambiguous++;
                if (dryRun) {
                    wouldSkip++;
                    logSevere("RESPAWN_DRY_RUN_AMBIGUOUS", "Dry-run blocked by ambiguous anchor relink candidates: "
                        + spawnContext(npc, trigger, world, null, null));
                } else {
                    logSevere("RESPAWN_RELINK_AMBIGUOUS", "Skipped replacement spawn due to ambiguous anchor relink candidates: "
                        + spawnContext(npc, trigger, world, null, null));
                }
                continue;
            }

            if (dryRun) {
                skipped++;
                wouldSkip++;
                wouldSpawn++;
                forceRequired++;
                logInfo("RESPAWN_DRY_RUN_WOULD_SPAWN", "Dry-run would create replacement entity for missing NPC: "
                    + spawnContext(npc, trigger, world, null, null));
                logInfo("RESPAWN_DRY_RUN_FORCE_REQUIRED", "Dry-run indicates replacement spawn requires --force: "
                    + spawnContext(npc, trigger, world, null, null));
                continue;
            }

            boolean allowRespawn = forceSpawn;
            if (!allowRespawn) {
                skipped++;
                forceRequired++;
                logInfo("RESPAWN_FORCE_REQUIRED", "No safe relink target found; replacement spawn requires --force: "
                    + spawnContext(npc, trigger, world, null, null));
                continue;
            }

            if (!passesRespawnForcePrecheck(world, npc, trigger)) {
                skipped++;
                failed++;
                logSevere("RESPAWN_FORCE_PRECHECK_FAILED", "Skipped replacement spawn because final precheck failed: "
                    + spawnContext(npc, trigger, world, null, null));
                continue;
            }

            if (spawnNpcEntity(world, npc, trigger)) {
                logInfo("RESPAWN_CREATED_REPLACEMENT", "Created replacement entity for missing NPC: "
                    + spawnContext(npc, trigger, world, null, null));
                respawned++;
                stateChanged = true;
                clearRespawnFailureState(npc.npcId());
            } else {
                skipped++;
                failed++;
            }
        }

        return new RespawnMissingResult(
            checked,
            relinked,
            respawned,
            skipped,
            ambiguous,
            forceRequired,
            wouldSpawn,
            wouldRelink,
            wouldSkip,
            skippedNotMissing,
            failed,
            stateChanged
        );
    }

    public record CleanupOrphansResult(
        int found,
        int removed,
        boolean blocked,
        int openRelinkRecords,
        int wouldRemove,
        boolean dryRun
    ) {
    }

    public CleanupOrphansResult cleanupOrphans(World world, Vec3 center, int radiusBlocks) {
        return cleanupOrphans(world, center, radiusBlocks, false, false);
    }

    public CleanupOrphansResult cleanupOrphans(World world, Vec3 center, int radiusBlocks, boolean dryRun, boolean force) {
        if (world == null || center == null || radiusBlocks <= 0) {
            return new CleanupOrphansResult(0, 0, false, 0, 0, dryRun);
        }

        int openRelinkRecords = countOpenRelinkRecords(world.getName());
        if (openRelinkRecords > 0 && !force) {
            logSevere("CLEANUP_BLOCKED_RELINK_PENDING", "Blocked orphan cleanup because unresolved relink states exist: world="
                + world.getName() + " openRelinkRecords=" + openRelinkRecords);
            return new CleanupOrphansResult(0, 0, true, openRelinkRecords, 0, dryRun);
        }

        int clampedRadius = Math.min(Math.max(radiusBlocks, 1), CLEANUP_ORPHAN_MAX_RADIUS_BLOCKS);
        double radius = clampedRadius;
        double radiusSq = radius * radius;
        Set<Integer> managedRoleIndices = new HashSet<>();
        for (RoleDefinition definition : roleDefinitions.list()) {
            int roleIndex = NPCPlugin.get().getIndex(definition.npcPluginRoleName());
            if (roleIndex >= 0) {
                managedRoleIndices.add(roleIndex);
            }
        }

        OwnershipSnapshot ownership = buildOwnershipSnapshot(world.getName());
        Set<String> knownEntityUuids = new HashSet<>(ownership.claimedEntityUuids().keySet());
        List<RelinkWorkflowService.OwnedEntityRefClaim> knownLiveRefs = ownership.claimedEntityRefs();

        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return new CleanupOrphansResult(0, 0, false, openRelinkRecords, 0, dryRun);
        }

        Store<EntityStore> store = world.getEntityStore().getStore();
        Query<EntityStore> npcQuery = Query.and(npcType, TransformComponent.getComponentType());
        final int[] found = new int[] {0};
        final int[] removed = new int[] {0};
        final int[] wouldRemove = new int[] {0};

        store.forEachChunk(npcQuery, (java.util.function.BiConsumer<com.hypixel.hytale.component.ArchetypeChunk<EntityStore>, com.hypixel.hytale.component.CommandBuffer<EntityStore>>) (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                NPCEntity liveNpc = archetypeChunk.getComponent(index, npcType);
                if (liveNpc == null) {
                    continue;
                }

                int roleIndex = liveNpc.getRoleIndex();
                int spawnRoleIndex = liveNpc.getSpawnRoleIndex();
                if (!managedRoleIndices.contains(roleIndex) && !managedRoleIndices.contains(spawnRoleIndex)) {
                    continue;
                }

                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }

                if (relinkSupport.distanceSq(center, transform.getPosition()) > radiusSq) {
                    continue;
                }

                Ref<EntityStore> candidateRef = archetypeChunk.getReferenceTo(index);
                if (candidateRef == null || !candidateRef.isValid()) {
                    continue;
                }

                if (isClaimedByLiveRef(candidateRef, knownLiveRefs)) {
                    continue;
                }

                UUIDComponent uuidComponent = candidateRef.getStore().getComponent(candidateRef, UUIDComponent.getComponentType());
                String entityUuid = uuidComponent == null || uuidComponent.getUuid() == null
                    ? null
                    : uuidComponent.getUuid().toString();

                if (entityUuid != null && knownEntityUuids.contains(entityUuid)) {
                    continue;
                }

                logInfo("CLEANUP_ORPHAN_FOUND", "Found orphan NPC entity in world cleanup: world=" + world.getName()
                    + " roleIndex=" + roleIndex
                    + " spawnRoleIndex=" + spawnRoleIndex
                    + " entityUuid=" + nullToDash(entityUuid));

                found[0]++;
                if (dryRun) {
                    wouldRemove[0]++;
                    logInfo("CLEANUP_ORPHAN_DRY_RUN", "Dry-run: orphan candidate would be removed: world=" + world.getName()
                        + " roleIndex=" + roleIndex
                        + " spawnRoleIndex=" + spawnRoleIndex
                        + " entityUuid=" + nullToDash(entityUuid));
                    continue;
                }

                candidateRef.getStore().removeEntity(candidateRef, RemoveReason.REMOVE);
                removed[0]++;
                logInfo("CLEANUP_ORPHAN_REMOVED", "Removed orphan NPC entity in world cleanup: world=" + world.getName()
                    + " roleIndex=" + roleIndex
                    + " spawnRoleIndex=" + spawnRoleIndex
                    + " entityUuid=" + nullToDash(entityUuid));
            }
        });

        return new CleanupOrphansResult(found[0], removed[0], false, openRelinkRecords, wouldRemove[0], dryRun);
    }

    public int spawnRestoredNpcs(String trigger) {
        int queued = 0;
        long now = System.currentTimeMillis();
        for (NpcRecord npc : npcs.values()) {
            if (npc.entityStatus() == NpcEntityStatus.DISABLED) {
                continue;
            }

            if (hasLiveEntity(npc)) {
                npc.entityId(1);
                npc.entityStatus(NpcEntityStatus.ACTIVE);
                updatePersistedEntityIdentity(npc, npc.entityRef());
                clearRespawnFailureState(npc.npcId());
                npc.lastValidationWarningKey(null);
                continue;
            }

            npc.entityRef(null);
            npc.entityId(0);

            World world = Universe.get().getWorld(npc.worldId().value());
            if (world != null) {
                OwnershipSnapshot ownership = buildOwnershipSnapshot(world.getName());
                RelinkWorkflowService.RelinkOutcome relinkOutcome = tryRelinkEntityRef(world, npc, trigger, ownership);
                if (relinkOutcome == RelinkWorkflowService.RelinkOutcome.SUCCESS) {
                    clearRespawnFailureState(npc.npcId());
                    npc.lastValidationWarningKey(null);
                    continue;
                }

                RelinkWorkflowService.AnchorRelinkOutcome anchorOutcome = tryAnchorRelinkEntityRef(world, npc, trigger, ownership);
                if (anchorOutcome == RelinkWorkflowService.AnchorRelinkOutcome.SUCCESS) {
                    clearRespawnFailureState(npc.npcId());
                    logInfo("RESPAWN_MATCH_FOUND_EXISTING_ENTITY", "Found existing entity during relink fallback: "
                        + spawnContext(npc, trigger, world, null, null));
                    npc.lastValidationWarningKey(null);
                    continue;
                }
                if (anchorOutcome == RelinkWorkflowService.AnchorRelinkOutcome.AMBIGUOUS) {
                    String warnKey = "anchor-relink-ambiguous:" + npc.entityUuid();
                    if (!warnKey.equals(npc.lastValidationWarningKey())) {
                        logSevere("RESPAWN_RELINK_AMBIGUOUS", "Skipped replacement spawn due to ambiguous anchor relink candidates: "
                            + spawnContext(npc, trigger, world, null, null));
                        npc.lastValidationWarningKey(warnKey);
                    }
                    continue;
                }

                if (npc.entityStatus() != NpcEntityStatus.MISSING_ENTITY) {
                    continue;
                }
            } else {
                if (npc.entityStatus() != NpcEntityStatus.MISSING_ENTITY) {
                    npc.entityStatus(NpcEntityStatus.NEEDS_RELINK);
                }
                continue;
            }

            if (!respawnPolicyConfig.enableAutoRespawnMissingNpc()) {
                String warnKey = "auto-respawn-disabled:" + npc.entityUuid();
                if (!warnKey.equals(npc.lastValidationWarningKey())) {
                    logSevere("AUTO_RESPAWN_DISABLED", "NPC entity missing. Use /knpc respawn-missing or enable autoRespawnMissingNpc: "
                        + spawnContext(npc, trigger, world, null, null));
                    npc.lastValidationWarningKey(warnKey);
                }
                continue;
            }

            Long retryAt = respawnRetryAtMs.get(npc.npcId());
            if (retryAt != null && now < retryAt) {
                continue;
            }

            if (!spawnRequestsInFlight.add(npc.npcId())) {
                continue;
            }

            queued++;
            world.execute(() -> {
                try {
                    if (spawnNpcEntity(world, npc, trigger + "-auto-respawn")) {
                        logInfo("RESPAWN_CREATED_REPLACEMENT", "Created replacement entity for missing NPC: "
                            + spawnContext(npc, trigger, world, null, null));
                        clearRespawnFailureState(npc.npcId());
                        npc.lastValidationWarningKey(null);
                    } else {
                        int failureCount = respawnFailureCounts.getOrDefault(npc.npcId(), 0) + 1;
                        respawnFailureCounts.put(npc.npcId(), failureCount);
                        long delayMs = Math.min(RESPAWN_RETRY_MAX_MS, RESPAWN_RETRY_BASE_MS << Math.min(5, failureCount - 1));
                        respawnRetryAtMs.put(npc.npcId(), System.currentTimeMillis() + delayMs);
                    }
                } catch (Exception ex) {
                    int failureCount = respawnFailureCounts.getOrDefault(npc.npcId(), 0) + 1;
                    respawnFailureCounts.put(npc.npcId(), failureCount);
                    long delayMs = Math.min(RESPAWN_RETRY_MAX_MS, RESPAWN_RETRY_BASE_MS << Math.min(5, failureCount - 1));
                    respawnRetryAtMs.put(npc.npcId(), System.currentTimeMillis() + delayMs);
                    logSevere("RESPAWN_EXCEPTION", "Exception while respawning NPC: "
                        + spawnContext(npc, trigger, world, null, null)
                        + " exception=" + ex.getClass().getSimpleName() + ":" + ex.getMessage());
                } finally {
                    spawnRequestsInFlight.remove(npc.npcId());
                }
            });
        }

        return queued;
    }

    public int spawnRestoredNpcs() {
        return spawnRestoredNpcs("manual");
    }

    public NpcRecord spawnNpc(String npcId, String name, String roleId, keystone.npc.markers.WorldId worldId, keystone.npc.markers.Vec3 position) {
        var npc = new NpcRecord(npcId, name, roleId, worldId);

        // Set spawn position
        npc.currentPosition(position);

        // Bind only role-required markers from current "active" markers.
        bindActiveMarkersByRole(npc);
        reconcilePersistedMarkerAssignments(npc);
        npc.entityStatus(NpcEntityStatus.NEEDS_RELINK);

        npcs.put(npc.npcId(), npc);
        logInfo("RECORD_CREATED", "Created NPC record: " + spawnContext(npc, "spawn-record-only", null, null, null));
        return npc;
    }

    public NpcRecord spawnNpcWithEntity(
        String npcId,
        String name,
        String roleId,
        keystone.npc.markers.WorldId worldId,
        keystone.npc.markers.Vec3 position,
        World world,
        String trigger
    ) {
        if (world == null) {
            return null;
        }

        Ref<EntityStore> createdEntityRef = null;
        boolean recordRegistered = false;
        var npc = new NpcRecord(npcId, name, roleId, worldId);

        try {
            npc.currentPosition(position);
            bindActiveMarkersByRole(npc);
            reconcilePersistedMarkerAssignments(npc);

            if (!spawnNpcEntity(world, npc, trigger)) {
                return null;
            }

            createdEntityRef = npc.entityRef();

            npcs.put(npc.npcId(), npc);
            recordRegistered = true;
            logInfo("RECORD_CREATED", "Created NPC record: " + spawnContext(npc, trigger, world, null, null));
            return npc;
        } catch (RuntimeException ex) {
            if (recordRegistered) {
                npcs.remove(npc.npcId());
            }
            rollbackSpawnedEntity(world, createdEntityRef != null ? createdEntityRef : npc.entityRef(), npc.npcId(), trigger, ex);
            return null;
        }
    }

    public NpcRecord linkEntityRef(String npcId, Ref<EntityStore> entityRef) {
        NpcRecord npc = npcs.get(npcId);
        if (npc != null) {
            npc.entityRef(entityRef);
            npc.entityId(1);  // Mark as spawned (persist to JSON)
            npc.entityStatus(NpcEntityStatus.ACTIVE);
            updatePersistedEntityIdentity(npc, entityRef);
        }
        return npc;
    }

    public boolean spawnEntityForNpc(World world, NpcRecord npc, String trigger) {
        if (world == null || npc == null) {
            return false;
        }

        return spawnNpcEntity(world, npc, trigger);
    }

    private boolean validateSpawnMarkerRequirements(
        World world,
        NpcRecord npc,
        String trigger,
        RoleDefinition roleDefinition,
        int roleIndex
    ) {
        List<String> invalidRoleReasons = roleDefinitions.invalidRoleReasons(npc.roleId());
        if (!invalidRoleReasons.isEmpty()) {
            logSevere("SPAWN_ABORT_INVALID_ROLE", "Role " + npc.roleId() + " is invalid: "
                + String.join(" | ", invalidRoleReasons)
                + " | " + spawnContext(npc, trigger, world, roleDefinition, roleIndex));
            return false;
        }

        if (templateResolver.resolveByRoleId(npc.roleId()).isEmpty()) {
            logSevere("SPAWN_ABORT_MISSING_ROLE_JSON", "No role JSON loaded for role " + npc.roleId()
                + ". Cannot resolve requiredMarkers. "
                + spawnContext(npc, trigger, world, roleDefinition, roleIndex));
            return false;
        }

        if (templateResolver.resolveRoutineByRoleId(npc.roleId()).isEmpty()) {
            logSevere("SPAWN_ABORT_MISSING_ROUTINE", "No routine loaded for role " + npc.roleId()
                + ". Spawn blocked. "
                + spawnContext(npc, trigger, world, roleDefinition, roleIndex));
            return false;
        }

        List<String> invalidRequiredMarkers = new ArrayList<>();
        List<String> missingRequiredMarkers = new ArrayList<>();
        List<RequiredMarkerResolver.Requirement> requirements = requiredMarkerResolver.resolveRequirements(npc.roleId());

        if (requirements.isEmpty()) {
            logSevere("SPAWN_ABORT_REQUIRED_MARKERS_UNRESOLVED", "No requiredMarkers resolved for role " + npc.roleId()
                + ". Spawn blocked. " + spawnContext(npc, trigger, world, roleDefinition, roleIndex));
            return false;
        }

        for (RequiredMarkerResolver.Requirement requirement : requirements) {
            MarkerType markerType = requirement.markerType();
            if (markerType == null) {
                invalidRequiredMarkers.add(requirement.name().toUpperCase(Locale.ROOT));
                continue;
            }

            if (markerResolver.resolveRequiredMarkerWithFallback(npc, markerType).isEmpty()) {
                missingRequiredMarkers.add(markerType.name());
            }
        }

        if (!invalidRequiredMarkers.isEmpty()) {
            for (String invalidRequiredMarker : invalidRequiredMarkers) {
                logSevere("SPAWN_ABORT_INVALID_REQUIRED_MARKER_TYPE", "Unknown marker type in requiredMarkers: "
                    + invalidRequiredMarker
                    + " | " + spawnContext(npc, trigger, world, roleDefinition, roleIndex));
            }
            return false;
        }

        if (!missingRequiredMarkers.isEmpty()) {
            logSevere("SPAWN_ABORT_MISSING_REQUIRED_MARKER", "Cannot spawn because required markers are missing: "
                + spawnContext(npc, trigger, world, roleDefinition, roleIndex)
                + " missing=" + String.join(",", missingRequiredMarkers));
            return false;
        }

        return true;
    }

    private boolean isMarkerAllowedForRole(String roleId, MarkerType markerType) {
        if (roleId == null || roleId.isBlank() || markerType == null) {
            return false;
        }

        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(roleId)) {
            if (markerType == requirement.markerType()) {
                return true;
            }
        }

        return false;
    }

    private boolean spawnNpcEntity(World world, NpcRecord npc, String trigger) {
        if (hasLiveEntity(npc)) {
            npc.entityId(1);
            npc.entityStatus(NpcEntityStatus.ACTIVE);
            return true;
        }

        reconcilePersistedMarkerAssignments(npc);

        SpawnIdentitySnapshot oldIdentity = captureSpawnIdentitySnapshot(npc);

        npc.entityRef(null);
        npc.entityId(0);
        if (npc.entityStatus() != NpcEntityStatus.DISABLED) {
            npc.entityStatus(NpcEntityStatus.NEEDS_RELINK);
        }

        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            logSevere("RESPAWN_UNKNOWN_ROLE", "Unknown role while restoring NPC: "
                + spawnContext(npc, trigger, world, null, null));
            return false;
        }

        RoleDefinition definition = roleDefinition.get();
        int roleIndex = NPCPlugin.get().getIndex(definition.npcPluginRoleName());

        if (roleIndex < 0) {
            logSevere("RESPAWN_ROLE_INDEX_MISSING", "Cannot spawn role " + npc.roleId() + ". "
                + "Resolved NPCPlugin role '" + definition.npcPluginRoleName() + "' was not found. "
                + spawnContext(npc, trigger, world, definition, roleIndex));
            return false;
        }

        if (!validateSpawnMarkerRequirements(world, npc, trigger, definition, roleIndex)) {
            return false;
        }

        Vec3 position = npc.currentPosition() != null ? npc.currentPosition() : new Vec3(0, 0, 0);
        Vector3d spawnPosition = new Vector3d(position.x(), position.y(), position.z());
        Store<EntityStore> store = world.getEntityStore().getStore();

        Ref<EntityStore> spawnedRef;
        try {
            var pair = NPCPlugin.get().spawnEntity(store, roleIndex, spawnPosition, Rotation3f.IDENTITY, null, null);
            if (pair == null) {
                restoreSpawnIdentitySnapshot(npc, oldIdentity, world, trigger, "spawn-entity-null");
                logSevere("RESPAWN_SPAWN_ENTITY_NULL", "spawnEntity returned null: "
                    + spawnContext(npc, trigger, world, definition, roleIndex));
                return false;
            }

            spawnedRef = pair.first();
            if (spawnedRef == null || !spawnedRef.isValid()) {
                restoreSpawnIdentitySnapshot(npc, oldIdentity, world, trigger, "spawn-entity-invalid-ref");
                logSevere("RESPAWN_INVALID_ENTITY_REF", "spawnEntity returned invalid ref: "
                    + spawnContext(npc, trigger, world, definition, roleIndex));
                return false;
            }
        } catch (RuntimeException ex) {
            restoreSpawnIdentitySnapshot(npc, oldIdentity, world, trigger, "spawn-entity-create-exception");
            logSevere("SPAWN_ENTITY_CREATE_FAILED", "spawnEntity threw exception; restored old identity: "
                + spawnContext(npc, trigger, world, definition, roleIndex)
                + " exception=" + ex.getClass().getSimpleName() + ":" + ex.getMessage());
            return false;
        }

        try {
            npc.entityRef(spawnedRef);
            npc.entityId(1);  // Mark as spawned (persist to JSON)
            npc.entityStatus(NpcEntityStatus.ACTIVE);
            updatePersistedEntityIdentity(npc, spawnedRef);
            logInfo("SPAWN_ENTITY_CREATED", "Spawned NPC entity: "
                + spawnContext(npc, trigger, world, definition, roleIndex));
            OwnershipSnapshot ownership = buildOwnershipSnapshot(world.getName());
            dedupeRoleIdDuplicates(world, npc, spawnedRef, definition, roleIndex, trigger, "post-spawn", ownership);
            if (respawnFailureCounts.getOrDefault(npc.npcId(), 0) > 0) {
                logInfo("RESPAWN_SUCCESS_AFTER_RETRY", "Restored NPC entity after retries: "
                    + spawnContext(npc, trigger, world, definition, roleIndex));
            }
            return true;
        } catch (RuntimeException ex) {
            rollbackSpawnedEntityAfterSpawnFailure(world, npc, spawnedRef, oldIdentity, trigger, ex);
            return false;
        }
    }

    private record SpawnIdentitySnapshot(
        String entityUuid,
        Ref<EntityStore> entityRef,
        long entityId,
        NpcEntityStatus entityStatus
    ) {
    }

    private SpawnIdentitySnapshot captureSpawnIdentitySnapshot(NpcRecord npc) {
        return new SpawnIdentitySnapshot(
            npc.entityUuid(),
            npc.entityRef(),
            npc.entityId(),
            npc.entityStatus()
        );
    }

    private void restoreSpawnIdentitySnapshot(
        NpcRecord npc,
        SpawnIdentitySnapshot oldIdentity,
        World world,
        String trigger,
        String reason
    ) {
        Ref<EntityStore> oldRef = oldIdentity.entityRef();
        boolean oldRefValid = oldRef != null && oldRef.isValid();
        Ref<EntityStore> restoredRef = oldRefValid ? oldRef : null;
        long restoredEntityId = oldRefValid ? Math.max(0L, oldIdentity.entityId()) : 0L;

        NpcEntityStatus restoredStatus = oldIdentity.entityStatus();
        if (restoredStatus == null) {
            restoredStatus = NpcEntityStatus.NEEDS_RELINK;
        }
        if (restoredStatus == NpcEntityStatus.ACTIVE && !oldRefValid) {
            restoredStatus = oldIdentity.entityUuid() == null || oldIdentity.entityUuid().isBlank()
                ? NpcEntityStatus.MISSING_ENTITY
                : NpcEntityStatus.NEEDS_RELINK;
        }
        if (restoredStatus == NpcEntityStatus.DISABLED) {
            restoredRef = null;
            restoredEntityId = 0L;
        }

        npc.entityUuid(oldIdentity.entityUuid());
        npc.entityRef(restoredRef);
        npc.entityId(restoredEntityId);
        npc.entityStatus(restoredStatus);

        logSevere("SPAWN_ENTITY_ROLLBACK_RESTORED_OLD_IDENTITY", "Restored old NPC identity after failed spawn path: "
            + spawnContext(npc, trigger, world, null, null)
            + " reason=" + reason
            + " oldStatus=" + oldIdentity.entityStatus()
            + " oldRefValid=" + oldRefValid
            + " oldEntityId=" + oldIdentity.entityId()
            + " oldEntityUuid=" + nullToDash(oldIdentity.entityUuid()));
    }

    private boolean passesRespawnForcePrecheck(World world, NpcRecord npc, String trigger) {
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            logSevere("RESPAWN_FORCE_PRECHECK_FAILED", "Unknown role while validating forced replacement spawn: "
                + spawnContext(npc, trigger, world, null, null));
            return false;
        }

        int roleIndex = NPCPlugin.get().getIndex(roleDefinition.get().npcPluginRoleName());
        if (roleIndex < 0) {
            logSevere("RESPAWN_FORCE_PRECHECK_FAILED", "Cannot spawn role " + npc.roleId() + ". "
                + "Resolved NPCPlugin role '" + roleDefinition.get().npcPluginRoleName() + "' was not found. "
                + spawnContext(npc, trigger, world, roleDefinition.get(), roleIndex));
            return false;
        }

        return true;
    }

    private void removeLiveEntity(NpcRecord npc) {
        Ref<EntityStore> liveRef = npc.entityRef();
        clearEntityIdentity(npc);

        if (liveRef == null || !liveRef.isValid()) {
            return;
        }

        World world = Universe.get().getWorld(npc.worldId().value());
        if (world == null) {
            System.err.println("[KeystoneNPC] Cannot remove entity for NPC '" + npc.npcName()
                + "' (" + npc.npcId() + "): world not found");
            return;
        }

        world.execute(() -> {
            if (liveRef != null && liveRef.isValid()) {
                liveRef.getStore().removeEntity(liveRef, RemoveReason.REMOVE);
            }
        });
    }

    private void rollbackSpawnedEntity(World world, Ref<EntityStore> entityRef, String npcId, String trigger, RuntimeException ex) {
        if (entityRef != null && entityRef.isValid()) {
            try {
                entityRef.getStore().removeEntity(entityRef, RemoveReason.REMOVE);
                logSevere("SPAWN_ROLLBACK_ORPHAN_PREVENTED", "Rolled back spawned entity to prevent orphan NPC: "
                    + "npcId=" + npcId
                    + " trigger=" + quote(trigger)
                    + " reason=" + ex.getClass().getSimpleName() + ":" + ex.getMessage());
                return;
            } catch (RuntimeException rollbackEx) {
                logSevere("SPAWN_ROLLBACK_FAILED", "Rollback failed after spawn exception: "
                    + "npcId=" + npcId
                    + " trigger=" + quote(trigger)
                    + " root=" + ex.getClass().getSimpleName() + ":" + ex.getMessage()
                    + " rollback=" + rollbackEx.getClass().getSimpleName() + ":" + rollbackEx.getMessage());
                throw rollbackEx;
            }
        }

        logSevere("SPAWN_ROLLBACK_ORPHAN_PREVENTED", "Spawn exception occurred before entity registration; no orphan persisted: "
            + "npcId=" + npcId
            + " trigger=" + quote(trigger)
            + " reason=" + ex.getClass().getSimpleName() + ":" + ex.getMessage());
    }

    private void rollbackSpawnedEntityAfterSpawnFailure(
        World world,
        NpcRecord npc,
        Ref<EntityStore> spawnedRef,
        SpawnIdentitySnapshot oldIdentity,
        String trigger,
        RuntimeException ex
    ) {
        boolean removed = false;
        boolean queued = false;

        if (spawnedRef != null && spawnedRef.isValid()) {
            try {
                spawnedRef.getStore().removeEntity(spawnedRef, RemoveReason.REMOVE);
                removed = true;
            } catch (RuntimeException removeEx) {
                if (world != null) {
                    try {
                        world.execute(() -> {
                            if (spawnedRef.isValid()) {
                                spawnedRef.getStore().removeEntity(spawnedRef, RemoveReason.REMOVE);
                            }
                        });
                        queued = true;
                    } catch (RuntimeException queueEx) {
                        logSevere("SPAWN_ENTITY_ROLLBACK_FAILED", "Failed to remove spawned entity after post-spawn exception: "
                            + spawnContext(npc, trigger, world, null, null)
                            + " root=" + ex.getClass().getSimpleName() + ":" + ex.getMessage()
                            + " remove=" + removeEx.getClass().getSimpleName() + ":" + removeEx.getMessage()
                            + " queue=" + queueEx.getClass().getSimpleName() + ":" + queueEx.getMessage());
                    }
                }
            }
        }

        restoreSpawnIdentitySnapshot(npc, oldIdentity, world, trigger, "post-spawn-step-exception");

        if (queued) {
            logSevere("SPAWN_ENTITY_ROLLBACK_REMOVE_QUEUED", "Entity removal queued after post-spawn exception: "
                + spawnContext(npc, trigger, world, null, null)
                + " reason=" + ex.getClass().getSimpleName() + ":" + ex.getMessage());
        }

        logSevere("SPAWN_ENTITY_ROLLBACK_ORPHAN_PREVENTED", "Post-spawn exception handled with rollback safeguards: "
            + spawnContext(npc, trigger, world, null, null)
            + " removed=" + removed
            + " queued=" + queued
            + " reason=" + ex.getClass().getSimpleName() + ":" + ex.getMessage());
    }

    /**
     * Hytale ECS tick entrypoint.
     * Runs once per world store tick through NpcTickSystem.
     */
    public void tickStore(Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();
        spawnRestoredNpcs("tick-retry");
        long now = System.currentTimeMillis();
        boolean startupIdleGuard = (now - lastRestoreAtMs) < IDLE_SYNC_GRACE_AFTER_RESTORE_MS;

        List<NpcRecord> npcSnapshot = new ArrayList<>(npcs.values());
        for (NpcRecord npc : npcSnapshot) {
            if (!world.getName().equals(npc.worldId().value())) {
                continue;
            }

            if (hasLiveEntity(npc) && !hasActiveNavigation(npc.navigationState())) {
                // Marker-authoritative idle states: never pull entity position back into NPC state.
                if (hasAuthoritativeIdleMarker(npc.state())) {
                    enforceAuthoritativeIdlePosition(npc, startupIdleGuard ? "startup-idle-guard" : "idle-marker-authority", true);
                } else if (!startupIdleGuard) {
                    // Non-idle states can still follow the live entity after startup settles.
                    updateNpcPositionFromEntity(npc, npc.entityRef());
                }
            }

            updateNpc(npc, world);
        }
    }

    /** MVP A Tick-Logik mit Ingame-Weltzeit. */
    public void tick() {
        spawnRestoredNpcs("tick-retry");

        List<NpcRecord> npcSnapshot = new ArrayList<>(npcs.values());
        for (NpcRecord npc : npcSnapshot) {
            World world = Universe.get().getWorld(npc.worldId().value());
            if (world != null) {
                world.execute(() -> updateNpc(npc, world));
            }
        }
    }

    private void updateNpc(NpcRecord npc, World world) {
        if (!passesLiveEntityGate(npc)) {
            return;
        }

        if (npc.state() == null || !npc.state().isWalking()) {
            npc.lastCapabilityDecisionKey(null);
        }
        reconcilePersistedMarkerAssignments(npc);
        npcUpdateWorkflowService.updateNpc(npc, world);
    }

    private boolean passesLiveEntityGate(NpcRecord npc) {
        if (npc.entityStatus() == NpcEntityStatus.DISABLED) {
            return false;
        }

        Ref<EntityStore> entityRef = npc.entityRef();
        if (entityRef != null && entityRef.isValid()) {
            return true;
        }

        npc.entityRef(null);
        npc.entityId(0);
        if (npc.entityUuid() == null || npc.entityUuid().isBlank()) {
            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
        } else {
            npc.entityStatus(NpcEntityStatus.NEEDS_RELINK);
        }

        clearRuntimeStateForMissingLiveEntity(npc);
        return false;
    }

    private void clearRuntimeStateForMissingLiveEntity(NpcRecord npc) {
        resetNavigationForRetarget(npc);
        npc.activeRoutineMarker(null);
        npc.activeRoutineState(null);
        npc.activeRoutineActionId(null);
        npc.activeRoutineSource(null);
        npc.lastCapabilityDecisionKey(null);
    }

    private boolean shouldStartRetargetWalkFromCurrentMarker(NpcRecord npc, MarkerType markerType, MarkerRecord previousMarker) {
        if (previousMarker == null) {
            return false;
        }

        NpcState state = npc.state();
        if (state == null || !state.isIdle()) {
            return false;
        }

        Optional<MarkerType> authoritativeType = resolveAuthoritativeMarkerType(state);
        if (authoritativeType.isEmpty() || authoritativeType.get() != markerType) {
            return false;
        }

        Vec3 currentPos = resolveCurrentNpcPosition(npc);
        if (currentPos == null) {
            return false;
        }

        return distanceSq(currentPos, previousMarker.position()) <= ENGINE_NAVIGATION_ARRIVAL_DISTANCE_SQ;
    }

    private Vec3 resolveCurrentNpcPosition(NpcRecord npc) {
        Vec3 livePos = readPosition(npc.entityRef());
        if (livePos != null) {
            npc.currentPosition(livePos);
            return livePos;
        }
        return npc.currentPosition();
    }

    private NpcState idleStateForMarker(MarkerType markerType) {
        return switch (markerType) {
            case BED -> NpcState.SLEEPING;
            case DOOR -> NpcState.OPENING_DOOR;
            case CHEST -> NpcState.USING_CHEST;
            case FOOD -> NpcState.EATING;
            case WORK -> NpcState.WORKING;
            case CHILL -> NpcState.CHILLING;
        };
    }

    private void resetNavigationForRetarget(NpcRecord npc) {
        npc.navigationState().clear();
        npc.pendingActionId(null);
        npc.activeActionId(null);
        npc.lastActionNoRestartLog(null);
        nextDoorActionAtMs.remove(npc.npcId());
        nextDoorCloseActionAtMs.remove(npc.npcId());
        nextDoorMarkerSkipLogAtMs.remove(npc.npcId());
        pendingDoorAttempts.remove(npc.npcId());
        pendingDoorCloseAttempts.remove(npc.npcId());
        activeDoorPasses.remove(npc.npcId());
    }

    private record OwnershipSnapshot(
        Map<String, String> claimedEntityUuids,
        List<RelinkWorkflowService.OwnedEntityRefClaim> claimedEntityRefs
    ) {
    }

    private OwnershipSnapshot buildOwnershipSnapshot(String worldName) {
        Map<String, String> claimedEntityUuids = new HashMap<>();
        List<RelinkWorkflowService.OwnedEntityRefClaim> claimedEntityRefs = new ArrayList<>();

        for (NpcRecord record : npcs.values()) {
            if (record.entityStatus() == NpcEntityStatus.DISABLED) {
                continue;
            }
            if (worldName != null && !worldName.equals(record.worldId().value())) {
                continue;
            }

            String entityUuid = record.entityUuid();
            if (entityUuid != null && !entityUuid.isBlank()) {
                claimedEntityUuids.putIfAbsent(entityUuid, record.npcId());
            }

            Ref<EntityStore> entityRef = record.entityRef();
            if (entityRef != null && entityRef.isValid()) {
                claimedEntityRefs.add(new RelinkWorkflowService.OwnedEntityRefClaim(record.npcId(), entityRef));
            }
        }

        return new OwnershipSnapshot(claimedEntityUuids, claimedEntityRefs);
    }

    private boolean isClaimedByLiveRef(Ref<EntityStore> candidateRef, List<RelinkWorkflowService.OwnedEntityRefClaim> claims) {
        if (candidateRef == null || claims == null || claims.isEmpty()) {
            return false;
        }

        for (RelinkWorkflowService.OwnedEntityRefClaim claim : claims) {
            if (claim == null || claim.entityRef() == null) {
                continue;
            }
            if (relinkSupport.sameRef(claim.entityRef(), candidateRef)) {
                return true;
            }
        }
        return false;
    }

    private int countOpenRelinkRecords(String worldName) {
        int open = 0;
        for (NpcRecord record : npcs.values()) {
            if (record.entityStatus() == NpcEntityStatus.DISABLED) {
                continue;
            }
            if (worldName != null && !worldName.equals(record.worldId().value())) {
                continue;
            }

            if (record.entityStatus() == NpcEntityStatus.NEEDS_RELINK
                || record.entityStatus() == NpcEntityStatus.MISSING_ENTITY
                || (record.entityStatus() == NpcEntityStatus.ACTIVE && !hasLiveEntity(record))) {
                open++;
            }
        }
        return open;
    }

    private RelinkWorkflowService.RelinkOutcome tryRelinkEntityRef(
        World world,
        NpcRecord npc,
        String trigger,
        OwnershipSnapshot ownership
    ) {
        return relinkWorkflowService.tryRelinkEntityRef(
            world,
            npc,
            trigger,
            ownership.claimedEntityUuids(),
            ownership.claimedEntityRefs()
        );
    }

    private RelinkWorkflowService.RelinkOutcome evaluateRelinkEntityRef(
        World world,
        NpcRecord npc,
        OwnershipSnapshot ownership
    ) {
        return relinkWorkflowService.evaluateRelinkEntityRef(
            world,
            npc,
            ownership.claimedEntityUuids(),
            ownership.claimedEntityRefs()
        );
    }

    private RelinkWorkflowService.RelinkEvaluationOutcome evaluateRelinkEntityRefDetailed(
        World world,
        NpcRecord npc,
        OwnershipSnapshot ownership
    ) {
        return relinkWorkflowService.evaluateRelinkEntityRefDetailed(
            world,
            npc,
            ownership.claimedEntityUuids(),
            ownership.claimedEntityRefs()
        );
    }

    private RelinkWorkflowService.AnchorRelinkOutcome tryAnchorRelinkEntityRef(
        World world,
        NpcRecord npc,
        String trigger,
        OwnershipSnapshot ownership
    ) {
        return relinkWorkflowService.tryAnchorRelinkEntityRef(
            world,
            npc,
            trigger,
            ownership.claimedEntityUuids(),
            ownership.claimedEntityRefs()
        );
    }

    private RelinkWorkflowService.AnchorRelinkOutcome evaluateAnchorRelinkEntityRef(
        World world,
        NpcRecord npc,
        OwnershipSnapshot ownership
    ) {
        return relinkWorkflowService.evaluateAnchorRelinkEntityRef(
            world,
            npc,
            ownership.claimedEntityUuids(),
            ownership.claimedEntityRefs()
        );
    }

    private RelinkWorkflowService.AnchorRelinkEvaluationOutcome evaluateAnchorRelinkEntityRefDetailed(
        World world,
        NpcRecord npc,
        OwnershipSnapshot ownership
    ) {
        return relinkWorkflowService.evaluateAnchorRelinkEntityRefDetailed(
            world,
            npc,
            ownership.claimedEntityUuids(),
            ownership.claimedEntityRefs()
        );
    }

    private boolean registerDryRunPlannedClaim(
        Map<String, String> plannedClaimedEntityUuids,
        List<RelinkWorkflowService.OwnedEntityRefClaim> plannedClaimedEntityRefs,
        Ref<EntityStore> candidateRef,
        String candidateUuid,
        String npcId
    ) {
        if (candidateRef == null && (candidateUuid == null || candidateUuid.isBlank())) {
            return false;
        }

        if (candidateUuid != null && !candidateUuid.isBlank()) {
            String owner = plannedClaimedEntityUuids.get(candidateUuid);
            if (owner != null && !owner.equals(npcId)) {
                return false;
            }
        }

        if (candidateRef != null && candidateRef.isValid()) {
            for (RelinkWorkflowService.OwnedEntityRefClaim claim : plannedClaimedEntityRefs) {
                if (claim == null || claim.entityRef() == null) {
                    continue;
                }
                if (relinkSupport.sameRef(claim.entityRef(), candidateRef) && !npcId.equals(claim.npcId())) {
                    return false;
                }
            }
        }

        if (candidateUuid != null && !candidateUuid.isBlank()) {
            plannedClaimedEntityUuids.putIfAbsent(candidateUuid, npcId);
        }
        if (candidateRef != null && candidateRef.isValid()) {
            plannedClaimedEntityRefs.add(new RelinkWorkflowService.OwnedEntityRefClaim(npcId, candidateRef));
        }
        return true;
    }

    private void dedupeRoleIdDuplicates(
        World world,
        NpcRecord npc,
        Ref<EntityStore> preferredKeepRef,
        RoleDefinition roleDefinition,
        int roleIndex,
        String trigger,
        String source,
        OwnershipSnapshot ownership
    ) {
        relinkWorkflowService.dedupeRoleIdDuplicates(
            world,
            npc,
            preferredKeepRef,
            roleDefinition,
            roleIndex,
            trigger,
            source,
            ownership.claimedEntityUuids(),
            ownership.claimedEntityRefs()
        );
    }

    private double distanceSq(Vec3 a, Vec3 b) {
        return relinkSupport.distanceSq(a, b);
    }

    private Vec3 readPosition(Ref<EntityStore> entityRef) {
        return entitySync.readPosition(entityRef);
    }

    private void updateNpcPositionFromEntity(NpcRecord npc, Ref<EntityStore> entityRef) {
        entitySync.updateNpcPositionFromEntity(npc, entityRef);
    }

    private void updatePersistedEntityIdentity(NpcRecord npc, Ref<EntityStore> entityRef) {
        entitySync.updatePersistedEntityIdentity(npc, entityRef);
    }

    private void clearEntityIdentity(NpcRecord npc) {
        npc.entityRef(null);
        npc.entityId(0);
        if (npc.entityStatus() != NpcEntityStatus.DISABLED) {
            npc.entityStatus(
                npc.entityUuid() == null || npc.entityUuid().isBlank()
                    ? NpcEntityStatus.MISSING_ENTITY
                    : NpcEntityStatus.NEEDS_RELINK
            );
        }
    }

    private void clearRespawnFailureState(String npcId) {
        respawnRecoveryService.clearRespawnFailureState(npcId);
    }

    private void normalizeRestorePosition(NpcRecord npc) {
        idleMarkerService.normalizeRestorePosition(npc);
    }

    private MarkerRecord resolveRestoreAnchor(NpcRecord npc, Vec3 current) {
        return idleMarkerService.resolveRestoreAnchor(npc, current);
    }

    private Optional<MarkerRecord> resolveStatePreferredMarker(NpcRecord npc) {
        return idleMarkerService.resolveStatePreferredMarker(npc);
    }

    private Optional<MarkerType> resolveMarkerTypeForRole(TargetRole role) {
        return markerResolver.resolveMarkerTypeForRole(role);
    }

    private Optional<MarkerType> resolveAuthoritativeMarkerType(NpcState state) {
        return markerResolver.resolveAuthoritativeMarkerType(state);
    }

    private boolean hasAuthoritativeIdleMarker(NpcState state) {
        return markerResolver.hasAuthoritativeIdleMarker(state);
    }

    private boolean enforceAuthoritativeIdlePosition(NpcRecord npc, String reason, boolean alignEntity) {
        return idleMarkerService.enforceAuthoritativeIdlePosition(npc, reason, alignEntity);
    }

    private Optional<MarkerRecord> resolveMarkerInNpcWorld(NpcRecord npc, MarkerType markerType, String markerId) {
        return markerResolver.resolveMarkerInNpcWorld(npc, markerType, markerId);
    }

    private Optional<MarkerRecord> resolveRequiredMarkerWithFallback(NpcRecord npc, MarkerType markerType) {
        return markerResolver.resolveRequiredMarkerWithFallback(npc, markerType);
    }

    private void scheduleRespawnRetry(String npcId, String reason) {
        respawnRecoveryService.scheduleRespawnRetry(npcId, reason);
    }

    private String staleReasonForRestore(NpcRecord npc) {
        return respawnRecoveryService.staleReasonForRestore(npc);
    }

    private String spawnContext(NpcRecord npc, String trigger, World world, RoleDefinition roleDefinition, Integer roleIndex) {
        String worldName = world != null ? world.getName() : npc.worldId().value();
        String roleName = roleDefinition != null ? roleDefinition.npcPluginRoleName() : "<unresolved>";
        int resolvedRoleIndex = roleIndex != null ? roleIndex : -1;
        int failures = respawnFailureCounts.getOrDefault(npc.npcId(), 0);
        Vec3 pos = npc.currentPosition();

        return "npcId=" + npc.npcId()
            + " npcName=" + quote(npc.npcName())
            + " roleId=" + npc.roleId()
            + " roleName=" + quote(roleName)
            + " roleIndex=" + resolvedRoleIndex
            + " entityUuid=" + nullToDash(npc.entityUuid())
            + " world=" + quote(worldName)
            + " pos=" + formatPosition(pos)
            + " trigger=" + quote(trigger)
            + " failures=" + failures
            + " markers={bed=" + nullToDash(npc.bedMarkerId())
            + ",door=" + nullToDash(npc.doorMarkerId())
                + ",chest=" + nullToDash(npc.chestMarkerId())
                + ",food=" + nullToDash(npc.foodMarkerId())
                + ",work=" + nullToDash(npc.workMarkerId())
                + ",chill=" + nullToDash(npc.chillMarkerId()) + "}";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String quote(String value) {
        return value == null ? "-" : value;
    }

    private String formatPosition(Vec3 pos) {
        if (pos == null) {
            return "-";
        }
        return String.format(Locale.ROOT, "%.3f,%.3f,%.3f", pos.x(), pos.y(), pos.z());
    }

    private void logInfo(String eventKey, String message) {
        System.out.println("[KeystoneNPC][" + eventKey + "] " + message);
    }

    private void logSevere(String eventKey, String message) {
        System.err.println("[KeystoneNPC][" + eventKey + "] " + message);
    }

    private boolean shouldLogDoorEvent(NpcRecord npc) {
        return npc != null && npc.state() != null && npc.state().isWalking();
    }

    private void logDoorInfo(NpcRecord npc, String eventKey, String message) {
        if (!shouldLogDoorEvent(npc)) {
            return;
        }
        logInfo(eventKey, message);
    }

    private boolean canOpenDoorsWithDebug(NpcRecord npc) {
        boolean canOpen = capabilityChecks.hasOrDefault(npc.roleId(), NpcCapability.OPEN_DOORS, true);

        NpcState state = npc.state();
        if (state == null || !state.isWalking()) {
            npc.lastCapabilityDecisionKey(null);
            return canOpen;
        }

        if (NpcDebugSupport.logCapabilityChecksEnabled(templateResolver, npc.roleId())) {
            String decisionKey = "door-capability:" + state.name() + ":" + canOpen;
            if (!decisionKey.equals(npc.lastCapabilityDecisionKey())) {
                logInfo("DOOR_CAPABILITY_CHECK", "Open-door capability check: npcId=" + npc.npcId()
                    + " npcName=" + quote(npc.npcName())
                    + " roleId=" + npc.roleId()
                    + " state=" + state.name()
                    + " allowed=" + canOpen);
                npc.lastCapabilityDecisionKey(decisionKey);
            }
        }
        return canOpen;
    }

    private boolean isRoutineLoggingEnabled(NpcRecord npc) {
        return NpcDebugSupport.logRoutineChangesEnabled(templateResolver, npc.roleId());
    }

    private void emitRoutineChatMessage(NpcRecord npc, String message) {
        if (npc == null || message == null || message.isBlank()) {
            return;
        }
        NpcDebugSupport.sendGlobalChat(message);
    }

    private void emitMissingMarkerWarnings(NpcRecord npc, String missingMarkers) {
        if (npc == null) {
            return;
        }
        logSevere("MISSING_REQUIRED_MARKERS", "NPC is missing required marker assignments: npcId="
            + npc.npcId() + " npcName=" + quote(npc.npcName()) + " missing=" + missingMarkers);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private boolean hasActiveNavigation(NavigationTarget navState) {
        return navigationRuntimeService.hasActiveNavigation(navState);
    }

    private boolean hasLiveEntity(NpcRecord npc) {
        Ref<EntityStore> entityRef = npc.entityRef();
        return entityRef != null && entityRef.isValid();
    }

}
