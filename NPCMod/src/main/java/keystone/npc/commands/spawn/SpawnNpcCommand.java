package keystone.npc.commands.spawn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.domain.MarkerAssignment;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.ActiveSpawnMarkerStore;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;

public final class SpawnNpcCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> npcIdArg = this.withRequiredArg("npcId", "keystone.commands.knpc.spawn.role", ArgTypes.STRING);

    private final OptionalArg<String> nameArg = this.withOptionalArg("name", "keystone.commands.knpc.spawn.name", ArgTypes.STRING);

    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final ActiveSpawnMarkerStore activeSpawnMarkerStore;
    private final RoleDefinitionRegistry roleDefinitions;
    private final NpcTemplateResolver templateResolver;
    private final RequiredMarkerResolver requiredMarkerResolver;
    private final NpcRoutineRunner scheduler;

    public SpawnNpcCommand(
        KeystoneNpcPlugin plugin,
        MarkerRegistry markerRegistry,
        RoleDefinitionRegistry roleDefinitions,
        NpcTemplateResolver templateResolver,
        NpcRoutineRunner scheduler
    ) {
        super("spawn", "keystone.commands.knpc.spawn");
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.activeSpawnMarkerStore = this.plugin.activeSpawnMarkerStore();
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.templateResolver = Objects.requireNonNull(templateResolver);
        this.requiredMarkerResolver = new RequiredMarkerResolver(this.templateResolver, this.roleDefinitions);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        String requestedId = RoleDefinition.normalizeRoleId(npcIdArg.get(context));
        List<String> invalidRoleReasons = roleDefinitions.invalidRoleReasons(requestedId);
        if (!invalidRoleReasons.isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Role " + requestedId + " is invalid:"));
            for (String reason : invalidRoleReasons) {
                context.sendMessage(Message.raw("- " + reason));
            }
            return;
        }

        RoleDefinition role = roleDefinitions.findByRoleId(requestedId).orElse(null);
        if (role == null) {
            context.sendMessage(Message.raw("[knpc] Unknown role: '" + requestedId + "'. Known roles: "
                + String.join(", ", roleDefinitions.knownRoleIds())));
            return;
        }

        if (templateResolver.resolveByRoleId(role.roleId()).isEmpty()) {
            context.sendMessage(Message.raw("[knpc] NPC definition not loadable for role '" + role.roleId() + "'."));
            return;
        }

        if (templateResolver.resolveRoutineByRoleId(role.roleId()).isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            context.sendMessage(Message.raw("[knpc] No routine loaded for role '" + role.roleId() + "'."));
            return;
        }

        int resolvedRoleIndex = NPCPlugin.get().getIndex(role.npcPluginRoleName());
        if (resolvedRoleIndex < 0) {
            context.sendMessage(Message.raw("Cannot spawn role " + role.roleId() + "."));
            context.sendMessage(Message.raw("Resolved NPCPlugin role '" + role.npcPluginRoleName() + "' was not found."));
            return;
        }

        List<RequiredMarkerResolver.Requirement> requirements = requiredMarkerResolver.resolveRequirements(role.roleId());
        if (requirements.isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            context.sendMessage(Message.raw("[knpc] No role JSON loaded for role '" + role.roleId() + "'. Cannot resolve requiredMarkers."));
            return;
        }

        System.out.println("[KeystoneNPC] Checking required role-staging markers for role " + role.roleId() + ": "
            + formatRequiredMarkersForLog(requirements));

        RoleStagingResolveResult stagingResolveResult = resolveRoleStagingForRequiredMarkers(role.roleId(), world.getName(), requirements);
        if (stagingResolveResult.blocked()) {
            System.err.println("[KeystoneNPC][SPAWN_ABORT_ROLE_STAGING_MISSING_OR_INVALID] roleId=" + role.roleId()
                + " missing=" + String.join(",", stagingResolveResult.missingMarkers())
                + " invalid=" + String.join(",", stagingResolveResult.invalidMarkers())
                + " conflicts=" + String.join(",", stagingResolveResult.sharedMarkerConflicts())
                + " worldMismatch=" + String.join(",", stagingResolveResult.worldMismatchMarkers()));
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            if (!stagingResolveResult.invalidMarkers().isEmpty()) {
                context.sendMessage(Message.raw("[knpc] Invalid required markers:"));
                for (String invalidMarker : stagingResolveResult.invalidMarkers()) {
                    context.sendMessage(Message.raw("- " + invalidMarker));
                }
                context.sendMessage(Message.raw("[knpc] Supported markers: BED, DOOR, CHEST, FOOD, WORK, CHILL"));
            }
            if (!stagingResolveResult.missingMarkers().isEmpty()) {
                context.sendMessage(Message.raw("[knpc] Missing required role-staged markers:"));
                for (String missingMarker : stagingResolveResult.missingMarkers()) {
                    context.sendMessage(Message.raw("- " + missingMarker));
                }
            }
            if (!stagingResolveResult.worldMismatchMarkers().isEmpty()) {
                context.sendMessage(Message.raw("[knpc] Role-staged markers are bound to another world:"));
                for (String mismatchMarker : stagingResolveResult.worldMismatchMarkers()) {
                    context.sendMessage(Message.raw("- " + mismatchMarker));
                }
            }
            if (!stagingResolveResult.sharedMarkerConflicts().isEmpty()) {
                context.sendMessage(Message.raw("[knpc] Shared markers are not supported in role staging:"));
                for (String conflict : stagingResolveResult.sharedMarkerConflicts()) {
                    context.sendMessage(Message.raw("- " + conflict));
                }
            }
            return;
        }

        StagingOwnershipPrecheckResult stagingPrecheckResult = stagingOwnershipPrecheck(stagingResolveResult.requiredMarkers());
        if (stagingPrecheckResult.blocked()) {
            System.err.println("[KeystoneNPC][SPAWN_ABORT_ROLE_STAGING_OWNERSHIP_CONFLICT] roleId=" + role.roleId()
                + " conflicts=" + stagingPrecheckResult.conflicts().size());
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            context.sendMessage(Message.raw("[knpc] Role-staging ownership precheck failed: staged marker already owned by another NPC."));
            context.sendMessage(Message.raw("[knpc] Shared markers are not supported. Resolve conflicting staged markers first."));
            for (StagingOwnershipConflict conflict : stagingPrecheckResult.conflicts()) {
                OwnershipHit owner = conflict.owner();
                context.sendMessage(Message.raw("- markerName='" + conflict.markerName() + "' markerId=" + conflict.markerId()
                    + " already assigned to npc='" + owner.ownerNpcName() + "'"
                    + " (npcId=" + owner.ownerNpcId()
                    + ", role=" + owner.ownerRoleId()
                    + ", key=" + owner.ownerLogicalMarkerName() + ")"));
            }
            return;
        }

        String requestedName = context.provided(nameArg) ? nameArg.get(context) : role.npcPluginRoleName();
        String normalizedName = normalizeNpcName(requestedName);
        if (normalizedName == null) {
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            context.sendMessage(Message.raw("[knpc] Invalid npcName: name must not be blank."));
            return;
        }

        if (npcNameExists(normalizedName)) {
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            context.sendMessage(Message.raw("[knpc] npcName already exists (case-insensitive): '" + requestedName.trim() + "'."));
            context.sendMessage(Message.raw("[knpc] Choose a unique npcName before spawn."));
            return;
        }

        String name = requestedName.trim();

        var transformType = TransformComponent.getComponentType();
        if (transformType == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position."));
            return;
        }

        TransformComponent transform = store.getComponent(ref, transformType);
        if (transform == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position."));
            return;
        }

        Vector3d playerPos = transform.getPosition();
        var playerRotation = transform.getRotation();

        Vector3d forward = new Vector3d(0, 0, -2);
        playerRotation.transform(forward);
        Vector3d spawnPos = new Vector3d(playerPos).add(forward);
        if (!Double.isFinite(spawnPos.x()) || !Double.isFinite(spawnPos.y()) || !Double.isFinite(spawnPos.z())) {
            context.sendMessage(Message.raw("[knpc] Invalid spawn position."));
            return;
        }

        String npcId = java.util.UUID.randomUUID().toString();
        var npc = scheduler.spawnNpcWithEntity(
            npcId,
            name,
            role.roleId(),
            new WorldId(world.getName()),
            new Vec3(spawnPos.x(), spawnPos.y(), spawnPos.z()),
            world,
            "command-spawn"
        );
        if (npc == null) {
            context.sendMessage(Message.raw("[knpc] Failed to spawn NPC for role '" + role.roleId() + "'."));
            return;
        }

        List<String> insertedMarkerIds = materializeStagedMarkersIntoRegistry(stagingResolveResult.requiredMarkers());

        npc.markerAssignments(stagingResolveResult.markerAssignments());
        if (!allAssignedMarkerIdsPresent(npc.markerAssignments())) {
            rollbackInsertedMarkerRecords(insertedMarkerIds);

            NpcRoutineRunner.RemoveNpcResult removeResult = scheduler.removeNpcDetailedById(npc.npcId());
            System.err.println("[KeystoneNPC][SPAWN_ABORT_ASSIGNMENT_MARKER_MISSING_IN_REGISTRY] npcId=" + npc.npcId()
                + " entityRemovalOutcome=" + removeResult.entityRemovalOutcome()
                + " removed=" + removeResult.removed());
            context.sendMessage(Message.raw("[knpc] Spawn aborted: marker assignment references marker IDs that are missing in MarkerRegistry."));
            context.sendMessage(Message.raw("[knpc] Runtime rollback attempted (entityRemovalOutcome=" + removeResult.entityRemovalOutcome() + ")."));
            return;
        }

        if (!plugin.saveStateSafely()) {
            rollbackInsertedMarkerRecords(insertedMarkerIds);

            NpcRoutineRunner.RemoveNpcResult removeResult = scheduler.removeNpcDetailedById(npc.npcId());
            if (removeResult.removed()) {
                System.err.println("[KeystoneNPC][SPAWN_ROLLBACK_COMPLETED_AFTER_SAVE_FAILURE] Spawn rollback completed after save failure: "
                    + "npcId=" + npc.npcId()
                    + " entityRemovalOutcome=" + removeResult.entityRemovalOutcome());
                context.sendMessage(Message.raw("[knpc] Spawn aborted: state persistence failed. Runtime rollback completed."
                    + " (entityRemovalOutcome=" + removeResult.entityRemovalOutcome() + ")"));
            } else if (removeResult.found()) {
                System.err.println("[KeystoneNPC][SPAWN_ROLLBACK_BLOCKED_AFTER_SAVE_FAILURE] Spawn rollback was blocked after save failure: "
                    + "npcId=" + npc.npcId()
                    + " entityRemovalOutcome=" + removeResult.entityRemovalOutcome()
                    + " message=" + removeResult.message());
                context.sendMessage(Message.raw("[knpc] Spawn aborted: state persistence failed and rollback was blocked."
                    + " (entityRemovalOutcome=" + removeResult.entityRemovalOutcome()
                    + ", reason=" + removeResult.message() + ")"));
                context.sendMessage(Message.raw("[knpc] Orphan risk: spawned entity may still exist without persisted NPC record."));
            } else {
                System.err.println("[KeystoneNPC][SPAWN_ROLLBACK_UNKNOWN_AFTER_SAVE_FAILURE] Spawn rollback status unknown after save failure: "
                    + "npcId=" + npc.npcId()
                    + " message=" + removeResult.message());
                context.sendMessage(Message.raw("[knpc] Spawn aborted: state persistence failed and rollback status is unknown."
                    + " (reason=" + removeResult.message() + ")"));
                context.sendMessage(Message.raw("[knpc] Orphan risk: spawned entity may still exist without persisted NPC record."));
            }
            return;
        }

        activeSpawnMarkerStore.clearRole(role.roleId());

        context.sendMessage(Message.raw("[knpc] Spawned " + role.roleId() + " '" + npc.npcName()
            + "' (id=" + npc.npcId() + ")."));

        if (NpcDebugSupport.showMarkersEnabled(templateResolver, npc.roleId())) {
            for (String line : NpcDebugSupport.buildMarkerSnapshotLines(npc, markerRegistry, templateResolver, roleDefinitions)) {
                if (line == null) {
                    continue;
                }
                context.sendMessage(Message.raw(line));
            }

            for (String missing : NpcDebugSupport.missingRequiredMarkerNames(npc, markerRegistry, templateResolver, roleDefinitions)) {
                context.sendMessage(Message.raw("[KNPC][Warning] " + npc.npcName()
                    + ": required marker " + missing + " fehlt"));
            }
        }
    }

    private String formatRequiredMarkersForLog(List<RequiredMarkerResolver.Requirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return "none";
        }

        List<String> values = new ArrayList<>(requirements.size());
        for (RequiredMarkerResolver.Requirement requirement : requirements) {
            MarkerType markerType = requirement.markerType();
            values.add(markerType != null
                ? markerType.name()
                : requirement.name().toUpperCase(Locale.ROOT));
        }
        return String.join(", ", values);
    }

    private boolean npcNameExists(String normalizedName) {
        if (normalizedName == null) {
            return false;
        }

        for (NpcRecord npc : scheduler.snapshot()) {
            if (normalizedName.equals(normalizeNpcName(npc.npcName()))) {
                return true;
            }
        }

        return false;
    }

    private static String normalizeNpcName(String rawName) {
        if (rawName == null) {
            return null;
        }

        String trimmed = rawName.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }

    private RoleStagingResolveResult resolveRoleStagingForRequiredMarkers(
        String roleId,
        String targetWorldName,
        List<RequiredMarkerResolver.Requirement> requirements
    ) {
        Map<String, ActiveSpawnMarkerStore.StagedSpawnMarker> stagedByName = activeSpawnMarkerStore.snapshotRole(roleId);

        List<String> invalidMarkers = new ArrayList<>();
        List<String> missingMarkers = new ArrayList<>();
        List<String> sharedMarkerConflicts = new ArrayList<>();
        List<String> worldMismatchMarkers = new ArrayList<>();
        List<RequiredRoleMarker> requiredMarkers = new ArrayList<>();
        Map<String, MarkerAssignment> markerAssignments = new LinkedHashMap<>();
        Map<String, String> markerNameByNormalizedMarkerId = new LinkedHashMap<>();

        for (RequiredMarkerResolver.Requirement requirement : requirements) {
            String markerName = normalizeMarkerName(requirement.name());
            MarkerType markerType = requirement.markerType();
            if (markerType == null) {
                invalidMarkers.add(requirement.name().toUpperCase(Locale.ROOT));
                continue;
            }

            ActiveSpawnMarkerStore.StagedSpawnMarker staged = stagedByName.get(markerName);
            if (staged == null) {
                missingMarkers.add(markerName);
                continue;
            }

            if (staged.markerType() != markerType) {
                invalidMarkers.add(markerName.toUpperCase(Locale.ROOT) + " (expected=" + markerType.name()
                    + ", staged=" + staged.markerType().name() + ")");
                continue;
            }

            if (!sameWorld(staged.worldId(), targetWorldName)) {
                worldMismatchMarkers.add(markerName + " (stagedWorld=" + staged.worldId().value()
                    + ", spawnWorld=" + targetWorldName + ")");
                continue;
            }

            String normalizedMarkerId = normalizeMarkerId(staged.markerId());
            if (normalizedMarkerId == null) {
                missingMarkers.add(markerName);
                continue;
            }

            String existingMarkerName = markerNameByNormalizedMarkerId.putIfAbsent(normalizedMarkerId, markerName);
            if (existingMarkerName != null && !existingMarkerName.equals(markerName)) {
                sharedMarkerConflicts.add(existingMarkerName + " <-> " + markerName + " (markerId=" + staged.markerId() + ")");
                continue;
            }

            requiredMarkers.add(new RequiredRoleMarker(
                markerName,
                markerType,
                staged.markerId(),
                staged.worldId(),
                staged.position()
            ));
            markerAssignments.put(logicalKeyForType(markerType), new MarkerAssignment(staged.markerId(), markerType));
        }

        if (!invalidMarkers.isEmpty() || !missingMarkers.isEmpty() || !sharedMarkerConflicts.isEmpty() || !worldMismatchMarkers.isEmpty()) {
            return RoleStagingResolveResult.block(invalidMarkers, missingMarkers, sharedMarkerConflicts, worldMismatchMarkers);
        }

        return RoleStagingResolveResult.pass(requiredMarkers, markerAssignments);
    }

    private StagingOwnershipPrecheckResult stagingOwnershipPrecheck(List<RequiredRoleMarker> requiredMarkers) {
        if (requiredMarkers.isEmpty()) {
            return StagingOwnershipPrecheckResult.pass();
        }

        Map<String, StagingOwnershipConflict> conflictsByMarkerName = new LinkedHashMap<>();
        for (RequiredRoleMarker requiredMarker : requiredMarkers) {
            String normalizedMarkerId = normalizeMarkerId(requiredMarker.markerId());
            if (normalizedMarkerId == null) {
                continue;
            }

            OwnershipHit owner = findOwnershipHitForMarkerId(normalizedMarkerId);
            if (owner != null) {
                conflictsByMarkerName.put(requiredMarker.markerName(),
                    new StagingOwnershipConflict(requiredMarker.markerName(), requiredMarker.markerId(), owner));
            }
        }

        if (conflictsByMarkerName.isEmpty()) {
            return StagingOwnershipPrecheckResult.pass();
        }

        return StagingOwnershipPrecheckResult.block(new ArrayList<>(conflictsByMarkerName.values()));
    }

    private OwnershipHit findOwnershipHitForMarkerId(String normalizedMarkerId) {
        for (NpcRecord npc : scheduler.snapshot()) {
            for (Map.Entry<String, MarkerAssignment> entry : npc.markerAssignments().entrySet()) {
                MarkerAssignment assignment = entry.getValue();
                if (assignment == null) {
                    continue;
                }

                String assignmentMarkerId = normalizeMarkerId(assignment.markerId());
                if (assignmentMarkerId == null || !assignmentMarkerId.equals(normalizedMarkerId)) {
                    continue;
                }

                return new OwnershipHit(
                    npc.npcId(),
                    npc.npcName(),
                    npc.roleId(),
                    entry.getKey()
                );
            }
        }

        return null;
    }

    private static String normalizeMarkerId(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return null;
        }

        return markerId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeMarkerName(String markerName) {
        if (markerName == null || markerName.isBlank()) {
            return null;
        }

        return markerName.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean sameWorld(WorldId stagedWorldId, String targetWorldName) {
        if (stagedWorldId == null || stagedWorldId.value() == null || targetWorldName == null || targetWorldName.isBlank()) {
            return false;
        }

        return stagedWorldId.value().trim().equalsIgnoreCase(targetWorldName.trim());
    }

    private static String logicalKeyForType(MarkerType markerType) {
        return switch (markerType) {
            case BED -> "bed";
            case DOOR -> "door";
            case CHEST -> "chest";
            case FOOD -> "food";
            case WORK -> "work";
            case CHILL -> "chill";
        };
    }

    private List<String> materializeStagedMarkersIntoRegistry(List<RequiredRoleMarker> requiredMarkers) {
        List<String> insertedMarkerIds = new ArrayList<>();
        for (RequiredRoleMarker requiredMarker : requiredMarkers) {
            String markerId = requiredMarker.markerId();
            if (markerRegistry.getById(markerId).isPresent()) {
                continue;
            }

            markerRegistry.upsertRecordOnly(new MarkerRecord(
                markerId,
                requiredMarker.worldId(),
                requiredMarker.position(),
                requiredMarker.markerType()
            ));
            insertedMarkerIds.add(markerId);
        }

        return insertedMarkerIds;
    }

    private boolean allAssignedMarkerIdsPresent(Map<String, MarkerAssignment> markerAssignments) {
        for (MarkerAssignment assignment : markerAssignments.values()) {
            if (assignment == null) {
                continue;
            }

            String markerId = assignment.markerId();
            if (markerId == null || markerId.isBlank()) {
                return false;
            }

            if (markerRegistry.getById(markerId).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void rollbackInsertedMarkerRecords(List<String> insertedMarkerIds) {
        for (String markerId : insertedMarkerIds) {
            if (markerId == null || markerId.isBlank()) {
                continue;
            }
            markerRegistry.removeById(markerId);
        }
    }

    private record RequiredRoleMarker(
        String markerName,
        MarkerType markerType,
        String markerId,
        WorldId worldId,
        Vec3 position
    ) {
    }

    private record OwnershipHit(
        String ownerNpcId,
        String ownerNpcName,
        String ownerRoleId,
        String ownerLogicalMarkerName
    ) {
    }

    private record StagingOwnershipConflict(
        String markerName,
        String markerId,
        OwnershipHit owner
    ) {
    }

    private record RoleStagingResolveResult(
        boolean blocked,
        List<String> invalidMarkers,
        List<String> missingMarkers,
        List<String> sharedMarkerConflicts,
        List<String> worldMismatchMarkers,
        List<RequiredRoleMarker> requiredMarkers,
        Map<String, MarkerAssignment> markerAssignments
    ) {
        private static RoleStagingResolveResult pass(List<RequiredRoleMarker> requiredMarkers, Map<String, MarkerAssignment> markerAssignments) {
            return new RoleStagingResolveResult(
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.copyOf(requiredMarkers),
                Map.copyOf(markerAssignments)
            );
        }

        private static RoleStagingResolveResult block(
            List<String> invalidMarkers,
            List<String> missingMarkers,
            List<String> sharedMarkerConflicts,
            List<String> worldMismatchMarkers
        ) {
            return new RoleStagingResolveResult(
                true,
                List.copyOf(invalidMarkers),
                List.copyOf(missingMarkers),
                List.copyOf(sharedMarkerConflicts),
                List.copyOf(worldMismatchMarkers),
                List.of(),
                Map.of()
            );
        }
    }

    private record StagingOwnershipPrecheckResult(
        boolean blocked,
        List<StagingOwnershipConflict> conflicts
    ) {
        private static StagingOwnershipPrecheckResult pass() {
            return new StagingOwnershipPrecheckResult(false, List.of());
        }

        private static StagingOwnershipPrecheckResult block(List<StagingOwnershipConflict> conflicts) {
            return new StagingOwnershipPrecheckResult(true, List.copyOf(conflicts));
        }
    }
}
