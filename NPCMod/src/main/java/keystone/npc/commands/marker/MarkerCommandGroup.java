package keystone.npc.commands.marker;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import keystone.npc.markers.MarkerRegistry;

public final class MarkerCommandGroup extends AbstractCommandCollection {

    public MarkerCommandGroup(MarkerRegistry markerRegistry) {
        super("marker", "keystone.commands.knpc.marker");
        this.addSubCommand(new MarkerSetCommand(markerRegistry));
        this.addSubCommand(new MarkerClearCommand(markerRegistry));
    }
}
