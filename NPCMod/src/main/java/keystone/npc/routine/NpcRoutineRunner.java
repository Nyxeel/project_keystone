package keystone.npc.routine;

import java.util.ArrayList;
import java.util.Deque;
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
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import keystone.npc.capabilities.CapabilityChecks;
import keystone.npc.capabilities.NpcCapability;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.domain.NpcRecord;
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
    private static final int UUID_RELINK_MAX_MISSES_BEFORE_RESPAWN = 120;
    private static final long UUID_RELINK_MIN_WAIT_BEFORE_RESPAWN_MS = 5_000L;
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
            UUID_RELINK_MAX_MISSES_BEFORE_RESPAWN,
            UUID_RELINK_MIN_WAIT_BEFORE_RESPAWN_MS,
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
            this.nextDoorActionAtMs,
            this.nextDoorCloseActionAtMs,
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
        pendingDoorAttempts.clear();
        pendingDoorCloseAttempts.clear();
        activeDoorPasses.clear();
        lastRestoreAtMs = System.currentTimeMillis();

        int cleaned = 0;
        for (var npc : loaded) {
            reconcilePersistedMarkerAssignments(npc);

            String staleReason = staleReasonForRestore(npc);
            if (staleReason != null) {
                cleaned++;
                logSevere("RESTORE_STALE_NPC", "Dropping stale NPC from persisted state: "
                    + spawnContext(npc, "restore", null, null, null)
                    + " reason=" + staleReason);
                continue;
            }

            // Entity references are runtime-only and always invalid after restart.
            npc.entityRef(null);
            npc.entityId(0);
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

        if (cleaned > 0) {
            logSevere("RESTORE_CLEANUP_SUMMARY", "Removed stale NPC entries during restore: count=" + cleaned);
        }
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

    public int spawnRestoredNpcs(String trigger) {
        int queued = 0;
        long now = System.currentTimeMillis();
        for (NpcRecord npc : npcs.values()) {
            if (hasLiveEntity(npc)) {
                npc.entityId(1);
                updatePersistedEntityIdentity(npc, npc.entityRef());
                clearRespawnFailureState(npc.npcId());
                continue;
            }

            World world = Universe.get().getWorld(npc.worldId().value());
            if (world != null) {
                RelinkWorkflowService.RelinkOutcome relinkOutcome = tryRelinkEntityRef(world, npc, trigger);
                if (relinkOutcome == RelinkWorkflowService.RelinkOutcome.SUCCESS) {
                    clearRespawnFailureState(npc.npcId());
                    continue;
                }
                if (relinkOutcome == RelinkWorkflowService.RelinkOutcome.PENDING) {
                    continue;
                }

                if (tryAnchorRelinkEntityRef(world, npc, trigger)) {
                    clearRespawnFailureState(npc.npcId());
                    continue;
                }
            }

            Long retryAt = respawnRetryAtMs.get(npc.npcId());
            if (retryAt != null && now < retryAt) {
                continue;
            }

            if (npc.entityRef() != null || npc.entityId() != 0) {
                npc.entityRef(null);
                npc.entityId(0);
            }

            if (!spawnRequestsInFlight.add(npc.npcId())) {
                continue;
            }

            if (world == null) {
                if (!"tick-retry".equals(trigger)) {
                    logSevere("RESPAWN_WORLD_MISSING", "Delaying NPC respawn: "
                        + spawnContext(npc, trigger, null, null, null)
                        + " worldMissing=" + npc.worldId());
                }
                scheduleRespawnRetry(npc.npcId(), "world-missing");
                spawnRequestsInFlight.remove(npc.npcId());
                continue;
            }

            queued++;
            int attempt = respawnFailureCounts.getOrDefault(npc.npcId(), 0) + 1;
            if (!"tick-retry".equals(trigger) || attempt > 1) {
                logInfo("RESPAWN_ATTEMPT", "Queue respawn attempt=" + attempt + " "
                    + spawnContext(npc, trigger, world, null, null));
            }
            world.execute(() -> {
                try {
                    if (spawnNpcEntity(world, npc, trigger)) {
                        clearRespawnFailureState(npc.npcId());
                    } else {
                        scheduleRespawnRetry(npc.npcId(), "spawn-failed");
                    }
                } catch (Exception ex) {
                    scheduleRespawnRetry(npc.npcId(), "spawn-exception");
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

        npcs.put(npc.npcId(), npc);
        return npc;
    }

    public NpcRecord linkEntityRef(String npcId, Ref<EntityStore> entityRef) {
        NpcRecord npc = npcs.get(npcId);
        if (npc != null) {
            npc.entityRef(entityRef);
            npc.entityId(1);  // Mark as spawned (persist to JSON)
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

    private boolean spawnNpcEntity(World world, NpcRecord npc, String trigger) {
        if (hasLiveEntity(npc)) {
            npc.entityId(1);
            return true;
        }

        npc.entityRef(null);
        npc.entityId(0);

        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            logSevere("RESPAWN_UNKNOWN_ROLE", "Unknown role while restoring NPC: "
                + spawnContext(npc, trigger, world, null, null));
            return false;
        }

        RoleDefinition definition = roleDefinition.get();
        int roleIndex = NPCPlugin.get().getIndex(definition.npcPluginRoleName());

        if (roleIndex < 0) {
            logSevere("RESPAWN_ROLE_INDEX_MISSING", "Role index not found: "
                + spawnContext(npc, trigger, world, definition, roleIndex));
            return false;
        }

        Vec3 position = npc.currentPosition() != null ? npc.currentPosition() : new Vec3(0, 0, 0);
        Vector3d spawnPosition = new Vector3d(position.x(), position.y(), position.z());
        Store<EntityStore> store = world.getEntityStore().getStore();

        var pair = NPCPlugin.get().spawnEntity(store, roleIndex, spawnPosition, Rotation3f.IDENTITY, null, null);
        if (pair == null) {
            logSevere("RESPAWN_SPAWN_ENTITY_NULL", "spawnEntity returned null: "
                + spawnContext(npc, trigger, world, definition, roleIndex));
            return false;
        }

        Ref<EntityStore> spawnedRef = pair.first();
        if (spawnedRef == null || !spawnedRef.isValid()) {
            logSevere("RESPAWN_INVALID_ENTITY_REF", "spawnEntity returned invalid ref: "
                + spawnContext(npc, trigger, world, definition, roleIndex));
            return false;
        }

        npc.entityRef(spawnedRef);
        npc.entityId(1);  // Mark as spawned (persist to JSON)
        updatePersistedEntityIdentity(npc, spawnedRef);
        dedupeRoleIdDuplicates(world, npc, spawnedRef, definition, roleIndex, trigger, "post-spawn");
        if (respawnFailureCounts.getOrDefault(npc.npcId(), 0) > 0) {
            logInfo("RESPAWN_SUCCESS_AFTER_RETRY", "Restored NPC entity after retries: "
                + spawnContext(npc, trigger, world, definition, roleIndex));
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
        if (npc.state() == null || !npc.state().isWalking()) {
            npc.lastCapabilityDecisionKey(null);
        }
        reconcilePersistedMarkerAssignments(npc);
        npcUpdateWorkflowService.updateNpc(npc, world);
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
        pendingDoorAttempts.remove(npc.npcId());
        pendingDoorCloseAttempts.remove(npc.npcId());
        activeDoorPasses.remove(npc.npcId());
    }

    private RelinkWorkflowService.RelinkOutcome tryRelinkEntityRef(World world, NpcRecord npc, String trigger)
	{
        return relinkWorkflowService.tryRelinkEntityRef(world, npc, trigger);
    }

    private boolean tryAnchorRelinkEntityRef(World world, NpcRecord npc, String trigger)
	{
        return relinkWorkflowService.tryAnchorRelinkEntityRef(world, npc, trigger);
    }

    private void dedupeRoleIdDuplicates(
        World world,
        NpcRecord npc,
        Ref<EntityStore> preferredKeepRef,
        RoleDefinition roleDefinition,
        int roleIndex,
        String trigger,
        String source
    ) {
        relinkWorkflowService.dedupeRoleIdDuplicates(world, npc, preferredKeepRef, roleDefinition, roleIndex, trigger, source);
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
        npc.entityUuid(null);
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
