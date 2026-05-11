package keystone.npc.commands.admin;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.routine.NpcRoutineRunner;

public final class NpcListCommand extends CommandBase {

    private final NpcRoutineRunner scheduler;

    public NpcListCommand(NpcRoutineRunner scheduler) {
        super("list", "keystone.commands.knpc.npc.list");
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        var npcs = scheduler.snapshotIndexed();
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
