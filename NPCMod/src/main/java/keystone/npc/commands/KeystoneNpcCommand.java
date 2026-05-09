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
 * - /knpc spawn lumberjack <name>
 * - /knpc status
 */
public final class KeystoneNpcCommand extends AbstractCommandCollection {

    public KeystoneNpcCommand(MarkerRegistry markerRegistry, NpcScheduler scheduler) {
        super("knpc", "keystone.commands.knpc");
        this.addSubCommand(new MarkerCommand(markerRegistry));
        this.addSubCommand(new SpawnCommand(markerRegistry, scheduler));
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

    private static final class SpawnCommand extends AbstractCommandCollection {

        SpawnCommand(MarkerRegistry markerRegistry, NpcScheduler scheduler) {
            super("spawn", "keystone.commands.knpc.spawn");
            this.addSubCommand(new SpawnLumberjackCommand(markerRegistry, scheduler));
        }
    }

    private static final class SpawnLumberjackCommand extends AbstractPlayerCommand {

        @Nonnull
        private final RequiredArg<String> nameArg = this.withRequiredArg("name", "keystone.commands.knpc.spawn.name", ArgTypes.STRING);

        private final MarkerRegistry markerRegistry;
        private final NpcScheduler scheduler;

        SpawnLumberjackCommand(MarkerRegistry markerRegistry, NpcScheduler scheduler) {
            super("lumberjack", "keystone.commands.knpc.spawn.lumberjack");
            this.markerRegistry = Objects.requireNonNull(markerRegistry);
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
            // MVP A: require all three markers to be set.
            if (markerRegistry.getActive(MarkerType.BED).isEmpty()
                    || markerRegistry.getActive(MarkerType.DOOR).isEmpty()
                    || markerRegistry.getActive(MarkerType.WORK).isEmpty()) {
                context.sendMessage(Message.raw("[knpc] Please set all markers first: /knpc marker set bed|door|work"));
                return;
            }

            String name = nameArg.get(context);
            var npc = scheduler.spawnLumberjack(java.util.UUID.randomUUID().toString(), name, new WorldId(world.getName()));

            context.sendMessage(Message.raw("[knpc] Spawned lumberjack '" + npc.npcName() + "' (id=" + npc.npcId() + ")"));
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
