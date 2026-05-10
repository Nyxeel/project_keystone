package keystone.npc.schedule;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Runs NpcScheduler on the native ECS world tick. */
public final class NpcSchedulerTickSystem extends TickingSystem<EntityStore> {

    private final NpcScheduler scheduler;

    public NpcSchedulerTickSystem(NpcScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        scheduler.tickStore(store);
    }
}
