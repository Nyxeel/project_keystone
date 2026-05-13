package keystone.npc.commands.marker;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.markers.ActiveSpawnMarkerStore;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;

public final class MarkerSetCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> roleArg = this.withRequiredArg("role", "keystone.commands.knpc.marker.set.role", ArgTypes.STRING);

    private final RequiredArg<String> markerNameArg = this.withRequiredArg("markerName", "keystone.commands.knpc.marker.set.name", ArgTypes.STRING);

    private final ActiveSpawnMarkerStore activeSpawnMarkerStore;
    private final RoleDefinitionRegistry roleDefinitions;
    private final RequiredMarkerResolver requiredMarkerResolver;

    public MarkerSetCommand(
        KeystoneNpcPlugin plugin,
        MarkerRegistry markerRegistry,
        NpcRoutineRunner scheduler,
        keystone.npc.definition.NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        super("set", "keystone.commands.knpc.marker.set");
        Objects.requireNonNull(markerRegistry);
        Objects.requireNonNull(scheduler);
        this.activeSpawnMarkerStore = Objects.requireNonNull(plugin).activeSpawnMarkerStore();
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.requiredMarkerResolver = new RequiredMarkerResolver(Objects.requireNonNull(templateResolver), this.roleDefinitions);
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        String rawRole = roleArg.get(context);
        String roleId;
        try {
            roleId = RoleDefinition.normalizeRoleId(rawRole);
        } catch (IllegalArgumentException ex) {
            context.sendMessage(Message.raw("[knpc] Invalid role: '" + rawRole + "'."));
            return;
        }

        List<String> invalidRoleReasons = roleDefinitions.invalidRoleReasons(roleId);
        if (!invalidRoleReasons.isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Role " + roleId + " is invalid:"));
            for (String reason : invalidRoleReasons) {
                context.sendMessage(Message.raw("- " + reason));
            }
            return;
        }

        if (roleDefinitions.findByRoleId(roleId).isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Unknown role: '" + roleId + "'. Known roles: "
                + String.join(", ", roleDefinitions.knownRoleIds())));
            return;
        }

        String rawMarkerName = markerNameArg.get(context);
        if (rawMarkerName == null || rawMarkerName.isBlank()) {
            context.sendMessage(Message.raw("[knpc] Usage: /knpc marker set <role> <markerName>"));
            context.sendMessage(Message.raw("[knpc] Marker set without role is blocked in Marker-v2 staging mode."));
            return;
        }

        String markerName = rawMarkerName.trim().toLowerCase(Locale.ROOT);
        RequiredMarkerResolver.Requirement requirement = findRequirement(roleId, markerName);
        if (requirement == null) {
            context.sendMessage(Message.raw("[knpc] markerName '" + markerName + "' is not required for role '" + roleId + "'."));
            context.sendMessage(Message.raw("[knpc] Allowed marker names: " + String.join(", ", requiredMarkerResolver.resolveRequiredMarkerNames(roleId))));
            return;
        }

        MarkerType markerType = requirement.markerType();
        if (markerType == null) {
            context.sendMessage(Message.raw("[knpc] markerRoles mapping is invalid for markerName '" + markerName + "' in role '" + roleId + "'."));
            return;
        }

        var transformType = TransformComponent.getComponentType();
        if (transformType == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position (TransformComponent type missing)."));
            return;
        }

        TransformComponent transform = store.getComponent(ref, transformType);
        if (transform == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position (TransformComponent missing)."));
            return;
        }

        Vector3d p = transform.getPosition();
        if (!isFinitePosition(p)) {
            context.sendMessage(Message.raw("[knpc] Could not set marker: player position contains non-finite values."));
            return;
        }

        WorldId worldId = new WorldId(world.getName());
        Vec3 position = new Vec3(p.x(), p.y(), p.z());
        String markerId = UUID.randomUUID().toString();

        activeSpawnMarkerStore.put(roleId, markerName, markerId, markerType, worldId, position);

        context.sendMessage(Message.raw("[KNPC][Marker][Staging] role='" + roleId
            + "' marker='" + markerName
            + "' type=" + markerType.name()
            + " markerId=" + markerId
            + " pos=" + NpcDebugSupport.formatPositionForChat(position)));
        context.sendMessage(Message.raw("[KNPC][Marker][Staging] Stored in role-scoped spawn staging only; no NPC assignments were changed."));
    }

    private RequiredMarkerResolver.Requirement findRequirement(String roleId, String markerName) {
        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(roleId)) {
            if (markerName.equals(requirement.name())) {
                return requirement;
            }
        }

        return null;
    }

    private static boolean isFinitePosition(Vector3d position) {
        return position != null
            && Double.isFinite(position.x())
            && Double.isFinite(position.y())
            && Double.isFinite(position.z());
    }
}
