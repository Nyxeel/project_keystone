package keystone.npc.commands.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;

public final class SpawnNpcCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<String> npcIdArg = this.withRequiredArg("npcId", "keystone.commands.knpc.spawn.role", ArgTypes.STRING);

    @Nonnull
    private final OptionalArg<String> nameArg = this.withOptionalArg("name", "keystone.commands.knpc.spawn.name", ArgTypes.STRING);

    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
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

        System.out.println("[KeystoneNPC] Checking required markers for role " + role.roleId() + ": "
            + formatRequiredMarkersForLog(requirements));

        List<String> missingMarkers = new ArrayList<>();
        List<String> invalidMarkers = new ArrayList<>();
        for (RequiredMarkerResolver.Requirement requirement : requirements) {
            MarkerType markerType = requirement.markerType();
            if (markerType == null) {
                invalidMarkers.add(requirement.name().toUpperCase(Locale.ROOT));
                continue;
            }

            if (markerRegistry.getActive(markerType).isEmpty()) {
                String markerName = markerType.name();
                missingMarkers.add(markerName);
                System.err.println("[KeystoneNPC] Missing required marker for role " + role.roleId() + ": " + markerName);
            }
        }

        if (!invalidMarkers.isEmpty()) {
            for (String invalidMarker : invalidMarkers) {
                System.err.println("[KeystoneNPC] Unknown marker type in requiredMarkers: " + invalidMarker);
            }
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            context.sendMessage(Message.raw("[knpc] Invalid required markers:"));
            for (String invalidMarker : invalidMarkers) {
                context.sendMessage(Message.raw("- " + invalidMarker));
            }
            context.sendMessage(Message.raw("[knpc] Supported markers: BED, DOOR, CHEST, FOOD, WORK, CHILL"));
            return;
        }

        if (!missingMarkers.isEmpty()) {
            String missingJoined = String.join("/", missingMarkers);
            System.err.println("[KeystoneNPC][SPAWN_ABORT_MISSING_REQUIRED_MARKER] roleId=" + role.roleId()
                + " missing=" + missingJoined);
            context.sendMessage(Message.raw("[knpc] Cannot spawn " + role.roleId() + "."));
            context.sendMessage(Message.raw("[knpc] Missing required markers:"));
            for (String missingMarker : missingMarkers) {
                context.sendMessage(Message.raw("- " + missingMarker));
            }
            return;
        }

        String name = context.provided(nameArg) ? nameArg.get(context) : role.npcPluginRoleName();

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
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

        if (!plugin.saveStateSafely()) {
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

        context.sendMessage(Message.raw("[knpc] Spawned " + role.roleId() + " '" + npc.npcName()
            + "' (id=" + npc.npcId() + ")."));

        if (NpcDebugSupport.showMarkersEnabled(templateResolver, npc.roleId())) {
            for (String line : NpcDebugSupport.buildMarkerSnapshotLines(npc, markerRegistry, templateResolver, roleDefinitions)) {
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
}
