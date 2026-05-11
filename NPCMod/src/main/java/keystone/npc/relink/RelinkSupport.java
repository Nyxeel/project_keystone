package keystone.npc.relink;

import java.util.List;
import java.util.Objects;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.routine.entity.EntitySyncService;
import keystone.npc.markers.Vec3;

public final class RelinkSupport {
    private final EntitySyncService entitySync;

    public RelinkSupport(EntitySyncService entitySync) {
        this.entitySync = Objects.requireNonNull(entitySync);
    }

    public boolean containsRef(List<Ref<EntityStore>> refs, Ref<EntityStore> target) {
        for (Ref<EntityStore> ref : refs) {
            if (sameRef(ref, target)) {
                return true;
            }
        }
        return false;
    }

    public Ref<EntityStore> findClosestRef(List<Ref<EntityStore>> refs, Vec3 center) {
        Ref<EntityStore> closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Ref<EntityStore> ref : refs) {
            Vec3 pos = entitySync.readPosition(ref);
            if (pos == null) {
                continue;
            }

            double distanceSq = distanceSq(center, new Vector3d(pos.x(), pos.y(), pos.z()));
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closest = ref;
            }
        }

        return closest;
    }

    public boolean sameRef(Ref<EntityStore> a, Ref<EntityStore> b) {
        return a == b || (a != null && a.equals(b));
    }

    public boolean isRefNearCenter(Ref<EntityStore> ref, Vec3 center, double maxDistanceSq) {
        Vec3 refPos = entitySync.readPosition(ref);
        return refPos != null && distanceSq(center, refPos) <= maxDistanceSq;
    }

    public boolean isNearAnyDedupeAnchor(List<Vec3> anchors, Vector3d position, double roleIdDedupeRadiusSq) {
        for (Vec3 anchor : anchors) {
            if (distanceSq(anchor, position) <= roleIdDedupeRadiusSq) {
                return true;
            }
        }
        return false;
    }

    public double distanceSq(Vec3 center, Vector3d other) {
        double dx = center.x() - other.x;
        double dy = center.y() - other.y;
        double dz = center.z() - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
