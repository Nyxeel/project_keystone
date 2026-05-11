package keystone.npc.relink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import keystone.npc.domain.NpcRecord;
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
    private final int uuidRelinkMaxMissesBeforeRespawn;
    private final long uuidRelinkMinWaitBeforeRespawnMs;
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
        int uuidRelinkMaxMissesBeforeRespawn,
        long uuidRelinkMinWaitBeforeRespawnMs,
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
        this.uuidRelinkMaxMissesBeforeRespawn = uuidRelinkMaxMissesBeforeRespawn;
        this.uuidRelinkMinWaitBeforeRespawnMs = uuidRelinkMinWaitBeforeRespawnMs;
        this.roleIdDedupeRadius = roleIdDedupeRadius;
        this.roleIdDedupeRadiusSq = roleIdDedupeRadiusSq;
        this.roleIdAnchorRelinkRadius = roleIdAnchorRelinkRadius;
        this.roleIdAnchorRelinkRadiusSq = roleIdAnchorRelinkRadiusSq;
        this.logInfoSink = logInfoSink;
        this.logSevereSink = logSevereSink;
        this.spawnContextFormatter = spawnContextFormatter;
    }

    public RelinkOutcome tryRelinkEntityRef(World world, NpcRecord npc, String trigger) {
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
                + spawnContextFormatter.format(npc, trigger, world, null, null)
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

            if (misses < uuidRelinkMaxMissesBeforeRespawn || waitedMs < uuidRelinkMinWaitBeforeRespawnMs) {
                if (misses == 1 || misses % 10 == 0) {
                    logInfo("RESPAWN_RELINK_PENDING", "Persisted UUID not found yet, deferring spawn: "
                        + spawnContextFormatter.format(npc, trigger, world, null, null)
                        + " entityUuid=" + rawUuid
                        + " misses=" + misses
                        + " threshold=" + uuidRelinkMaxMissesBeforeRespawn
                        + " waitedMs=" + waitedMs
                        + " minWaitMs=" + uuidRelinkMinWaitBeforeRespawnMs);
                }
                return RelinkOutcome.PENDING;
            }

            logSevere("RESPAWN_RELINK_GIVEUP", "Persisted UUID still not resolvable, allowing respawn fallback: "
                + spawnContextFormatter.format(npc, trigger, world, null, null)
                + " entityUuid=" + rawUuid
                + " misses=" + misses
                + " threshold=" + uuidRelinkMaxMissesBeforeRespawn
                + " waitedMs=" + waitedMs
                + " minWaitMs=" + uuidRelinkMinWaitBeforeRespawnMs);

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
                + spawnContextFormatter.format(npc, trigger, world, null, null)
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
                + spawnContextFormatter.format(npc, trigger, world, roleDefinition.orElse(null), expectedRoleIndex)
                + " liveRoleIndex=" + liveNpc.getRoleIndex()
                + " liveSpawnRoleIndex=" + liveNpc.getSpawnRoleIndex());
            npc.entityUuid(null);
            uuidRelinkMissCounts.remove(npc.npcId());
            uuidRelinkFirstMissAtMs.remove(npc.npcId());
            return RelinkOutcome.NO_MATCH;
        }

        npc.entityRef(relinkRef);
        npc.entityId(1);
        entitySync.updatePersistedEntityIdentity(npc, relinkRef);
        dedupeRoleIdDuplicates(world, npc, relinkRef, roleDefinition.orElse(null), expectedRoleIndex, trigger, "relink");
        idleMarkerService.enforceAuthoritativeIdlePosition(npc, "relink", true);
        uuidRelinkMissCounts.remove(npc.npcId());
        uuidRelinkFirstMissAtMs.remove(npc.npcId());

        logInfo("RESPAWN_RELINK_SUCCESS", "Re-linked persisted NPC to existing world entity: "
            + spawnContextFormatter.format(npc, trigger, world, roleDefinition.orElse(null), expectedRoleIndex));
        return RelinkOutcome.SUCCESS;
    }

    public boolean tryAnchorRelinkEntityRef(World world, NpcRecord npc, String trigger) {
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

                if (relinkSupport.distanceSq(center, transform.getPosition()) > roleIdAnchorRelinkRadiusSq) {
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

        Ref<EntityStore> keepRef = relinkSupport.findClosestRef(candidates, center);
        if (keepRef == null || !keepRef.isValid()) {
            return false;
        }

        npc.entityRef(keepRef);
        npc.entityId(1);
        entitySync.updatePersistedEntityIdentity(npc, keepRef);
        dedupeRoleIdDuplicates(world, npc, keepRef, roleDefinition.get(), roleIndex, trigger, "anchor-relink");
        idleMarkerService.enforceAuthoritativeIdlePosition(npc, "anchor-relink", true);

        uuidRelinkMissCounts.remove(npc.npcId());
        uuidRelinkFirstMissAtMs.remove(npc.npcId());

        logInfo("RESPAWN_RELINK_BY_ANCHOR", "Re-linked NPC via role+position fallback: "
            + spawnContextFormatter.format(npc, trigger, world, roleDefinition.get(), roleIndex)
            + " candidates=" + candidates.size()
            + " anchorRadius=" + roleIdAnchorRelinkRadius);
        return true;
    }

    public void dedupeRoleIdDuplicates(
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
        for (Ref<EntityStore> candidateRef : nearbyCandidates) {
            if (relinkSupport.sameRef(candidateRef, keepRef)) {
                continue;
            }

            if (candidateRef != null && candidateRef.isValid()) {
                candidateRef.getStore().removeEntity(candidateRef, RemoveReason.REMOVE);
                removed++;
                logSevere("DEDUPE_ROLEID_REMOVED", "Removed duplicate NPC entity by roleId proximity: "
                    + spawnContextFormatter.format(npc, trigger, world, roleDefinition, roleIndex)
                    + " source=" + source
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

    private void logInfo(String eventKey, String message) {
        logInfoSink.accept(eventKey, message);
    }

    private void logSevere(String eventKey, String message) {
        logSevereSink.accept(eventKey, message);
    }
}
