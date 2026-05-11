package keystone.npc.commands.debug;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.util.Objects;
import javax.annotation.Nonnull;
import keystone.npc.routine.NpcRoutineRunner;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;

public final class NpcStatusCommand extends CommandBase {

    private final MarkerRegistry markerRegistry;
    private final NpcRoutineRunner scheduler;

    public NpcStatusCommand(MarkerRegistry markerRegistry, NpcRoutineRunner scheduler) {
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
