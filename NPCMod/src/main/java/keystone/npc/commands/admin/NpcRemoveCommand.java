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
        boolean removed = scheduler.removeNpcByIndex(index);
        if (!removed) {
            context.sendMessage(Message.raw("[knpc] Invalid NPC index: " + index));
            return;
        }

        if (!plugin.saveStateSafely()) {
            context.sendMessage(Message.raw("[knpc] Removed NPC #" + index + ", but state save failed. Runtime changes may not be persisted."));
            return;
        }

        context.sendMessage(Message.raw("[knpc] Removed NPC #" + index + " and saved state."));
    }
}
