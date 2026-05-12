package keystone.npc.relink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcEntityStatus;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.entity.EntitySyncService;
import keystone.npc.routine.marker.IdleMarkerService;
import keystone.npc.routine.marker.MarkerResolver;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;

public final class RelinkWorkflowService {
    public enum RelinkOutcome {
        SUCCESS,
        PENDING,
        NO_MATCH
    }

    public enum RolePrefixRelinkOutcome {
        SUCCESS,
        NO_MATCH,
        AMBIGUOUS
    }

    public enum AnchorRelinkOutcome {
        SUCCESS,
        NO_MATCH,
        AMBIGUOUS
    }

    public record OwnedEntityRefClaim(String npcId, Ref<EntityStore> entityRef) {
    }

    public record RelinkEvaluationOutcome(
        RelinkOutcome outcome,
        Ref<EntityStore> candidateRef,
        String candidateUuid
    ) {
    }

    private record UuidResolveOutcome(
        Ref<EntityStore> entityRef,
        NPCEntity liveNpc,
        String liveUuid
    ) {
    }

    public record AnchorRelinkEvaluationOutcome(
        AnchorRelinkOutcome outcome,
        Ref<EntityStore> candidateRef,
        String candidateUuid
    ) {
    }

    public record RolePrefixRelinkEvaluationOutcome(
        RolePrefixRelinkOutcome outcome,
        Ref<EntityStore> candidateRef,
        String candidateUuid,
        int candidateCount
    ) {
    }

    @FunctionalInterface
    public interface SpawnContextFormatter {
        String format(NpcRecord npc, String trigger, World world, RoleDefinition roleDefinition, Integer roleIndex);
    }

    private final RoleDefinitionRegistry roleDefinitions;
    private final MarkerResolver markerResolver;
    private final EntitySyncService entitySync;
    private final IdleMarkerService idleMarkerService;
    private final RelinkSupport relinkSupport;
    private final Map<String, Integer> uuidRelinkMissCounts;
    private final Map<String, Long> uuidRelinkFirstMissAtMs;
    private final int relinkRetryCount;
    private final long relinkRetryDelayMs;
    private final double roleIdDedupeRadius;
    private final double roleIdDedupeRadiusSq;
    private final double roleIdAnchorRelinkRadius;
    private final double roleIdAnchorRelinkRadiusSq;
    private final BiConsumer<String, String> logInfoSink;
    private final BiConsumer<String, String> logSevereSink;
    private final SpawnContextFormatter spawnContextFormatter;

    public RelinkWorkflowService(
        RoleDefinitionRegistry roleDefinitions,
        MarkerResolver markerResolver,
        EntitySyncService entitySync,
        IdleMarkerService idleMarkerService,
        RelinkSupport relinkSupport,
        Map<String, Integer> uuidRelinkMissCounts,
        Map<String, Long> uuidRelinkFirstMissAtMs,
        int relinkRetryCount,
        long relinkRetryDelayMs,
        double roleIdDedupeRadius,
        double roleIdDedupeRadiusSq,
        double roleIdAnchorRelinkRadius,
        double roleIdAnchorRelinkRadiusSq,
        BiConsumer<String, String> logInfoSink,
        BiConsumer<String, String> logSevereSink,
        SpawnContextFormatter spawnContextFormatter
    ) {
        this.roleDefinitions = roleDefinitions;
        this.markerResolver = markerResolver;
        this.entitySync = entitySync;
        this.idleMarkerService = idleMarkerService;
        this.relinkSupport = relinkSupport;
        this.uuidRelinkMissCounts = uuidRelinkMissCounts;
        this.uuidRelinkFirstMissAtMs = uuidRelinkFirstMissAtMs;
        this.relinkRetryCount = Math.max(1, relinkRetryCount);
        this.relinkRetryDelayMs = Math.max(1L, relinkRetryDelayMs);
        this.roleIdDedupeRadius = roleIdDedupeRadius;
        this.roleIdDedupeRadiusSq = roleIdDedupeRadiusSq;
        this.roleIdAnchorRelinkRadius = roleIdAnchorRelinkRadius;
        this.roleIdAnchorRelinkRadiusSq = roleIdAnchorRelinkRadiusSq;
        this.logInfoSink = logInfoSink;
        this.logSevereSink = logSevereSink;
        this.spawnContextFormatter = spawnContextFormatter;
    }

    public RelinkOutcome tryRelinkEntityRef(
        World world,
        NpcRecord npc,
        String trigger,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        String rawUuid = npc.entityUuid();
        if (rawUuid == null || rawUuid.isBlank()) {
            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            logInfo("RELINK_PENDING", "No persisted entity UUID available: "
                + spawnContextFormatter.format(npc, trigger, world, null, null));
            return RelinkOutcome.PENDING;
        }

        String claimedByUuid = ownerByUuid(rawUuid, claimedEntityUuids);
        if (claimedByUuid != null && !claimedByUuid.equals(npc.npcId())) {
            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
            logSevere("RELINK_UUID_CLAIMED_BY_OTHER", "Persisted UUID belongs to another NPC record; marked missing: "
                + spawnContextFormatter.format(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid
                + " ownerNpcId=" + claimedByUuid);
            return RelinkOutcome.PENDING;
        }

        UUID entityUuid;
        try {
            entityUuid = UUID.fromString(rawUuid);
        } catch (IllegalArgumentException ex) {
            logSevere("RELINK_GIVEUP_MARKED_MISSING", "Invalid persisted entity UUID; marked missing: "
                + spawnContextFormatter.format(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid);
            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.PENDING;
        }

        UuidResolveOutcome resolved = resolveEntityRefByUuid(world, entityUuid);
        if (resolved == null) {
            long now = System.currentTimeMillis();
            long firstMissAt = uuidRelinkFirstMissAtMs.computeIfAbsent(npc.npcId(), key -> now);
            int misses = uuidRelinkMissCounts.getOrDefault(npc.npcId(), 0) + 1;
            uuidRelinkMissCounts.put(npc.npcId(), misses);
            npc.entityStatus(NpcEntityStatus.NEEDS_RELINK);

            if (misses == 1) {
                logInfo("RELINK_ATTEMPT", "Attempt relink by persisted UUID: "
                    + spawnContextFormatter.format(npc, trigger, world, null, null)
                    + " entityUuid=" + rawUuid);
            }

            long waitedMs = Math.max(0L, now - firstMissAt);
            long requiredWaitMs = relinkRetryCount * relinkRetryDelayMs;

            if (misses <= relinkRetryCount || waitedMs < requiredWaitMs) {
                if (misses == 1 || misses == relinkRetryCount) {
                    logInfo("RELINK_RETRY", "Entity UUID not found, retrying relink: "
                        + spawnContextFormatter.format(npc, trigger, world, null, null)
                        + " entityUuid=" + rawUuid
                        + " retry=" + misses
                        + " retryLimit=" + relinkRetryCount
                        + " waitedMs=" + waitedMs
                        + " requiredWaitMs=" + requiredWaitMs);
                }
                return RelinkOutcome.PENDING;
            }

            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
            logSevere("RELINK_GIVEUP_MARKED_MISSING", "Persisted UUID still not resolvable; marked missing: "
                + spawnContextFormatter.format(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid
                + " misses=" + misses
                + " retryLimit=" + relinkRetryCount
                + " waitedMs=" + waitedMs
                + " requiredWaitMs=" + requiredWaitMs);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.PENDING;
        }

        Ref<EntityStore> relinkRef = resolved.entityRef();
        NPCEntity liveNpc = resolved.liveNpc();
        String liveUuid = resolved.liveUuid();

        String claimedByRef = ownerByRef(relinkRef, claimedEntityRefs);
        if (claimedByRef != null && !claimedByRef.equals(npc.npcId())) {
            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
            logSevere("RELINK_REF_CLAIMED_BY_OTHER", "Persisted UUID resolves to entity claimed by another NPC record; marked missing: "
                + spawnContextFormatter.format(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid
                + " ownerNpcId=" + claimedByRef);
            return RelinkOutcome.PENDING;
        }

        String claimedByLiveUuid = ownerByUuid(liveUuid, claimedEntityUuids);
        if (claimedByLiveUuid != null && !claimedByLiveUuid.equals(npc.npcId())) {
            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
            logSevere("RELINK_UUID_CLAIMED_BY_OTHER", "Resolved live entity UUID belongs to another NPC record; marked missing: "
                + spawnContextFormatter.format(npc, trigger, world, null, null)
                + " persistedEntityUuid=" + rawUuid
                + " liveEntityUuid=" + liveUuid
                + " ownerNpcId=" + claimedByLiveUuid);
            return RelinkOutcome.PENDING;
        }

        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        int expectedRoleIndex = roleDefinition
            .map(definition -> NPCPlugin.get().getIndex(definition.npcPluginRoleName()))
            .orElse(-1);

        if (expectedRoleIndex >= 0
            && liveNpc.getRoleIndex() != expectedRoleIndex
            && liveNpc.getSpawnRoleIndex() != expectedRoleIndex) {
            npc.entityStatus(NpcEntityStatus.MISSING_ENTITY);
            logSevere("RELINK_GIVEUP_MARKED_MISSING", "Persisted UUID points to wrong role entity; marked missing: "
                + spawnContextFormatter.format(npc, trigger, world, roleDefinition.orElse(null), expectedRoleIndex)
                + " liveRoleIndex=" + liveNpc.getRoleIndex()
                + " liveSpawnRoleIndex=" + liveNpc.getSpawnRoleIndex());
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.PENDING;
        }

        npc.entityRef(relinkRef);
        npc.entityId(1);
        npc.entityStatus(NpcEntityStatus.ACTIVE);
        normalizeRuntimeRoleName(npc, relinkRef, liveNpc, world, trigger, "uuid-relink");
        entitySync.updatePersistedEntityIdentity(npc, relinkRef);
        dedupeRoleIdDuplicates(
            world,
            npc,
            relinkRef,
            roleDefinition.orElse(null),
            expectedRoleIndex,
            trigger,
            "relink",
            claimedEntityUuids,
            claimedEntityRefs
        );
        idleMarkerService.enforceAuthoritativeIdlePosition(npc, "relink", true);
        uuidRelinkMissCounts.remove(npc.npcId());
        uuidRelinkFirstMissAtMs.remove(npc.npcId());

        logInfo("RELINK_SUCCESS", "Re-linked persisted NPC to existing world entity: "
            + spawnContextFormatter.format(npc, trigger, world, roleDefinition.orElse(null), expectedRoleIndex));
        return RelinkOutcome.SUCCESS;
    }

    public RelinkOutcome evaluateRelinkEntityRef(
        World world,
        NpcRecord npc,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        return evaluateRelinkEntityRefDetailed(world, npc, claimedEntityUuids, claimedEntityRefs).outcome();
    }

    public RelinkEvaluationOutcome evaluateRelinkEntityRefDetailed(
        World world,
        NpcRecord npc,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        String rawUuid = npc.entityUuid();
        if (rawUuid == null || rawUuid.isBlank()) {
            return new RelinkEvaluationOutcome(RelinkOutcome.PENDING, null, null);
        }

        String claimedByUuid = ownerByUuid(rawUuid, claimedEntityUuids);
        if (claimedByUuid != null && !claimedByUuid.equals(npc.npcId())) {
            return new RelinkEvaluationOutcome(RelinkOutcome.PENDING, null, null);
        }

        UUID entityUuid;
        try {
            entityUuid = UUID.fromString(rawUuid);
        } catch (IllegalArgumentException ex) {
            return new RelinkEvaluationOutcome(RelinkOutcome.PENDING, null, null);
        }

        UuidResolveOutcome resolved = resolveEntityRefByUuid(world, entityUuid);
        if (resolved == null) {
            return new RelinkEvaluationOutcome(RelinkOutcome.PENDING, null, null);
        }

        Ref<EntityStore> relinkRef = resolved.entityRef();
        NPCEntity liveNpc = resolved.liveNpc();
        String liveUuid = resolved.liveUuid();

        String claimedByRef = ownerByRef(relinkRef, claimedEntityRefs);
        if (claimedByRef != null && !claimedByRef.equals(npc.npcId())) {
            return new RelinkEvaluationOutcome(RelinkOutcome.PENDING, null, null);
        }

        String claimedByLiveUuid = ownerByUuid(liveUuid, claimedEntityUuids);
        if (claimedByLiveUuid != null && !claimedByLiveUuid.equals(npc.npcId())) {
            return new RelinkEvaluationOutcome(RelinkOutcome.PENDING, null, null);
        }

        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        int expectedRoleIndex = roleDefinition
            .map(definition -> NPCPlugin.get().getIndex(definition.npcPluginRoleName()))
            .orElse(-1);

        if (expectedRoleIndex >= 0
            && liveNpc.getRoleIndex() != expectedRoleIndex
            && liveNpc.getSpawnRoleIndex() != expectedRoleIndex) {
            return new RelinkEvaluationOutcome(RelinkOutcome.PENDING, null, null);
        }

        return new RelinkEvaluationOutcome(RelinkOutcome.SUCCESS, relinkRef, liveUuid);
    }

    public RolePrefixRelinkOutcome tryRolePrefixRelinkEntityRef(
        World world,
        NpcRecord npc,
        String trigger,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        return RolePrefixRelinkOutcome.NO_MATCH;
    }

    public RolePrefixRelinkOutcome evaluateRolePrefixRelinkEntityRef(
        World world,
        NpcRecord npc,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        return evaluateRolePrefixRelinkEntityRefDetailed(world, npc, claimedEntityUuids, claimedEntityRefs).outcome();
    }

    public RolePrefixRelinkEvaluationOutcome evaluateRolePrefixRelinkEntityRefDetailed(
        World world,
        NpcRecord npc,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        return new RolePrefixRelinkEvaluationOutcome(RolePrefixRelinkOutcome.NO_MATCH, null, null, 0);
    }

    private UuidResolveOutcome resolveEntityRefByUuid(World world, UUID entityUuid) {
        if (world == null || entityUuid == null) {
            return null;
        }

        Ref<EntityStore> relinkRef = resolveEntityRefViaWorldApi(world, entityUuid);
        if ((relinkRef == null || !relinkRef.isValid()) && world.getEntityStore() != null) {
            relinkRef = world.getEntityStore().getRefFromUUID(entityUuid);
        }

        if (relinkRef == null || !relinkRef.isValid()) {
            return null;
        }

        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return null;
        }

        NPCEntity liveNpc = relinkRef.getStore().getComponent(relinkRef, npcType);
        if (liveNpc == null) {
            return null;
        }

        String liveUuid = readEntityUuid(relinkRef);
        if (liveUuid == null || liveUuid.isBlank()) {
            return null;
        }

        if (!liveUuid.equalsIgnoreCase(entityUuid.toString())) {
            return null;
        }

        return new UuidResolveOutcome(relinkRef, liveNpc, liveUuid);
    }

    private Ref<EntityStore> resolveEntityRefViaWorldApi(World world, UUID entityUuid) {
        Ref<EntityStore> refByUuid = invokeWorldGetEntityRef(world, entityUuid, UUID.class);
        if (refByUuid != null && refByUuid.isValid()) {
            return refByUuid;
        }

        Ref<EntityStore> refByString = invokeWorldGetEntityRef(world, entityUuid.toString(), String.class);
        if (refByString != null && refByString.isValid()) {
            return refByString;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> Ref<EntityStore> invokeWorldGetEntityRef(World world, T arg, Class<T> argType) {
        try {
            Method method = world.getClass().getMethod("getEntityRef", argType);
            Object result = method.invoke(world, arg);
            if (result instanceof Ref<?> genericRef) {
                return (Ref<EntityStore>) genericRef;
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // API variant not available on this runtime; caller will use fallback.
        }
        return null;
    }

    public AnchorRelinkOutcome tryAnchorRelinkEntityRef(
        World world,
        NpcRecord npc,
        String trigger,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            return AnchorRelinkOutcome.NO_MATCH;
        }

        int roleIndex = NPCPlugin.get().getIndex(roleDefinition.get().npcPluginRoleName());
        if (roleIndex < 0) {
            return AnchorRelinkOutcome.NO_MATCH;
        }

        Vec3 center = npc.currentPosition();
        if (center == null) {
            return AnchorRelinkOutcome.NO_MATCH;
        }
        List<Vec3> anchors = collectDedupeAnchors(npc, center);

        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return AnchorRelinkOutcome.NO_MATCH;
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

                if (!relinkSupport.isNearAnyDedupeAnchor(anchors, transform.getPosition(), roleIdAnchorRelinkRadiusSq)) {
                    continue;
                }

                Ref<EntityStore> candidateRef = archetypeChunk.getReferenceTo(index);
                if (candidateRef == null || !candidateRef.isValid()) {
                    continue;
                }

                String ownerByRef = ownerByRef(candidateRef, claimedEntityRefs);
                if (ownerByRef != null && !ownerByRef.equals(npc.npcId())) {
                    logInfo("RELINK_ANCHOR_SKIPPED_CLAIMED", "Skipping anchor relink candidate claimed by another NPC: "
                        + spawnContextFormatter.format(npc, trigger, world, roleDefinition.get(), roleIndex)
                        + " ownerNpcId=" + ownerByRef);
                    continue;
                }

                String candidateUuid = readEntityUuid(candidateRef);
                String ownerByUuid = ownerByUuid(candidateUuid, claimedEntityUuids);
                if (ownerByUuid != null && !ownerByUuid.equals(npc.npcId())) {
                    logInfo("RELINK_ANCHOR_SKIPPED_CLAIMED", "Skipping anchor relink candidate UUID claimed by another NPC: "
                        + spawnContextFormatter.format(npc, trigger, world, roleDefinition.get(), roleIndex)
                        + " candidateUuid=" + candidateUuid
                        + " ownerNpcId=" + ownerByUuid);
                    continue;
                }

                candidates.add(candidateRef);
            }
        });

        if (candidates.isEmpty()) {
            return AnchorRelinkOutcome.NO_MATCH;
        }

        if (candidates.size() > 1) {
            logSevere("RELINK_ANCHOR_AMBIGUOUS", "Anchor relink found multiple ownership-safe candidates; skipping relink: "
                + spawnContextFormatter.format(npc, trigger, world, roleDefinition.get(), roleIndex)
                + " candidates=" + candidates.size()
                + " anchorRadius=" + roleIdAnchorRelinkRadius);
            return AnchorRelinkOutcome.AMBIGUOUS;
        }

        Ref<EntityStore> keepRef = candidates.get(0);
        if (keepRef == null || !keepRef.isValid()) {
            return AnchorRelinkOutcome.NO_MATCH;
        }

        npc.entityRef(keepRef);
        npc.entityId(1);
        npc.entityStatus(NpcEntityStatus.ACTIVE);
        normalizeRuntimeRoleName(npc, keepRef, null, world, trigger, "anchor-relink");
        entitySync.updatePersistedEntityIdentity(npc, keepRef);
        dedupeRoleIdDuplicates(
            world,
            npc,
            keepRef,
            roleDefinition.get(),
            roleIndex,
            trigger,
            "anchor-relink",
            claimedEntityUuids,
            claimedEntityRefs
        );
        idleMarkerService.enforceAuthoritativeIdlePosition(npc, "anchor-relink", true);

        uuidRelinkMissCounts.remove(npc.npcId());
        uuidRelinkFirstMissAtMs.remove(npc.npcId());

        logInfo("RELINK_ANCHOR_UNIQUE_MATCH", "Anchor relink selected unique ownership-safe candidate: "
            + spawnContextFormatter.format(npc, trigger, world, roleDefinition.get(), roleIndex)
            + " anchors=" + anchors.size()
            + " anchorRadius=" + roleIdAnchorRelinkRadius);
        return AnchorRelinkOutcome.SUCCESS;
    }

    public AnchorRelinkOutcome evaluateAnchorRelinkEntityRef(
        World world,
        NpcRecord npc,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        return evaluateAnchorRelinkEntityRefDetailed(world, npc, claimedEntityUuids, claimedEntityRefs).outcome();
    }

    public AnchorRelinkEvaluationOutcome evaluateAnchorRelinkEntityRefDetailed(
        World world,
        NpcRecord npc,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.NO_MATCH, null, null);
        }

        int roleIndex = NPCPlugin.get().getIndex(roleDefinition.get().npcPluginRoleName());
        if (roleIndex < 0) {
            return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.NO_MATCH, null, null);
        }

        Vec3 center = npc.currentPosition();
        if (center == null) {
            return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.NO_MATCH, null, null);
        }
        List<Vec3> anchors = collectDedupeAnchors(npc, center);

        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.NO_MATCH, null, null);
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

                if (!relinkSupport.isNearAnyDedupeAnchor(anchors, transform.getPosition(), roleIdAnchorRelinkRadiusSq)) {
                    continue;
                }

                Ref<EntityStore> candidateRef = archetypeChunk.getReferenceTo(index);
                if (candidateRef == null || !candidateRef.isValid()) {
                    continue;
                }

                String ownerByRef = ownerByRef(candidateRef, claimedEntityRefs);
                if (ownerByRef != null && !ownerByRef.equals(npc.npcId())) {
                    continue;
                }

                String candidateUuid = readEntityUuid(candidateRef);
                String ownerByUuid = ownerByUuid(candidateUuid, claimedEntityUuids);
                if (ownerByUuid != null && !ownerByUuid.equals(npc.npcId())) {
                    continue;
                }

                candidates.add(candidateRef);
            }
        });

        if (candidates.isEmpty()) {
            return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.NO_MATCH, null, null);
        }

        if (candidates.size() > 1) {
            return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.AMBIGUOUS, null, null);
        }

        Ref<EntityStore> keepRef = candidates.get(0);
        if (keepRef == null || !keepRef.isValid()) {
            return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.NO_MATCH, null, null);
        }

        return new AnchorRelinkEvaluationOutcome(AnchorRelinkOutcome.SUCCESS, keepRef, readEntityUuid(keepRef));
    }

    public void dedupeRoleIdDuplicates(
        World world,
        NpcRecord npc,
        Ref<EntityStore> preferredKeepRef,
        RoleDefinition roleDefinition,
        int roleIndex,
        String trigger,
        String source,
        Map<String, String> claimedEntityUuids,
        List<OwnedEntityRefClaim> claimedEntityRefs
    ) {
        if (roleIndex < 0) {
            return;
        }

        Vec3 tempCenter = npc.currentPosition();
        if (tempCenter == null) {
            tempCenter = entitySync.readPosition(preferredKeepRef);
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

                if (!relinkSupport.isNearAnyDedupeAnchor(dedupeAnchors, transform.getPosition(), roleIdDedupeRadiusSq)) {
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
            || !relinkSupport.containsRef(nearbyCandidates, keepRef)
            || !relinkSupport.isRefNearCenter(keepRef, center, roleIdDedupeRadiusSq)) {
            keepRef = relinkSupport.findClosestRef(nearbyCandidates, center);
        }

        if (keepRef == null || !keepRef.isValid()) {
            return;
        }

        int removed = 0;
        int skippedOwned = 0;
        int skippedAmbiguous = 0;
        int candidateUnclaimed = 0;
        for (Ref<EntityStore> candidateRef : nearbyCandidates) {
            if (relinkSupport.sameRef(candidateRef, keepRef)) {
                continue;
            }

            if (candidateRef != null && candidateRef.isValid()) {
                String candidateUuid = readEntityUuid(candidateRef);
                String ownerByRef = ownerByRef(candidateRef, claimedEntityRefs);
                String ownerByUuid = ownerByUuid(candidateUuid, claimedEntityUuids);
                boolean ownedByOther = (ownerByRef != null && !ownerByRef.equals(npc.npcId()))
                    || (ownerByUuid != null && !ownerByUuid.equals(npc.npcId()));
                boolean provenSameRecordDuplicate = (ownerByRef != null && ownerByRef.equals(npc.npcId()))
                    || (ownerByUuid != null && ownerByUuid.equals(npc.npcId()));

                if (ownedByOther) {
                    skippedOwned++;
                    logInfo("DEDUPE_ROLEID_SKIPPED_OWNED", "Skipped dedupe removal for claimed NPC entity: "
                        + spawnContextFormatter.format(npc, trigger, world, roleDefinition, roleIndex)
                        + " source=" + source
                        + " candidateUuid=" + nullToDash(candidateUuid)
                        + " ownerByRef=" + nullToDash(ownerByRef)
                        + " ownerByUuid=" + nullToDash(ownerByUuid));
                    continue;
                }

                if (!provenSameRecordDuplicate) {
                    if (ownerByRef == null && ownerByUuid == null) {
                        candidateUnclaimed++;
                        logInfo("DEDUPE_ROLEID_CANDIDATE_UNCLAIMED", "Unclaimed dedupe candidate detected; no destructive action taken: "
                            + spawnContextFormatter.format(npc, trigger, world, roleDefinition, roleIndex)
                            + " source=" + source
                            + " candidateUuid=" + nullToDash(candidateUuid)
                            + " radius=" + roleIdDedupeRadius);
                    }

                    skippedAmbiguous++;
                    logInfo("DEDUPE_ROLEID_SKIPPED_AMBIGUOUS", "Skipped dedupe candidate because ownership proof is ambiguous: "
                        + spawnContextFormatter.format(npc, trigger, world, roleDefinition, roleIndex)
                        + " source=" + source
                        + " candidateUuid=" + nullToDash(candidateUuid)
                        + " ownerByRef=" + nullToDash(ownerByRef)
                        + " ownerByUuid=" + nullToDash(ownerByUuid));
                    continue;
                }

                candidateRef.getStore().removeEntity(candidateRef, RemoveReason.REMOVE);
                removed++;
                logSevere("DEDUPE_ROLEID_REMOVED", "Removed proven same-record duplicate NPC entity: "
                    + spawnContextFormatter.format(npc, trigger, world, roleDefinition, roleIndex)
                    + " source=" + source
                    + " candidateUuid=" + nullToDash(candidateUuid)
                    + " radius=" + roleIdDedupeRadius);
            }
        }

        npc.entityRef(keepRef);
        npc.entityId(1);
        entitySync.updatePersistedEntityIdentity(npc, keepRef);
        idleMarkerService.enforceAuthoritativeIdlePosition(npc, "dedupe-" + source, true);

        if (removed > 0) {
            logSevere("DEDUPE_ROLEID_SUMMARY", "RoleId dedupe removed duplicates for NPC slot: "
                + spawnContextFormatter.format(npc, trigger, world, roleDefinition, roleIndex)
                + " source=" + source
                + " removed=" + removed
                + " skippedOwned=" + skippedOwned
            + " skippedAmbiguous=" + skippedAmbiguous
            + " candidateUnclaimed=" + candidateUnclaimed
                + " candidates=" + nearbyCandidates.size()
                + " radius=" + roleIdDedupeRadius);
        } else if (skippedOwned > 0 || skippedAmbiguous > 0 || candidateUnclaimed > 0) {
            logInfo("DEDUPE_ROLEID_SUMMARY", "RoleId dedupe skipped owned candidates without removal: "
                + spawnContextFormatter.format(npc, trigger, world, roleDefinition, roleIndex)
                + " source=" + source
                + " removed=" + removed
                + " skippedOwned=" + skippedOwned
            + " skippedAmbiguous=" + skippedAmbiguous
            + " candidateUnclaimed=" + candidateUnclaimed
                + " candidates=" + nearbyCandidates.size()
                + " radius=" + roleIdDedupeRadius);
        }
    }

    private List<Vec3> collectDedupeAnchors(NpcRecord npc, Vec3 center) {
        List<Vec3> anchors = new ArrayList<>();
        anchors.add(center);

        Optional<MarkerType> authoritativeMarkerType = markerResolver.resolveAuthoritativeMarkerType(npc.state());
        if (authoritativeMarkerType.isPresent()) {
            addMarkerAnchor(anchors, npc, authoritativeMarkerType.get());
            return anchors;
        }

        Optional<MarkerRecord> preferred = idleMarkerService.resolveStatePreferredMarker(npc);
        preferred.ifPresent(marker -> anchors.add(marker.position()));

        return anchors;
    }

    private void addMarkerAnchor(List<Vec3> anchors, NpcRecord npc, MarkerType markerType) {
        Optional<MarkerRecord> marker = markerResolver.resolveRequiredMarkerWithFallback(npc, markerType);
        marker.ifPresent(value -> anchors.add(value.position()));
    }

    private String ownerByRef(Ref<EntityStore> candidateRef, List<OwnedEntityRefClaim> claims) {
        if (candidateRef == null || claims == null || claims.isEmpty()) {
            return null;
        }

        for (OwnedEntityRefClaim claim : claims) {
            if (claim == null || claim.entityRef() == null) {
                continue;
            }
            if (relinkSupport.sameRef(claim.entityRef(), candidateRef)) {
                return claim.npcId();
            }
        }
        return null;
    }

    private String ownerByUuid(String candidateUuid, Map<String, String> claimedEntityUuids) {
        if (candidateUuid == null || candidateUuid.isBlank() || claimedEntityUuids == null || claimedEntityUuids.isEmpty()) {
            return null;
        }
        return claimedEntityUuids.get(candidateUuid);
    }

    private String readEntityUuid(Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid()) {
            return null;
        }

        UUIDComponent uuidComponent = entityRef.getStore().getComponent(entityRef, UUIDComponent.getComponentType());
        if (uuidComponent == null || uuidComponent.getUuid() == null) {
            return null;
        }

        return uuidComponent.getUuid().toString();
    }

    private void normalizeRuntimeRoleName(
        NpcRecord npc,
        Ref<EntityStore> entityRef,
        NPCEntity resolvedLiveNpc,
        World world,
        String trigger,
        String source
    ) {
        if (npc == null || entityRef == null || !entityRef.isValid()) {
            return;
        }

        // Runtime role normalization is intentionally disabled.
        // Engine role names must remain static roles that exist in Server/NPC/Roles.
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void logInfo(String eventKey, String message) {
        logInfoSink.accept(eventKey, message);
    }

    private void logSevere(String eventKey, String message) {
        logSevereSink.accept(eventKey, message);
    }
}
