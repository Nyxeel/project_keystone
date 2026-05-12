package keystone.npc.commands.marker;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.util.Objects;
import javax.annotation.Nonnull;
import keystone.npc.markers.MarkerRegistry;

public final class MarkerClearCommand extends CommandBase {
    private final MarkerRegistry markerRegistry;

    public MarkerClearCommand(MarkerRegistry markerRegistry) {
        super("clear", "keystone.commands.knpc.marker.clear");
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        markerRegistry.clear();
        context.sendMessage(Message.raw("[knpc] Cleared markers (bed/door/chest/food/work/chill)."));
    }
}
