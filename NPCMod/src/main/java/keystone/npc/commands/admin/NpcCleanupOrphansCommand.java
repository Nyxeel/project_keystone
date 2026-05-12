package keystone.npc.commands.admin;

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

import keystone.npc.markers.Vec3;
import keystone.npc.routine.NpcRoutineRunner;

public final class NpcCleanupOrphansCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<Integer> radiusArg = this.withRequiredArg("radius", "keystone.commands.knpc.cleanup.orphans.radius", ArgTypes.INTEGER);

    @Nonnull
    private final OptionalArg<String> modeArg = this.withOptionalArg("mode", "keystone.commands.knpc.cleanup.orphans.mode", ArgTypes.STRING);

    private final NpcRoutineRunner scheduler;

    public NpcCleanupOrphansCommand(NpcRoutineRunner scheduler) {
        super("cleanup-orphans", "keystone.commands.knpc.cleanup.orphans");
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
        int requestedRadius = radiusArg.get(context);
        int radius = Math.min(Math.max(1, requestedRadius), 256);
        boolean dryRun = false;
        boolean force = false;

        if (context.provided(modeArg)) {
            String mode = modeArg.get(context);
            String normalized = mode == null ? "" : mode.trim().toLowerCase(java.util.Locale.ROOT);
            if ("--dry-run".equals(normalized) || "dry-run".equals(normalized)) {
                dryRun = true;
            } else if ("--force".equals(normalized) || "force".equals(normalized)) {
                force = true;
            } else {
                context.sendMessage(Message.raw("[knpc] Usage: /knpc cleanup-orphans <radius> [--dry-run|--force]"));
                return;
            }
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position."));
            return;
        }

        Vector3d p = transform.getPosition();
        NpcRoutineRunner.CleanupOrphansResult result = scheduler.cleanupOrphans(
            world,
            new Vec3(p.x(), p.y(), p.z()),
            radius,
            dryRun,
            force
        );

        if (result.blocked()) {
            context.sendMessage(Message.raw("[knpc] Cleanup blocked: there are NEEDS_RELINK/MISSING/invalid ACTIVE records in this world ("
                + result.openRelinkRecords() + "). Run relink retries first or use --force."));
            return;
        }

        context.sendMessage(Message.raw("[knpc] cleanup-orphans mode=" + (dryRun ? "dry-run" : force ? "force" : "safe")
            + " radius=" + radius
            + " found=" + result.found()
            + " removed=" + result.removed()
            + " wouldRemove=" + result.wouldRemove()));
    }
}
