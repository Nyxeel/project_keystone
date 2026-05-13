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
        int removed = scheduler.clearNpcs();
        if (!plugin.saveStateSafely()) {
            context.sendMessage(Message.raw("[knpc] Removed " + removed + " NPC(s), but state save failed. Runtime changes may not be persisted."));
            return;
        }

        context.sendMessage(Message.raw("[knpc] Removed " + removed + " NPC(s) and saved state."));
    }
}
