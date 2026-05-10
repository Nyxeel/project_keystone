package keystone.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import keystone.npc.KeystoneNPCPlugin;
import keystone.npc.role.RoleDefinition;
import keystone.npc.role.RoleDefinitionRegistry;
import keystone.npc.schedule.NpcScheduler;
import keystone.npc.world.MarkerRegistry;
import keystone.npc.world.MarkerType;
import keystone.npc.world.Vec3;
import keystone.npc.world.WorldId;
import org.joml.Vector3d;

/**
 * MVP A:
 * - /knpc marker set <bed|door|work>
 * - /knpc marker clear
 * - /knpc spawn <role> <name>
 * - /knpc list
 * - /knpc remove <npcId>
 * - /knpc clear
 * - /knpc status
 */
public final class KeystoneNpcCommand extends AbstractCommandCollection {

    public KeystoneNpcCommand(KeystoneNPCPlugin plugin, MarkerRegistry markerRegistry, RoleDefinitionRegistry roleDefinitions, NpcScheduler scheduler) {
        super("knpc", "keystone.commands.knpc");
        this.addSubCommand(new MarkerCommand(markerRegistry));
        this.addSubCommand(new SpawnCommand(plugin, markerRegistry, roleDefinitions, scheduler));
        this.addSubCommand(new NpcListCommand(scheduler));
        this.addSubCommand(new NpcRemoveCommand(plugin, scheduler));
        this.addSubCommand(new NpcClearCommand(plugin, scheduler));
        this.addSubCommand(new StatusCommand(markerRegistry, scheduler));
    }

    // -------------------------------------------------------------------------
    // marker
    // -------------------------------------------------------------------------

    private static final class MarkerCommand extends AbstractCommandCollection {

        MarkerCommand(MarkerRegistry markerRegistry) {
            super("marker", "keystone.commands.knpc.marker");
            this.addSubCommand(new MarkerSetCommand(markerRegistry));
            this.addSubCommand(new MarkerClearCommand(markerRegistry));
        }
    }

    private static final class MarkerSetCommand extends AbstractPlayerCommand {

        @Nonnull
        private final RequiredArg<String> markerTypeArg = this.withRequiredArg("markerType", "keystone.commands.knpc.marker.set.type", ArgTypes.STRING);

        private final MarkerRegistry markerRegistry;

        MarkerSetCommand(MarkerRegistry markerRegistry) {
            super("set", "keystone.commands.knpc.marker.set");
            this.markerRegistry = Objects.requireNonNull(markerRegistry);
        }

        @Override
        protected void execute(
                @Nonnull CommandContext context,
                @Nonnull Store<EntityStore> store,
                @Nonnull Ref<EntityStore> ref,
                @Nonnull PlayerRef playerRef,
                @Nonnull World world
        ) {
            String rawType = markerTypeArg.get(context);
            MarkerType type = parseMarkerType(rawType);
            if (type == null) {
                context.sendMessage(Message.raw("[knpc] Unknown marker type: '" + rawType + "'. Use: bed|door|work"));
                return;
            }

            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform == null) {
                context.sendMessage(Message.raw("[knpc] Could not read player position (TransformComponent missing)."));
                return;
            }

            Vector3d p = transform.getPosition();
            var worldId = new WorldId(world.getName());
            var pos = new Vec3(p.x(), p.y(), p.z());

            markerRegistry.setActive(type, worldId, pos);
            context.sendMessage(Message.raw("[knpc] Marker '" + type.name().toLowerCase(Locale.ROOT) + "' set to " + pos + " (world=" + worldId + ")"));
        }

        private static MarkerType parseMarkerType(String raw) {
            if (raw == null) return null;
            return switch (raw.toLowerCase(Locale.ROOT)) {
                case "bed" -> MarkerType.BED;
                case "door" -> MarkerType.DOOR;
                case "work" -> MarkerType.WORK;
                default -> null;
            };
        }
    }

    private static final class MarkerClearCommand extends CommandBase {

        private final MarkerRegistry markerRegistry;

        MarkerClearCommand(MarkerRegistry markerRegistry) {
            super("clear", "keystone.commands.knpc.marker.clear");
            this.markerRegistry = Objects.requireNonNull(markerRegistry);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            markerRegistry.clear();
            context.sendMessage(Message.raw("[knpc] Cleared markers (bed/door/work)."));
        }
    }

    // -------------------------------------------------------------------------
    // spawn
    // -------------------------------------------------------------------------

    private static final class SpawnCommand extends AbstractPlayerCommand {

        @Nonnull
        private final RequiredArg<String> roleArg = this.withRequiredArg("role", "keystone.commands.knpc.spawn.role", ArgTypes.STRING);

        @Nonnull
        private final RequiredArg<String> nameArg = this.withRequiredArg("name", "keystone.commands.knpc.spawn.name", ArgTypes.STRING);

        private final KeystoneNPCPlugin plugin;
        private final MarkerRegistry markerRegistry;
        private final RoleDefinitionRegistry roleDefinitions;
        private final NpcScheduler scheduler;

        SpawnCommand(KeystoneNPCPlugin plugin, MarkerRegistry markerRegistry, RoleDefinitionRegistry roleDefinitions, NpcScheduler scheduler) {
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

            // Spawn 2 blocks in front of player
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

    // -------------------------------------------------------------------------
    // npc management
    // -------------------------------------------------------------------------

    private static final class NpcListCommand extends CommandBase {

        private final NpcScheduler scheduler;

        NpcListCommand(NpcScheduler scheduler) {
            super("list", "keystone.commands.knpc.npc.list");
            this.scheduler = Objects.requireNonNull(scheduler);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            var npcs = scheduler.snapshot();
            if (npcs.isEmpty()) {
                context.sendMessage(Message.raw("[knpc] No NPCs saved."));
                return;
            }

            context.sendMessage(Message.raw("[knpc] NPCs:"));
            for (int i = 0; i < npcs.size(); i++) {
                var npc = npcs.get(i);
                context.sendMessage(Message.raw(i + " - " + npc.npcId() + " | " + npc.npcName() + " | " + npc.roleId() + " | " + npc.state()));
            }
        }
    }

    private static final class NpcRemoveCommand extends CommandBase {

        @Nonnull
        private final RequiredArg<Integer> indexArg = this.withRequiredArg("index", "keystone.commands.knpc.remove.index", ArgTypes.INTEGER);

        private final KeystoneNPCPlugin plugin;
        private final NpcScheduler scheduler;

        NpcRemoveCommand(KeystoneNPCPlugin plugin, NpcScheduler scheduler) {
            super("remove", "keystone.commands.knpc.remove");
            this.plugin = Objects.requireNonNull(plugin);
            this.scheduler = Objects.requireNonNull(scheduler);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            int index = indexArg.get(context);
            boolean removed = scheduler.removeNpcByIndex(index);
            if (!removed) {
                context.sendMessage(Message.raw("[knpc] Invalid NPC index: " + index));
                return;
            }

            plugin.saveState();
            context.sendMessage(Message.raw("[knpc] Removed NPC #" + index + " and saved state."));
        }
    }

    private static final class NpcClearCommand extends CommandBase {

        private final KeystoneNPCPlugin plugin;
        private final NpcScheduler scheduler;

        NpcClearCommand(KeystoneNPCPlugin plugin, NpcScheduler scheduler) {
            super("clear", "keystone.commands.knpc.npc.clear");
            this.plugin = Objects.requireNonNull(plugin);
            this.scheduler = Objects.requireNonNull(scheduler);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            int removed = scheduler.clearNpcs();
            plugin.saveState();
            context.sendMessage(Message.raw("[knpc] Removed " + removed + " NPC(s) and saved state."));
        }
    }

    // -------------------------------------------------------------------------
    // status
    // -------------------------------------------------------------------------

    private static final class StatusCommand extends CommandBase {

        private final MarkerRegistry markerRegistry;
        private final NpcScheduler scheduler;

        StatusCommand(MarkerRegistry markerRegistry, NpcScheduler scheduler) {
            super("status", "keystone.commands.knpc.status");
            this.markerRegistry = Objects.requireNonNull(markerRegistry);
            this.scheduler = Objects.requireNonNull(scheduler);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            var bed = markerRegistry.getActive(MarkerType.BED).map(m -> m.position().toString()).orElse("<unset>");
            var door = markerRegistry.getActive(MarkerType.DOOR).map(m -> m.position().toString()).orElse("<unset>");
            var work = markerRegistry.getActive(MarkerType.WORK).map(m -> m.position().toString()).orElse("<unset>");

            var npcCount = scheduler.snapshot().size();

            context.sendMessage(Message.raw("[knpc] Markers: bed=" + bed + ", door=" + door + ", work=" + work));
            context.sendMessage(Message.raw("[knpc] NPCs: " + npcCount));
        }
    }
}
