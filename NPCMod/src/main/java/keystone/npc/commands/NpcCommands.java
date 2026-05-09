package keystone.npc.commands;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import java.util.Objects;
import keystone.npc.KeystoneNPCPlugin;
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
    private final NpcScheduler scheduler;

    public NpcCommands(KeystoneNPCPlugin plugin, MarkerRegistry markerRegistry, NpcScheduler scheduler) {
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    public void registerAll() {
        // Hytale-style registration (see: many built-in plugins)
        plugin.getCommandRegistry().registerCommand(new KeystoneNpcCommand(markerRegistry, scheduler));
    }
}
