package keystone.npc.model;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import keystone.npc.world.Vec3;
import keystone.npc.world.WorldId;

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
    private final NpcRole role;
    private NpcState state;

    private final WorldId worldId;
    private Vec3 currentPosition;

    private String homeInstanceId;
    private String workInstanceId;

    private String bedMarkerId;
    private String doorMarkerId;
    private String workMarkerId;

    private transient Ref<EntityStore> entityRef;

    public NpcRecord(String npcId, String npcName, NpcRole role, WorldId worldId) {
        this.npcId = Objects.requireNonNull(npcId);
        this.npcName = Objects.requireNonNull(npcName);
        this.role = Objects.requireNonNull(role);
        this.worldId = Objects.requireNonNull(worldId);
        this.state = NpcState.IDLE;
        this.currentPosition = new Vec3(0, 0, 0);
    }

    public String npcId() { return npcId; }
    public String npcName() { return npcName; }
    public void npcName(String name) { this.npcName = Objects.requireNonNull(name); }

    public NpcRole role() { return role; }

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
}
