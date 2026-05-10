package keystone.npc.commands;

import java.util.Objects;
import keystone.npc.KeystoneNPCPlugin;
import keystone.npc.role.RoleDefinitionRegistry;
import keystone.npc.schedule.NpcScheduler;
import keystone.npc.world.MarkerRegistry;

/**
 * MVP A: Command registration.
 *
 * Registers {@link KeystoneNpcCommand} as /knpc.
 */
public final class NpcCommands {

    private final KeystoneNPCPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final RoleDefinitionRegistry roleDefinitions;
    private final NpcScheduler scheduler;

    public NpcCommands(KeystoneNPCPlugin plugin, MarkerRegistry markerRegistry, RoleDefinitionRegistry roleDefinitions, NpcScheduler scheduler) {
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    public void registerAll() {
        // Hytale-style registration (see: many built-in plugins)
        plugin.getCommandRegistry().registerCommand(new KeystoneNpcCommand(plugin, markerRegistry, roleDefinitions, scheduler));
    }
}
