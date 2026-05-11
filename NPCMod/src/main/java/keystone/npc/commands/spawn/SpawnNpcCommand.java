package keystone.npc.commands.spawn;

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
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import org.joml.Vector3d;

public final class SpawnNpcCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<String> roleArg = this.withRequiredArg("role", "keystone.commands.knpc.spawn.role", ArgTypes.STRING);

    @Nonnull
    private final RequiredArg<String> nameArg = this.withRequiredArg("name", "keystone.commands.knpc.spawn.name", ArgTypes.STRING);

    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final RoleDefinitionRegistry roleDefinitions;
    private final NpcRoutineRunner scheduler;

    public SpawnNpcCommand(KeystoneNpcPlugin plugin, MarkerRegistry markerRegistry, RoleDefinitionRegistry roleDefinitions, NpcRoutineRunner scheduler) {
        super("spawn", "keystone.commands.knpc.spawn");
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
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
        String rawRole = roleArg.get(context);
        RoleDefinition role = roleDefinitions.findByRoleId(rawRole).orElse(null);
        if (role == null) {
            context.sendMessage(Message.raw("[knpc] Unknown role: '" + rawRole + "'. Known roles: "
                + String.join(", ", roleDefinitions.roleIds())));
            return;
        }

        var missingMarkers = new java.util.ArrayList<String>();
        for (MarkerType markerType : role.requiredMarkers()) {
            if (markerRegistry.getActive(markerType).isEmpty()) {
                missingMarkers.add(markerType.name().toLowerCase(Locale.ROOT));
            }
        }

        if (!missingMarkers.isEmpty()) {
            context.sendMessage(Message.raw("[knpc] Missing markers for role '" + role.roleId()
                + "': " + String.join(", ", missingMarkers) + ". Use /knpc marker set <type>"));
            return;
        }

        String name = nameArg.get(context);

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
    }
}
