package keystone.npc.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.commands.admin.NpcClearCommand;
import keystone.npc.commands.admin.NpcListCommand;
import keystone.npc.commands.admin.NpcRemoveCommand;
import keystone.npc.commands.debug.NpcStatusCommand;
import keystone.npc.commands.marker.MarkerCommandGroup;
import keystone.npc.commands.spawn.SpawnNpcCommand;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;

/**
 * MVP A:
 * - /knpc marker set <bed|door|work>
 * - /knpc marker clear
 * - /knpc spawn <role> <name>
 * - /knpc list
 * - /knpc remove <npcId>
 * - /knpc clear
 * - /knpc status
 */
public final class KeystoneNpcCommands extends AbstractCommandCollection {

    public KeystoneNpcCommands(
        KeystoneNpcPlugin plugin,
        MarkerRegistry markerRegistry,
        RoleDefinitionRegistry roleDefinitions,
        NpcTemplateResolver templateResolver,
        NpcRoutineRunner scheduler
    ) {
        super("knpc", "keystone.commands.knpc");
        this.addSubCommand(new MarkerCommandGroup(plugin, markerRegistry, scheduler, templateResolver, roleDefinitions));
        this.addSubCommand(new SpawnNpcCommand(plugin, markerRegistry, roleDefinitions, templateResolver, scheduler));
        this.addSubCommand(new NpcListCommand(scheduler));
        this.addSubCommand(new NpcRemoveCommand(plugin, scheduler));
        this.addSubCommand(new NpcClearCommand(plugin, scheduler));
        this.addSubCommand(new NpcStatusCommand(markerRegistry, scheduler, templateResolver, roleDefinitions));
    }
}
