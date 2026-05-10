package keystone.npc.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import keystone.npc.model.NpcRecord;
import keystone.npc.model.NpcState;
import keystone.npc.navigation.NavigationState;
import keystone.npc.navigation.NpcNavigation;
import keystone.npc.role.RoleDefinition;
import keystone.npc.role.RoleDefinitionRegistry;
import keystone.npc.world.MarkerRecord;
import keystone.npc.world.MarkerRegistry;
import keystone.npc.world.MarkerType;
import keystone.npc.world.Vec3;

/**
 * MVP A: minimaler "Scheduler".
 * - verwaltet 1..N NPCs (MVP A: 1)
 * - tickt periodisch
 * - wählt Ziel (work vs bed) anhand Tageszeit
 * - zwingt Routing via door_marker
 */
public final class NpcScheduler {
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

    private final MarkerRegistry markerRegistry;
    private final RoleDefinitionRegistry roleDefinitions;
    private final Map<String, NpcRecord> npcs = new ConcurrentHashMap<>();
    private final Set<String> spawnRequestsInFlight = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> respawnRetryAtMs = new ConcurrentHashMap<>();
    private final Map<String, Integer> respawnFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> uuidRelinkMissCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> uuidRelinkFirstMissAtMs = new ConcurrentHashMap<>();
    private volatile long lastRestoreAtMs = 0L;

    private enum RelinkOutcome {
        SUCCESS,
        PENDING,
        NO_MATCH
    }

    public NpcScheduler(MarkerRegistry markerRegistry, RoleDefinitionRegistry roleDefinitions) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
    }

    public void restore(List<NpcRecord> loaded) {
        npcs.clear();
        spawnRequestsInFlight.clear();
        respawnRetryAtMs.clear();
        respawnFailureCounts.clear();
        uuidRelinkMissCounts.clear();
        uuidRelinkFirstMissAtMs.clear();
        lastRestoreAtMs = System.currentTimeMillis();

        int cleaned = 0;
        for (var npc : loaded) {
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

            NavigationState navigationState = npc.navigationState();
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

    public NpcRecord getNpc(String npcId) {
        return npcs.get(npcId);
    }

    public boolean removeNpc(String npcId) {
        NpcRecord npc = npcs.remove(npcId);
        if (npc == null) {
            return false;
        }

        spawnRequestsInFlight.remove(npc.npcId());
        clearRespawnFailureState(npc.npcId());
        removeLiveEntity(npc);
        return true;
    }

    public boolean removeNpcByIndex(int index) {
        var npcList = new ArrayList<>(npcs.values());
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
                RelinkOutcome relinkOutcome = tryRelinkEntityRef(world, npc, trigger);
                if (relinkOutcome == RelinkOutcome.SUCCESS) {
                    clearRespawnFailureState(npc.npcId());
                    continue;
                }
                if (relinkOutcome == RelinkOutcome.PENDING) {
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

    public NpcRecord spawnNpc(String npcId, String name, String roleId, keystone.npc.world.WorldId worldId, keystone.npc.world.Vec3 position) {
        var npc = new NpcRecord(npcId, name, roleId, worldId);

        // Set spawn position
        npc.currentPosition(position);

        // MVP A: bind to "active" markers (last set)
        npc.bedMarkerId(markerRegistry.getActive(MarkerType.BED).map(m -> m.markerId()).orElse(null));
        npc.doorMarkerId(markerRegistry.getActive(MarkerType.DOOR).map(m -> m.markerId()).orElse(null));
        npc.workMarkerId(markerRegistry.getActive(MarkerType.WORK).map(m -> m.markerId()).orElse(null));

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
     * Runs once per world store tick through NpcSchedulerTickSystem.
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
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KeystoneNPC] Missing role definition for NPC '" + npc.npcName()
                    + "' (" + npc.npcId() + "): roleId=" + npc.roleId());
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        // 1) Validate markers
        if (!hasRequiredMarkers(npc, roleDefinition.get())) {
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KeystoneNPC] Missing marker assignment for NPC '" + npc.npcName() + "' ("
                    + npc.npcId() + "): required=" + missingRequiredMarkers(npc, roleDefinition.get()));
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        NpcState desiredTargetState = resolveDesiredTargetState(world, npc, roleDefinition.get());
        if (desiredTargetState == null) {
            return;
        }

        NavigationState navState = npc.navigationState();

        // 2) Handle active navigation.
        if (hasActiveNavigation(navState)) {
            if (navState.isComplete()) {
                finishNavigation(npc, navState);
                return;
            }

            // Update both logical and visible entity position.
            Vec3 currentPos = navState.getCurrentPosition();
            if (currentPos != null) {
                npc.currentPosition(currentPos);
                updateEntityPosition(npc, currentPos);
            }

            // Re-route if day/night target changed while still walking.
            NpcState activeTargetState = navState.getTargetState();
            if (activeTargetState != null && activeTargetState != desiredTargetState) {
                System.out.println("[KeystoneNPC] Navigation reroute: npc=" + npc.npcName()
                    + " activeTarget=" + targetTypeForState(activeTargetState)
                    + " newTarget=" + targetTypeForState(desiredTargetState)
                    + " currentPos=" + npc.currentPosition());
                navState.clear();

                if (desiredTargetState == NpcState.SLEEPING) {
                    startNavigationToBed(npc);
                } else if (desiredTargetState == NpcState.WORKING) {
                    startNavigationToWork(npc);
                }
                return;
            }

            if (navState.isComplete()) {
                finishNavigation(npc, navState);
            }
            return;
        }

        // 3) No active navigation: choose target from world time.
        if (desiredTargetState == NpcState.SLEEPING) {
            if (npc.state() != NpcState.SLEEPING) {
                startNavigationToBed(npc);
            } else {
                enforceAuthoritativeIdlePosition(npc, "idle-state-check", true);
            }
        } else if (desiredTargetState == NpcState.WORKING) {
            if (npc.state() != NpcState.WORKING) {
                startNavigationToWork(npc);
            } else {
                enforceAuthoritativeIdlePosition(npc, "idle-state-check", true);
            }
        }
    }

    private void startNavigationToBed(NpcRecord npc) {
        // Route: directly to bed (MVP A: simplified)
        Optional<MarkerRecord> bedMarker = resolveRequiredMarkerWithFallback(npc, MarkerType.BED);
        if (bedMarker.isEmpty()) {
            System.err.println("[KeystoneNPC] Missing bed marker for NPC '" + npc.npcName() + "' (" + npc.npcId() + ")"
                + " markerId=" + npc.bedMarkerId());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        Vec3 startPos = npc.currentPosition();
        Vec3 bedPos = bedMarker.get().position();
        long durationMs = NpcNavigation.calculateDurationMs(startPos, bedPos);
        npc.navigationState().startNavigation(startPos, bedPos, durationMs, NpcState.SLEEPING);
        npc.state(NpcState.WALKING_TO_BED);

        System.out.println("[KeystoneNPC] Navigation start: npc=" + npc.npcName()
            + " start=" + startPos
            + " target=" + bedPos
            + " targetType=BED");
    }

    private void startNavigationToWork(NpcRecord npc) {
        // Route: directly to work (MVP A: simplified)
        Optional<MarkerRecord> workMarker = resolveRequiredMarkerWithFallback(npc, MarkerType.WORK);
        if (workMarker.isEmpty()) {
            System.err.println("[KeystoneNPC] Missing work marker for NPC '" + npc.npcName() + "' (" + npc.npcId() + ")"
                + " markerId=" + npc.workMarkerId());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        Vec3 startPos = npc.currentPosition();
        Vec3 workPos = workMarker.get().position();
        long durationMs = NpcNavigation.calculateDurationMs(startPos, workPos);
        npc.navigationState().startNavigation(startPos, workPos, durationMs, NpcState.WORKING);
        npc.state(NpcState.WALKING_TO_WORK);

        System.out.println("[KeystoneNPC] Navigation start: npc=" + npc.npcName()
            + " start=" + startPos
            + " target=" + workPos
            + " targetType=WORK");
    }

    private NpcState resolveDesiredTargetState(World world, NpcRecord npc, RoleDefinition roleDefinition) {
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

    private boolean hasRequiredMarkers(NpcRecord npc, RoleDefinition roleDefinition) {
        for (MarkerType markerType : roleDefinition.requiredMarkers()) {
            if (resolveRequiredMarkerWithFallback(npc, markerType).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String missingRequiredMarkers(NpcRecord npc, RoleDefinition roleDefinition) {
        List<String> missing = new ArrayList<>();
        for (MarkerType markerType : roleDefinition.requiredMarkers()) {
            if (resolveRequiredMarkerWithFallback(npc, markerType).isEmpty()) {
                missing.add(markerType.name().toLowerCase(Locale.ROOT));
            }
        }
        return String.join(",", missing);
    }

    private String markerIdForType(NpcRecord npc, MarkerType markerType) {
        return switch (markerType) {
            case BED -> npc.bedMarkerId();
            case DOOR -> npc.doorMarkerId();
            case WORK -> npc.workMarkerId();
        };
    }

    private void setMarkerIdForType(NpcRecord npc, MarkerType markerType, String markerId) {
        switch (markerType) {
            case BED -> npc.bedMarkerId(markerId);
            case DOOR -> npc.doorMarkerId(markerId);
            case WORK -> npc.workMarkerId(markerId);
        }
    }

    private RelinkOutcome tryRelinkEntityRef(World world, NpcRecord npc, String trigger) {
        String rawUuid = npc.entityUuid();
        if (rawUuid == null || rawUuid.isBlank()) {
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.NO_MATCH;
        }

        UUID entityUuid;
        try {
            entityUuid = UUID.fromString(rawUuid);
        } catch (IllegalArgumentException ex) {
            logSevere("RESPAWN_RELINK_UUID_INVALID", "Ignoring invalid persisted entity UUID: "
                + spawnContext(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid);
            npc.entityUuid(null);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.NO_MATCH;
        }

        Ref<EntityStore> relinkRef = world.getEntityStore().getRefFromUUID(entityUuid);
        if (relinkRef == null || !relinkRef.isValid()) {
            long now = System.currentTimeMillis();
            long firstMissAt = uuidRelinkFirstMissAtMs.computeIfAbsent(npc.npcId(), key -> now);
            int misses = uuidRelinkMissCounts.getOrDefault(npc.npcId(), 0) + 1;
            uuidRelinkMissCounts.put(npc.npcId(), misses);

            long waitedMs = Math.max(0L, now - firstMissAt);

            if (misses < UUID_RELINK_MAX_MISSES_BEFORE_RESPAWN || waitedMs < UUID_RELINK_MIN_WAIT_BEFORE_RESPAWN_MS) {
                if (misses == 1 || misses % 10 == 0) {
                    logInfo("RESPAWN_RELINK_PENDING", "Persisted UUID not found yet, deferring spawn: "
                        + spawnContext(npc, trigger, world, null, null)
                        + " entityUuid=" + rawUuid
                        + " misses=" + misses
                        + " threshold=" + UUID_RELINK_MAX_MISSES_BEFORE_RESPAWN
                        + " waitedMs=" + waitedMs
                        + " minWaitMs=" + UUID_RELINK_MIN_WAIT_BEFORE_RESPAWN_MS);
                }
                return RelinkOutcome.PENDING;
            }

            logSevere("RESPAWN_RELINK_GIVEUP", "Persisted UUID still not resolvable, allowing respawn fallback: "
                + spawnContext(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid
                + " misses=" + misses
                + " threshold=" + UUID_RELINK_MAX_MISSES_BEFORE_RESPAWN
                + " waitedMs=" + waitedMs
                + " minWaitMs=" + UUID_RELINK_MIN_WAIT_BEFORE_RESPAWN_MS);

            npc.entityUuid(null);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.NO_MATCH;
        }

        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return RelinkOutcome.PENDING;
        }

        NPCEntity liveNpc = relinkRef.getStore().getComponent(relinkRef, npcType);
        if (liveNpc == null) {
            logSevere("RESPAWN_RELINK_NOT_NPC", "Persisted UUID resolved to non-NPC entity: "
                + spawnContext(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid);
            npc.entityUuid(null);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.NO_MATCH;
        }

        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        int expectedRoleIndex = roleDefinition
            .map(definition -> NPCPlugin.get().getIndex(definition.npcPluginRoleName()))
            .orElse(-1);

        if (expectedRoleIndex >= 0
            && liveNpc.getRoleIndex() != expectedRoleIndex
            && liveNpc.getSpawnRoleIndex() != expectedRoleIndex) {
            logSevere("RESPAWN_RELINK_ROLE_MISMATCH", "Persisted UUID points to wrong role entity: "
                + spawnContext(npc, trigger, world, roleDefinition.orElse(null), expectedRoleIndex)
                + " liveRoleIndex=" + liveNpc.getRoleIndex()
                + " liveSpawnRoleIndex=" + liveNpc.getSpawnRoleIndex());
            npc.entityUuid(null);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.NO_MATCH;
        }

        npc.entityRef(relinkRef);
        npc.entityId(1);
        updatePersistedEntityIdentity(npc, relinkRef);
        dedupeRoleIdDuplicates(world, npc, relinkRef, roleDefinition.orElse(null), expectedRoleIndex, trigger, "relink");
        enforceAuthoritativeIdlePosition(npc, "relink", true);
        uuidRelinkMissCounts.remove(npc.npcId());
        uuidRelinkFirstMissAtMs.remove(npc.npcId());

        logInfo("RESPAWN_RELINK_SUCCESS", "Re-linked persisted NPC to existing world entity: "
            + spawnContext(npc, trigger, world, roleDefinition.orElse(null), expectedRoleIndex));
        return RelinkOutcome.SUCCESS;
    }

    private boolean tryAnchorRelinkEntityRef(World world, NpcRecord npc, String trigger) {
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            return false;
        }

        int roleIndex = NPCPlugin.get().getIndex(roleDefinition.get().npcPluginRoleName());
        if (roleIndex < 0) {
            return false;
        }

        Vec3 center = npc.currentPosition();
        if (center == null) {
            return false;
        }

        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return false;
        }

        Store<EntityStore> store = world.getEntityStore().getStore();
        List<Ref<EntityStore>> candidates = new ArrayList<>();
        Query<EntityStore> npcQuery = Query.and(npcType);

        store.forEachChunk(npcQuery, (BiConsumer<com.hypixel.hytale.component.ArchetypeChunk<EntityStore>, com.hypixel.hytale.component.CommandBuffer<EntityStore>>) (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                NPCEntity liveNpc = archetypeChunk.getComponent(index, npcType);
                if (liveNpc == null) {
                    continue;
                }

                if (liveNpc.getRoleIndex() != roleIndex && liveNpc.getSpawnRoleIndex() != roleIndex) {
                    continue;
                }

                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }

                if (distanceSq(center, transform.getPosition()) > ROLEID_ANCHOR_RELINK_RADIUS_SQ) {
                    continue;
                }

                Ref<EntityStore> candidateRef = archetypeChunk.getReferenceTo(index);
                if (candidateRef != null && candidateRef.isValid()) {
                    candidates.add(candidateRef);
                }
            }
        });

        if (candidates.isEmpty()) {
            return false;
        }

        Ref<EntityStore> keepRef = findClosestRef(candidates, center);
        if (keepRef == null || !keepRef.isValid()) {
            return false;
        }

        npc.entityRef(keepRef);
        npc.entityId(1);
        updatePersistedEntityIdentity(npc, keepRef);
        dedupeRoleIdDuplicates(world, npc, keepRef, roleDefinition.get(), roleIndex, trigger, "anchor-relink");
        enforceAuthoritativeIdlePosition(npc, "anchor-relink", true);

        uuidRelinkMissCounts.remove(npc.npcId());
        uuidRelinkFirstMissAtMs.remove(npc.npcId());

        logInfo("RESPAWN_RELINK_BY_ANCHOR", "Re-linked NPC via role+position fallback: "
            + spawnContext(npc, trigger, world, roleDefinition.get(), roleIndex)
            + " candidates=" + candidates.size()
            + " anchorRadius=" + ROLEID_ANCHOR_RELINK_RADIUS);
        return true;
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
        if (roleIndex < 0) {
            return;
        }

        Vec3 tempCenter = npc.currentPosition();
        if (tempCenter == null) {
            tempCenter = readPosition(preferredKeepRef);
        }
        if (tempCenter == null) {
            return;
        }
        final Vec3 center = tempCenter;
        final List<Vec3> dedupeAnchors = collectDedupeAnchors(npc, center);

        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return;
        }

        Store<EntityStore> store = world.getEntityStore().getStore();
        List<Ref<EntityStore>> nearbyCandidates = new ArrayList<>();
        Query<EntityStore> npcQuery = Query.and(npcType);

        store.forEachChunk(npcQuery, (BiConsumer<com.hypixel.hytale.component.ArchetypeChunk<EntityStore>, com.hypixel.hytale.component.CommandBuffer<EntityStore>>) (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                NPCEntity liveNpc = archetypeChunk.getComponent(index, npcType);
                if (liveNpc == null) {
                    continue;
                }

                if (liveNpc.getRoleIndex() != roleIndex && liveNpc.getSpawnRoleIndex() != roleIndex) {
                    continue;
                }

                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }

                if (!isNearAnyDedupeAnchor(dedupeAnchors, transform.getPosition())) {
                    continue;
                }

                Ref<EntityStore> candidateRef = archetypeChunk.getReferenceTo(index);
                if (candidateRef != null && candidateRef.isValid()) {
                    nearbyCandidates.add(candidateRef);
                }
            }
        });

        if (nearbyCandidates.size() <= 1) {
            return;
        }

        Ref<EntityStore> keepRef = preferredKeepRef;
        if (keepRef == null
            || !keepRef.isValid()
            || !containsRef(nearbyCandidates, keepRef)
            || !isRefNearCenter(keepRef, center, ROLEID_DEDUPE_RADIUS_SQ)) {
            keepRef = findClosestRef(nearbyCandidates, center);
        }

        if (keepRef == null || !keepRef.isValid()) {
            return;
        }

        int removed = 0;
        for (Ref<EntityStore> candidateRef : nearbyCandidates) {
            if (sameRef(candidateRef, keepRef)) {
                continue;
            }

            if (candidateRef != null && candidateRef.isValid()) {
                candidateRef.getStore().removeEntity(candidateRef, RemoveReason.REMOVE);
                removed++;
                logSevere("DEDUPE_ROLEID_REMOVED", "Removed duplicate NPC entity by roleId proximity: "
                    + spawnContext(npc, trigger, world, roleDefinition, roleIndex)
                    + " source=" + source
                    + " radius=" + ROLEID_DEDUPE_RADIUS);
            }
        }

        npc.entityRef(keepRef);
        npc.entityId(1);
        updatePersistedEntityIdentity(npc, keepRef);
        enforceAuthoritativeIdlePosition(npc, "dedupe-" + source, true);

        if (removed > 0) {
            logSevere("DEDUPE_ROLEID_SUMMARY", "RoleId dedupe removed duplicates for NPC slot: "
                + spawnContext(npc, trigger, world, roleDefinition, roleIndex)
                + " source=" + source
                + " removed=" + removed
                + " candidates=" + nearbyCandidates.size()
                + " radius=" + ROLEID_DEDUPE_RADIUS);
        }
    }

    private boolean containsRef(List<Ref<EntityStore>> refs, Ref<EntityStore> target) {
        for (Ref<EntityStore> ref : refs) {
            if (sameRef(ref, target)) {
                return true;
            }
        }
        return false;
    }

    private Ref<EntityStore> findClosestRef(List<Ref<EntityStore>> refs, Vec3 center) {
        Ref<EntityStore> closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Ref<EntityStore> ref : refs) {
            Vec3 pos = readPosition(ref);
            if (pos == null) {
                continue;
            }

            double distanceSq = distanceSq(center, new Vector3d(pos.x(), pos.y(), pos.z()));
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closest = ref;
            }
        }

        return closest;
    }

    private boolean sameRef(Ref<EntityStore> a, Ref<EntityStore> b) {
        return a == b || (a != null && a.equals(b));
    }

    private List<Vec3> collectDedupeAnchors(NpcRecord npc, Vec3 center) {
        List<Vec3> anchors = new ArrayList<>();
        anchors.add(center);

        Optional<MarkerType> authoritativeMarkerType = resolveAuthoritativeMarkerType(npc.state());
        if (authoritativeMarkerType.isPresent()) {
            addMarkerAnchor(anchors, npc, authoritativeMarkerType.get());
            return anchors;
        }

        Optional<MarkerRecord> preferred = resolveStatePreferredMarker(npc);
        preferred.ifPresent(marker -> anchors.add(marker.position()));

        return anchors;
    }

    private boolean isRefNearCenter(Ref<EntityStore> ref, Vec3 center, double maxDistanceSq) {
        Vec3 refPos = readPosition(ref);
        return refPos != null && distanceSq(center, refPos) <= maxDistanceSq;
    }

    private void addMarkerAnchor(List<Vec3> anchors, NpcRecord npc, MarkerType markerType) {
        Optional<MarkerRecord> marker = resolveRequiredMarkerWithFallback(npc, markerType);
        marker.ifPresent(value -> anchors.add(value.position()));
    }

    private boolean isNearAnyDedupeAnchor(List<Vec3> anchors, Vector3d position) {
        for (Vec3 anchor : anchors) {
            if (distanceSq(anchor, position) <= ROLEID_DEDUPE_RADIUS_SQ) {
                return true;
            }
        }
        return false;
    }

    private double distanceSq(Vec3 center, Vector3d other) {
        double dx = center.x() - other.x;
        double dy = center.y() - other.y;
        double dz = center.z() - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private Vec3 readPosition(Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid()) {
            return null;
        }

        TransformComponent transform = entityRef.getStore().getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }

        Vector3d position = transform.getPosition();
        return new Vec3(position.x(), position.y(), position.z());
    }

    private void updateNpcPositionFromEntity(NpcRecord npc, Ref<EntityStore> entityRef) {
        Vec3 position = readPosition(entityRef);
        if (position != null) {
            npc.currentPosition(position);
        }
    }

    private void updatePersistedEntityIdentity(NpcRecord npc, Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid()) {
            npc.entityUuid(null);
            return;
        }

        UUIDComponent uuidComponent = entityRef.getStore().getComponent(entityRef, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            npc.entityUuid(null);
            return;
        }

        npc.entityUuid(uuidComponent.getUuid().toString());
    }

    private void clearEntityIdentity(NpcRecord npc) {
        npc.entityRef(null);
        npc.entityId(0);
        npc.entityUuid(null);
    }

    private void clearRespawnFailureState(String npcId) {
        respawnFailureCounts.remove(npcId);
        respawnRetryAtMs.remove(npcId);
        uuidRelinkMissCounts.remove(npcId);
        uuidRelinkFirstMissAtMs.remove(npcId);
    }

    private void normalizeRestorePosition(NpcRecord npc) {
        if (isWalkingState(npc.state())) {
            return;
        }

        Optional<MarkerType> authoritativeMarkerType = resolveAuthoritativeMarkerType(npc.state());
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
        if (current == null || distanceSq(current, anchorPos) > ROLEID_ANCHOR_RELINK_RADIUS_SQ) {
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

    private MarkerRecord resolveRestoreAnchor(NpcRecord npc, Vec3 current) {
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

    private Optional<MarkerRecord> resolveStatePreferredMarker(NpcRecord npc) {
        Optional<MarkerType> authoritativeMarkerType = resolveAuthoritativeMarkerType(npc.state());
        if (authoritativeMarkerType.isPresent()) {
            return resolveRequiredMarkerWithFallback(npc, authoritativeMarkerType.get());
        }

        if (npc.state() == NpcState.WALKING_TO_BED) {
            return resolveRequiredMarkerWithFallback(npc, MarkerType.BED);
        }
        if (npc.state() == NpcState.WALKING_TO_WORK) {
            return resolveRequiredMarkerWithFallback(npc, MarkerType.WORK);
        }
        if (npc.state() == NpcState.WALKING_TO_DOOR) {
            return resolveRequiredMarkerWithFallback(npc, MarkerType.DOOR);
        }

        return Optional.empty();
    }

    private Optional<MarkerType> resolveAuthoritativeMarkerType(NpcState state) {
        if (state == NpcState.SLEEPING) {
            return Optional.of(MarkerType.BED);
        }
        if (state == NpcState.WORKING) {
            return Optional.of(MarkerType.WORK);
        }
        return Optional.empty();
    }

    private boolean hasAuthoritativeIdleMarker(NpcState state) {
        return resolveAuthoritativeMarkerType(state).isPresent();
    }

    private boolean enforceAuthoritativeIdlePosition(NpcRecord npc, String reason, boolean alignEntity) {
        Optional<MarkerType> markerType = resolveAuthoritativeMarkerType(npc.state());
        if (markerType.isEmpty()) {
            return false;
        }

        Optional<MarkerRecord> marker = resolveRequiredMarkerWithFallback(npc, markerType.get());
        if (marker.isEmpty()) {
            return false;
        }

        Vec3 authoritativePos = marker.get().position();
        Vec3 currentPos = npc.currentPosition();
        boolean changed = currentPos == null || distanceSq(currentPos, authoritativePos) > IDLE_POSITION_EPSILON_SQ;

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

    private void alignEntityToNpcPosition(NpcRecord npc, String reason) {
        if (!hasLiveEntity(npc)) {
            return;
        }

        Vec3 authoritativePos = npc.currentPosition();
        if (authoritativePos == null) {
            return;
        }

        Vec3 livePos = readPosition(npc.entityRef());
        if (livePos == null || distanceSq(livePos, authoritativePos) > IDLE_POSITION_EPSILON_SQ) {
            updateEntityPosition(npc, authoritativePos);
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
        Optional<MarkerRecord> marker = resolveRequiredMarkerWithFallback(npc, markerType);
        marker.ifPresent(candidates::add);
    }

    private Optional<MarkerRecord> resolveMarkerInNpcWorld(NpcRecord npc, MarkerType markerType, String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return Optional.empty();
        }

        Optional<MarkerRecord> marker = markerRegistry.getById(markerId);
        if (marker.isEmpty()) {
            return Optional.empty();
        }

        if (marker.get().type() != markerType) {
            return Optional.empty();
        }

        if (!marker.get().worldId().equals(npc.worldId())) {
            return Optional.empty();
        }

        return marker;
    }

    private Optional<MarkerRecord> resolveRequiredMarkerWithFallback(NpcRecord npc, MarkerType markerType) {
        String assignedMarkerId = markerIdForType(npc, markerType);
        Optional<MarkerRecord> direct = resolveMarkerInNpcWorld(npc, markerType, assignedMarkerId);
        if (direct.isPresent()) {
            return direct;
        }

        String ringAnchorMarkerId = resolveRingFallbackAnchorMarkerId(npc, markerType, assignedMarkerId);
        Optional<MarkerRecord> fallback = markerRegistry.getNextAvailable(markerType, ringAnchorMarkerId, npc.worldId());
        if (fallback.isPresent()) {
            String oldMarkerId = assignedMarkerId;
            String newMarkerId = fallback.get().markerId();
            setMarkerIdForType(npc, markerType, newMarkerId);
            logInfo("MARKER_FALLBACK_SELECTED", "Resolved missing marker via ring fallback: "
                + "npcId=" + npc.npcId()
                + " npcName=" + quote(npc.npcName())
                + " markerType=" + markerType.name()
                + " oldMarkerId=" + nullToDash(oldMarkerId)
                + " ringAnchorMarkerId=" + nullToDash(ringAnchorMarkerId)
                + " newMarkerId=" + newMarkerId);
            return fallback;
        }

        return Optional.empty();
    }

    private String resolveRingFallbackAnchorMarkerId(NpcRecord npc, MarkerType markerType, String assignedMarkerId) {
        if (assignedMarkerId != null && !assignedMarkerId.isBlank()) {
            for (MarkerRecord candidate : markerRegistry.getCandidates(markerType, npc.worldId())) {
                if (assignedMarkerId.equals(candidate.markerId())) {
                    return assignedMarkerId;
                }
            }
        }

        Vec3 current = npc.currentPosition();
        if (current == null) {
            return assignedMarkerId;
        }

        List<MarkerRecord> candidates = markerRegistry.getCandidates(markerType, npc.worldId());
        if (candidates.isEmpty()) {
            return assignedMarkerId;
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

        return closest.markerId();
    }

    private void scheduleRespawnRetry(String npcId, String reason) {
        int failureCount = respawnFailureCounts.getOrDefault(npcId, 0) + 1;
        respawnFailureCounts.put(npcId, failureCount);

        NpcRecord npc = npcs.get(npcId);

        if (failureCount >= RESPAWN_MAX_FAILURES && !"world-missing".equals(reason)) {
            if (npc != null) {
                npcs.remove(npcId);
                clearRespawnFailureState(npcId);
                spawnRequestsInFlight.remove(npcId);

                logSevere("RESPAWN_HARD_CLEAN", "Removing NPC after repeated respawn failures: "
                    + spawnContext(npc, "tick-retry", null, null, null)
                    + " reason=" + reason + " failures=" + failureCount + " threshold=" + RESPAWN_MAX_FAILURES);
            }
            return;
        }

        long delay = Math.min(RESPAWN_RETRY_MAX_MS, RESPAWN_RETRY_BASE_MS << Math.min(5, failureCount - 1));
        long retryAt = System.currentTimeMillis() + delay;
        respawnRetryAtMs.put(npcId, retryAt);

        if (failureCount == 1 || failureCount % 10 == 0) {
            if (npc != null) {
                logSevere("RESPAWN_RETRY_SCHEDULED", "Respawn retry scheduled: "
                    + spawnContext(npc, "tick-retry", null, null, null)
                    + " reason=" + reason + " failures=" + failureCount + " delayMs=" + delay);
            } else {
                logSevere("RESPAWN_RETRY_SCHEDULED", "Respawn retry scheduled for unknown NPC "
                    + "npcId=" + npcId + " reason=" + reason + " failures=" + failureCount + " delayMs=" + delay);
            }
        }
    }

    private String staleReasonForRestore(NpcRecord npc) {
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            return "unknown-role roleId=" + npc.roleId();
        }

        for (MarkerType markerType : roleDefinition.get().requiredMarkers()) {
            String markerId = markerIdForType(npc, markerType);
            if (markerId == null || markerId.isBlank()) {
                return "missing-marker-id markerType=" + markerType;
            }

            Optional<MarkerRecord> marker = resolveRequiredMarkerWithFallback(npc, markerType);
            if (marker.isEmpty()) {
                return "missing-marker-record markerType=" + markerType + " markerId=" + markerId + " fallback=none";
            }
        }

        return null;
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
            + ",work=" + nullToDash(npc.workMarkerId()) + "}";
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

    private boolean hasActiveNavigation(NavigationState navState) {
        return navState.hasTarget();
    }

    private boolean isWalkingState(NpcState state) {
        return state == NpcState.WALKING_TO_BED
            || state == NpcState.WALKING_TO_WORK
            || state == NpcState.WALKING_TO_DOOR;
    }

    private void finishNavigation(NpcRecord npc, NavigationState navState) {
        Vec3 targetPos = navState.getTargetPosition();
        if (targetPos != null) {
            npc.currentPosition(targetPos);
            updateEntityPosition(npc, targetPos);
        }

        NpcState targetState = navState.getTargetState();
        if (targetState != null) {
            npc.state(targetState);
        }

        System.out.println("[KeystoneNPC] Navigation reached: npc=" + npc.npcName()
            + " reachedTarget=" + targetTypeForState(targetState)
            + " newState=" + npc.state());
        navState.clear();
    }

    private String targetTypeForState(NpcState state) {
        if (state == NpcState.WORKING) {
            return "WORK";
        }
        if (state == NpcState.SLEEPING) {
            return "BED";
        }
        return "UNKNOWN";
    }

    private boolean hasLiveEntity(NpcRecord npc) {
        Ref<EntityStore> entityRef = npc.entityRef();
        return entityRef != null && entityRef.isValid();
    }

    private void updateEntityPosition(NpcRecord npc, Vec3 newPosition) {
        Ref<EntityStore> entityRef = npc.entityRef();
        if (entityRef == null || !entityRef.isValid()) {
            System.err.println("[KeystoneNPC] Cannot move NPC '" + npc.npcName() + "' (" + npc.npcId()
                + "): missing or invalid EntityRef");
            return;
        }

        Store<EntityStore> store = entityRef.getStore();
        TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) {
            System.err.println("[KeystoneNPC] Cannot move NPC '" + npc.npcName() + "' (" + npc.npcId()
                + "): missing TransformComponent");
            return;
        }

        Vector3d newPos = new Vector3d(newPosition.x(), newPosition.y(), newPosition.z());
        transform.setPosition(newPos);
    }
}
