package keystone.npc.commands.admin;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.routine.NpcRoutineRunner;

public final class NpcRespawnMissingCommand extends AbstractPlayerCommand {

    @Nonnull
    private final OptionalArg<String> modeArg = this.withOptionalArg("mode", "keystone.commands.knpc.respawn.missing.mode", ArgTypes.STRING);

    private final KeystoneNpcPlugin plugin;
    private final NpcRoutineRunner scheduler;

    public NpcRespawnMissingCommand(KeystoneNpcPlugin plugin, NpcRoutineRunner scheduler) {
        super("respawn-missing", "keystone.commands.knpc.respawn.missing");
        this.plugin = Objects.requireNonNull(plugin);
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
        String mode = context.provided(modeArg) ? modeArg.get(context) : "";
        boolean force = false;
        boolean dryRun = false;
        if (!mode.isBlank()) {
            String normalized = mode.trim().toLowerCase(java.util.Locale.ROOT);
            if ("--force".equals(normalized) || "force".equals(normalized)) {
                force = true;
            } else if ("--dry-run".equals(normalized) || "dry-run".equals(normalized)) {
                dryRun = true;
            } else {
                context.sendMessage(Message.raw("[knpc] Usage: /knpc respawn-missing [--force|--dry-run]"));
                return;
            }
        }

        NpcRoutineRunner.RespawnMissingResult result = scheduler.respawnMissingNpcsInWorld(world, force, dryRun, "command-respawn-missing");
        if (result.stateChanged() && !dryRun) {
            plugin.saveStateSafely();
        }

        if (dryRun) {
            context.sendMessage(Message.raw("[knpc] Dry run complete:"));
            context.sendMessage(Message.raw("[knpc] - would relink: " + result.wouldRelink()));
            context.sendMessage(Message.raw("[knpc] - pending relink (blocked this cycle): " + result.pending()));
            context.sendMessage(Message.raw("[knpc] - ambiguous (unsafe, blocked): " + result.ambiguous()));
            context.sendMessage(Message.raw("[knpc] - would spawn with --force (only after no pending/ambiguous): " + result.wouldSpawn()));
            context.sendMessage(Message.raw("[knpc] - skipped/not missing: " + result.skippedNotMissing()));
            context.sendMessage(Message.raw("[knpc] - force required: " + result.forceRequired()));
            return;
        }

        if (force) {
            context.sendMessage(Message.raw("[knpc] Respawn complete (force):"
                + " relinked=" + result.relinked()
                + " pendingBlocked=" + result.pending()
                + " spawned=" + result.respawned()
                + " ambiguousSkipped=" + result.ambiguous()
                + " failed=" + result.failed()));
            return;
        }

        context.sendMessage(Message.raw("[knpc] Respawn complete (safe):"
            + " relinked=" + result.relinked()
            + " pendingBlocked=" + result.pending()
            + " forceRequired=" + result.forceRequired()
            + " ambiguous=" + result.ambiguous()
            + " spawned=0"));

        if (!force && result.forceRequired() > 0) {
            context.sendMessage(Message.raw("[knpc] No safe relink target found. Use /knpc respawn-missing --force to create replacement."));
        }
    }
}
