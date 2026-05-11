package keystone.npc.routine.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.Vec3;
import org.joml.Vector3d;

public final class EntitySyncService {
    public Vec3 readPosition(Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid()) {
            return null;
        }

        TransformComponent transform = entityRef.getStore().getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }

        Vector3d position = transform.getPosition();
        return new Vec3(position.x(), position.y(), position.z());
    }

    public void updateNpcPositionFromEntity(NpcRecord npc, Ref<EntityStore> entityRef) {
        Vec3 position = readPosition(entityRef);
        if (position != null) {
            npc.currentPosition(position);
        }
    }

    public void updatePersistedEntityIdentity(NpcRecord npc, Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid()) {
            npc.entityUuid(null);
            return;
        }

        UUIDComponent uuidComponent = entityRef.getStore().getComponent(entityRef, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            npc.entityUuid(null);
            return;
        }

        npc.entityUuid(uuidComponent.getUuid().toString());
    }

    public void updateEntityPosition(NpcRecord npc, Vec3 newPosition) {
        Ref<EntityStore> entityRef = npc.entityRef();
        if (entityRef == null || !entityRef.isValid()) {
            System.err.println("[KeystoneNPC] Cannot move NPC '" + npc.npcName() + "' (" + npc.npcId()
                + "): missing or invalid EntityRef");
            return;
        }

        Store<EntityStore> store = entityRef.getStore();
        TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) {
            System.err.println("[KeystoneNPC] Cannot move NPC '" + npc.npcName() + "' (" + npc.npcId()
                + "): missing TransformComponent");
            return;
        }

        Vector3d newPos = new Vector3d(newPosition.x(), newPosition.y(), newPosition.z());
        transform.setPosition(newPos);
    }
}
