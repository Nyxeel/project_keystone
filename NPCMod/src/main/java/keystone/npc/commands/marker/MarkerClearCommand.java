package keystone.npc.commands.marker;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.routine.NpcRoutineRunner;

public final class MarkerClearCommand extends CommandBase {

    public MarkerClearCommand(KeystoneNpcPlugin plugin, MarkerRegistry markerRegistry, NpcRoutineRunner scheduler) {
        super("clear", "keystone.commands.knpc.marker.clear");
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(markerRegistry);
        Objects.requireNonNull(scheduler);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        context.sendMessage(Message.raw("[knpc] /knpc marker clear is disabled."));
        context.sendMessage(Message.raw("[knpc] Marker are only removed when their owning NPC is safely removed."));
    }
}
