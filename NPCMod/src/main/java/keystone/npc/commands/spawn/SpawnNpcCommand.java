package keystone.npc.commands.spawn;

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
import java.util.Objects;
import javax.annotation.Nonnull;
import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import org.joml.Vector3d;

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
        RoleDefinition role = roleDefinitions.findByRoleId(requestedId).orElse(null);
        if (role == null) {
            context.sendMessage(Message.raw("[knpc] Unknown role: '" + requestedId + "'. Known roles: "
                + String.join(", ", roleDefinitions.roleIds())));
            return;
        }

        var missingMarkers = new java.util.ArrayList<String>();
        var unsupportedMarkers = new java.util.ArrayList<String>();
        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(role.roleId())) {
            MarkerType markerType = requirement.markerType();
            if (markerType == null) {
                unsupportedMarkers.add(requirement.name());
                continue;
            }

            if (markerRegistry.getActive(markerType).isEmpty()) {
                missingMarkers.add(requirement.name());
            }
        }

        if (!unsupportedMarkers.isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Ignoring unsupported requiredMarkers for role '" + role.roleId()
                + "': " + String.join(", ", unsupportedMarkers)
                + ". Supported markers: bed|chest|food|work|chill"));
        }

        if (!missingMarkers.isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Missing markers for role '" + role.roleId()
                + "': " + String.join(", ", missingMarkers) + ". Use /knpc marker set <type>"));
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

        String npcId = java.util.UUID.randomUUID().toString();
        var npc = scheduler.spawnNpc(npcId, name, role.roleId(), new WorldId(world.getName()), new Vec3(spawnPos.x(), spawnPos.y(), spawnPos.z()));
        boolean spawned = scheduler.spawnEntityForNpc(world, npc, "command-spawn");
        if (!spawned) {
            scheduler.removeNpc(npc.npcId());
            context.sendMessage(Message.raw("[knpc] Failed to spawn NPC for role '" + role.roleId() + "'."));
            return;
        }

        markerRegistry.clearActive();
        plugin.saveState();

        context.sendMessage(Message.raw("[knpc] Spawned " + role.roleId() + " '" + npc.npcName()
            + "' (id=" + npc.npcId() + "). Active markers reset."));

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
}
