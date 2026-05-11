package keystone.npc.routine;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Runs NpcRoutineRunner on the native ECS world tick. */
public final class NpcTickSystem extends TickingSystem<EntityStore> {

    private final NpcRoutineRunner scheduler;

    public NpcTickSystem(NpcRoutineRunner scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        scheduler.tickStore(store);
    }
}
