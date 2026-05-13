package keystone.npc.commands.admin;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.routine.NpcRoutineRunner;

public final class NpcClearCommand extends CommandBase {

    private final KeystoneNpcPlugin plugin;
    private final NpcRoutineRunner scheduler;

    public NpcClearCommand(KeystoneNpcPlugin plugin, NpcRoutineRunner scheduler) {
        super("clear", "keystone.commands.knpc.npc.clear");
        this.plugin = Objects.requireNonNull(plugin);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        NpcRoutineRunner.ClearNpcsSnapshot clearSnapshot = scheduler.snapshotForClear();
        NpcRoutineRunner.ClearNpcsResult clearResult = scheduler.clearNpcsDetailed();

        if (!plugin.saveStateSafely()) {
            boolean rollbackFromEntries = scheduler.rollbackClearedNpcs(clearResult);
            boolean snapshotRestored = true;
            if (!rollbackFromEntries) {
                snapshotRestored = scheduler.restoreClearSnapshot(clearSnapshot);
            }

            if (rollbackFromEntries) {
                context.sendMessage(Message.raw("[knpc] Clear aborted: state save failed. Runtime rollback completed."));
            } else if (snapshotRestored) {
                context.sendMessage(Message.raw("[knpc] Clear aborted: state save failed. Rollback was partially recovered from pre-clear snapshot."));
                context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: runtime cleanup state may be partially divergent until next stable save."));
            } else {
                context.sendMessage(Message.raw("[knpc] Clear aborted: state save failed and rollback was incomplete."));
                context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: rollback could not fully restore markers/records."));
            }
            return;
        }

        context.sendMessage(Message.raw("[knpc] Clear complete: removed=" + clearResult.removedCount()
            + " blocked=" + clearResult.blockedCount()
            + " requested=" + clearResult.requestedCount()
            + " ownedMarkersRemoved=" + clearResult.removedOwnedMarkerCount()
            + "."));

        if (clearResult.blockedCount() > 0) {
            context.sendMessage(Message.raw("[knpc] Some NPC records were kept because entity removal was not safely confirmed. Use /knpc remove per NPC after fixing entity/world state."));
        }
    }
}
