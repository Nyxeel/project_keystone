package keystone.npc.commands.marker;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;

public final class MarkerCommandGroup extends AbstractCommandCollection {

    public MarkerCommandGroup(
        KeystoneNpcPlugin plugin,
        MarkerRegistry markerRegistry,
        NpcRoutineRunner scheduler,
        NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        super("marker", "keystone.commands.knpc.marker");
        this.addSubCommand(new MarkerSetCommand(plugin, markerRegistry, scheduler, templateResolver, roleDefinitions));
        this.addSubCommand(new MarkerClearCommand(plugin, markerRegistry, scheduler));
    }
}
