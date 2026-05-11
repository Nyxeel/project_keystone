package keystone.npc.commands;

import java.util.Objects;
import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;
import keystone.npc.markers.MarkerRegistry;

/**
 * MVP A: Command registration.
 *
 * Registers {@link KeystoneNpcCommands} as /knpc.
 */
public final class NpcCommandRegistrar {

    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final RoleDefinitionRegistry roleDefinitions;
    private final NpcRoutineRunner scheduler;

    public NpcCommandRegistrar(KeystoneNpcPlugin plugin, MarkerRegistry markerRegistry, RoleDefinitionRegistry roleDefinitions, NpcRoutineRunner scheduler) {
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    public void registerAll() {
        // Hytale-style registration (see: many built-in plugins)
        plugin.getCommandRegistry().registerCommand(new KeystoneNpcCommands(plugin, markerRegistry, roleDefinitions, scheduler));
    }
}
