package keystone.npc.navigation;

import java.util.Objects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;

import keystone.npc.world.Vec3;

/**
 * Bridges scheduler-owned targets into the engine NPC motion pipeline.
 *
 * We drive movement by updating the NPC leash point and letting role
 * instructions (Sensor Leash + BodyMotion Seek) perform pathing/collision.
 */
public final class EngineNavigationController {
    private static final String DEFAULT_MOTION_CONTROLLER = "Walk";

    private final String motionControllerName;

    public EngineNavigationController() {
        this(DEFAULT_MOTION_CONTROLLER);
    }

    public EngineNavigationController(String motionControllerName) {
        this.motionControllerName = Objects.requireNonNull(motionControllerName, "motionControllerName");
    }

    public boolean setTarget(Ref<EntityStore> entityRef, Vec3 target) {
        if (entityRef == null || !entityRef.isValid() || target == null) {
            return false;
        }

        Store<EntityStore> store = entityRef.getStore();
        var npcType = NPCEntity.getComponentType();
        if (npcType == null) {
            return false;
        }

        NPCEntity npc = store.getComponent(entityRef, npcType);
        if (npc == null) {
            return false;
        }

        Role role = npc.getRole();
        if (role != null) {
            role.setActiveMotionController(entityRef, npc, Objects.requireNonNull(motionControllerName), store);
        }

        npc.getLeashPoint().set(target.x(), target.y(), target.z());
        return true;
    }

    public Vec3 readCurrentPosition(Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid()) {
            return null;
        }

        var transformType = TransformComponent.getComponentType();
        if (transformType == null) {
            return null;
        }

        TransformComponent transform = entityRef.getStore().getComponent(entityRef, transformType);
        if (transform == null) {
            return null;
        }

        return new Vec3(transform.getPosition().x(), transform.getPosition().y(), transform.getPosition().z());
    }
}