package keystone.npc.commands.admin;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.routine.NpcRoutineRunner;

public final class NpcRemoveCommand extends CommandBase {

    @Nonnull
    private final RequiredArg<Integer> indexArg = this.withRequiredArg("index", "keystone.commands.knpc.remove.index", ArgTypes.INTEGER);

    private final KeystoneNpcPlugin plugin;
    private final NpcRoutineRunner scheduler;

    public NpcRemoveCommand(KeystoneNpcPlugin plugin, NpcRoutineRunner scheduler) {
        super("remove", "keystone.commands.knpc.remove");
        this.plugin = Objects.requireNonNull(plugin);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        int index = indexArg.get(context);
        NpcRoutineRunner.RemoveNpcResult removeResult = scheduler.removeNpcByIndexDetailed(index);
        if (!removeResult.found()) {
            context.sendMessage(Message.raw("[knpc] Invalid NPC index: " + index));
            return;
        }

        context.sendMessage(Message.raw("[knpc] Remove target: index=" + removeResult.index()
            + " npcId=" + removeResult.npcId()
            + " name='" + removeResult.npcName() + "'"
            + " role=" + removeResult.roleId()
            + " entityStatus=" + (removeResult.entityStatus() == null ? "-" : removeResult.entityStatus().name())));

        if (!removeResult.removed()) {
            context.sendMessage(Message.raw("[knpc] Remove blocked: " + removeResult.message()
                + " (entityRemovalOutcome=" + removeResult.entityRemovalOutcome() + ")."));
            return;
        }

        if (!plugin.saveStateSafely()) {
            boolean rolledBack = scheduler.rollbackRemovedNpc(removeResult.rollbackSnapshot());
            if (rolledBack) {
                context.sendMessage(Message.raw("[knpc] Remove aborted: state save failed. Runtime rollback completed for NPC #" + index + "."));
            } else {
                context.sendMessage(Message.raw("[knpc] Remove aborted: state save failed and runtime rollback was incomplete for NPC #" + index + "."));
                context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: rollback could not fully restore marker/runtime state."));
            }
            return;
        }

        context.sendMessage(Message.raw("[knpc] Removed NPC #" + index
            + " and saved state (ownedMarkersRemoved=" + removeResult.removedOwnedMarkerCount() + ")."));
    }
}
