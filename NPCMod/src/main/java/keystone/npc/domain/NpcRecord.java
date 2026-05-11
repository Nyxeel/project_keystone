package keystone.npc.domain;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.roles.RoleDefinition;

import java.util.Objects;

/**
 * MVP A: persistierbarer NPC-Datensatz.
 *
 * Minimum-Felder laut Spec:
 * npcId, npcName, role, state, worldId, currentPosition,
 * homeInstanceId, workInstanceId,
 * bedMarkerId, doorMarkerId, workMarkerId
 */
public final class NpcRecord {

    private final String npcId;
    private String npcName;
    private final String roleId;
    private NpcState state;

    private final WorldId worldId;
    private Vec3 currentPosition;

    private String homeInstanceId;
    private String workInstanceId;

    private String bedMarkerId;
    private String doorMarkerId;
    private String workMarkerId;

    private long entityId = 0;  // Serializable: non-zero means entity was already spawned
    private String entityUuid;

    private transient Ref<EntityStore> entityRef;
    private transient NavigationTarget navigationState;

    public NpcRecord(String npcId, String npcName, String roleId, WorldId worldId) {
        this.npcId = Objects.requireNonNull(npcId);
        this.npcName = Objects.requireNonNull(npcName);
        this.roleId = RoleDefinition.normalizeRoleId(roleId);
        this.worldId = Objects.requireNonNull(worldId);
        this.state = NpcState.IDLE;
        this.currentPosition = new Vec3(0, 0, 0);
    }

    public String npcId() { return npcId; }
    public String npcName() { return npcName; }
    public void npcName(String name) { this.npcName = Objects.requireNonNull(name); }

    public String roleId() { return roleId; }

    public NpcState state() { return state; }
    public void state(NpcState state) { this.state = Objects.requireNonNull(state); }

    public WorldId worldId() { return worldId; }

    public Vec3 currentPosition() { return currentPosition; }
    public void currentPosition(Vec3 pos) { this.currentPosition = Objects.requireNonNull(pos); }

    public String homeInstanceId() { return homeInstanceId; }
    public void homeInstanceId(String v) { this.homeInstanceId = v; }

    public String workInstanceId() { return workInstanceId; }
    public void workInstanceId(String v) { this.workInstanceId = v; }

    public String bedMarkerId() { return bedMarkerId; }
    public void bedMarkerId(String v) { this.bedMarkerId = v; }

    public String doorMarkerId() { return doorMarkerId; }
    public void doorMarkerId(String v) { this.doorMarkerId = v; }

    public String workMarkerId() { return workMarkerId; }
    public void workMarkerId(String v) { this.workMarkerId = v; }

    public Ref<EntityStore> entityRef() { return entityRef; }
    public void entityRef(Ref<EntityStore> entityRef) { this.entityRef = entityRef; }

    public long entityId() { return entityId; }
    public void entityId(long id) { this.entityId = id; }

    public String entityUuid() { return entityUuid; }
    public void entityUuid(String entityUuid) { this.entityUuid = entityUuid; }

    public NavigationTarget navigationState() {
        if (navigationState == null) {
            navigationState = new NavigationTarget();
        }
        return navigationState;
    }
}
