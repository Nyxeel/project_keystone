package keystone.npc.domain;

import java.util.Objects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.roles.RoleDefinition;

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
    private boolean currentPositionKnown;

    private String homeInstanceId;
    private String workInstanceId;

    private String bedMarkerId;
    private String doorMarkerId;
    private String chestMarkerId;
    private String foodMarkerId;
    private String workMarkerId;
    private String chillMarkerId;

    private long entityId = 0;  // Serializable: non-zero means entity was already spawned
    private String entityUuid;
    private NpcEntityStatus entityStatus;

    private transient Ref<EntityStore> entityRef;
    private transient NavigationTarget navigationState;
    private transient String activeRoutineMarker;
    private transient NpcState activeRoutineState;
    private transient String activeRoutineActionId;
    private transient String activeRoutineSource;
    private transient String pendingActionId;
    private transient String activeActionId;
    private transient String lastActionNoRestartLog;
    private transient String lastValidationWarningKey;
    private transient String lastSkillDecisionKey;
    private transient String movementProfileId;
    private transient String navigationProfileId;
    private transient Integer navigationProfileVersion;
    private transient String navigationPathStyle;
    private transient String navigationDoorPolicy;
    private transient String navigationDangerPolicy;
    private transient String navigationTargetPolicy;
    private transient String navigationShortcutPolicy;
    private transient String navigationClimbPolicy;
    private transient boolean movementProfileMissingWarned;
    private transient String motionControllerType;
    private transient Double stopDistance;
    private transient Double slowDownDistance;
    private transient Boolean usePathfinder;
    private transient Boolean useSteering;
    private transient Double maxWalkSpeed;

    public NpcRecord(String npcId, String npcName, String roleId, WorldId worldId) {
        this.npcId = Objects.requireNonNull(npcId);
        this.npcName = Objects.requireNonNull(npcName);
        this.roleId = RoleDefinition.normalizeRoleId(roleId);
        this.worldId = Objects.requireNonNull(worldId);
        this.state = NpcState.IDLE;
        this.currentPosition = new Vec3(0, 0, 0);
        this.currentPositionKnown = false;
        this.entityStatus = NpcEntityStatus.NEEDS_RELINK;
    }

    public String npcId() { return npcId; }
    public String npcName() { return npcName; }
    public void npcName(String name) { this.npcName = Objects.requireNonNull(name); }

    public String roleId() { return roleId; }

    public NpcState state() { return state; }
    public void state(NpcState state) { this.state = Objects.requireNonNull(state); }

    public WorldId worldId() { return worldId; }

    public Vec3 currentPosition() { return currentPosition; }
    public void currentPosition(Vec3 pos) {
        this.currentPosition = Objects.requireNonNull(pos);
        this.currentPositionKnown = true;
    }
    public boolean hasKnownCurrentPosition() { return currentPositionKnown; }
    public void clearCurrentPosition() { this.currentPositionKnown = false; }

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

    public String chestMarkerId() { return chestMarkerId; }
    public void chestMarkerId(String v) { this.chestMarkerId = v; }

    public String foodMarkerId() { return foodMarkerId; }
    public void foodMarkerId(String v) { this.foodMarkerId = v; }

    public String chillMarkerId() { return chillMarkerId; }
    public void chillMarkerId(String v) { this.chillMarkerId = v; }

    public Ref<EntityStore> entityRef() { return entityRef; }
    public void entityRef(Ref<EntityStore> entityRef) { this.entityRef = entityRef; }

    public long entityId() { return entityId; }
    public void entityId(long id) { this.entityId = id; }

    public String entityUuid() { return entityUuid; }
    public void entityUuid(String entityUuid) { this.entityUuid = entityUuid; }

    public NpcEntityStatus entityStatus() { return entityStatus; }
    public void entityStatus(NpcEntityStatus entityStatus) { this.entityStatus = Objects.requireNonNull(entityStatus); }

    public NavigationTarget navigationState() {
        if (navigationState == null) {
            navigationState = new NavigationTarget();
        }
        return navigationState;
    }

    public String activeRoutineMarker() { return activeRoutineMarker; }
    public void activeRoutineMarker(String activeRoutineMarker) { this.activeRoutineMarker = activeRoutineMarker; }

    public NpcState activeRoutineState() { return activeRoutineState; }
    public void activeRoutineState(NpcState activeRoutineState) { this.activeRoutineState = activeRoutineState; }

    public String activeRoutineActionId() { return activeRoutineActionId; }
    public void activeRoutineActionId(String activeRoutineActionId) { this.activeRoutineActionId = activeRoutineActionId; }

    public String activeRoutineSource() { return activeRoutineSource; }
    public void activeRoutineSource(String activeRoutineSource) { this.activeRoutineSource = activeRoutineSource; }

    public String pendingActionId() { return pendingActionId; }
    public void pendingActionId(String pendingActionId) { this.pendingActionId = pendingActionId; }

    public String activeActionId() { return activeActionId; }
    public void activeActionId(String activeActionId) { this.activeActionId = activeActionId; }

    public String lastActionNoRestartLog() { return lastActionNoRestartLog; }
    public void lastActionNoRestartLog(String lastActionNoRestartLog) { this.lastActionNoRestartLog = lastActionNoRestartLog; }

    public String lastValidationWarningKey() { return lastValidationWarningKey; }
    public void lastValidationWarningKey(String lastValidationWarningKey) { this.lastValidationWarningKey = lastValidationWarningKey; }

    public String lastSkillDecisionKey() { return lastSkillDecisionKey; }
    public void lastSkillDecisionKey(String lastSkillDecisionKey) { this.lastSkillDecisionKey = lastSkillDecisionKey; }

    public String movementProfileId() { return movementProfileId; }
    public void movementProfileId(String movementProfileId) { this.movementProfileId = movementProfileId; }

    public String navigationProfileId() { return navigationProfileId; }
    public void navigationProfileId(String navigationProfileId) { this.navigationProfileId = navigationProfileId; }

    public Integer navigationProfileVersion() { return navigationProfileVersion; }
    public void navigationProfileVersion(Integer navigationProfileVersion) { this.navigationProfileVersion = navigationProfileVersion; }

    public String navigationPathStyle() { return navigationPathStyle; }
    public void navigationPathStyle(String navigationPathStyle) { this.navigationPathStyle = navigationPathStyle; }

    public String navigationDoorPolicy() { return navigationDoorPolicy; }
    public void navigationDoorPolicy(String navigationDoorPolicy) { this.navigationDoorPolicy = navigationDoorPolicy; }

    public String navigationDangerPolicy() { return navigationDangerPolicy; }
    public void navigationDangerPolicy(String navigationDangerPolicy) { this.navigationDangerPolicy = navigationDangerPolicy; }

    public String navigationTargetPolicy() { return navigationTargetPolicy; }
    public void navigationTargetPolicy(String navigationTargetPolicy) { this.navigationTargetPolicy = navigationTargetPolicy; }

    public String navigationShortcutPolicy() { return navigationShortcutPolicy; }
    public void navigationShortcutPolicy(String navigationShortcutPolicy) { this.navigationShortcutPolicy = navigationShortcutPolicy; }

    public String navigationClimbPolicy() { return navigationClimbPolicy; }
    public void navigationClimbPolicy(String navigationClimbPolicy) { this.navigationClimbPolicy = navigationClimbPolicy; }

    public boolean movementProfileMissingWarned() { return movementProfileMissingWarned; }
    public void movementProfileMissingWarned(boolean movementProfileMissingWarned) { this.movementProfileMissingWarned = movementProfileMissingWarned; }

    public String motionControllerType() { return motionControllerType; }
    public void motionControllerType(String motionControllerType) { this.motionControllerType = motionControllerType; }

    public Double stopDistance() { return stopDistance; }
    public void stopDistance(Double stopDistance) { this.stopDistance = stopDistance; }

    public Double slowDownDistance() { return slowDownDistance; }
    public void slowDownDistance(Double slowDownDistance) { this.slowDownDistance = slowDownDistance; }

    public Boolean usePathfinder() { return usePathfinder; }
    public void usePathfinder(Boolean usePathfinder) { this.usePathfinder = usePathfinder; }

    public Boolean useSteering() { return useSteering; }
    public void useSteering(Boolean useSteering) { this.useSteering = useSteering; }

    public Double maxWalkSpeed() { return maxWalkSpeed; }
    public void maxWalkSpeed(Double maxWalkSpeed) { this.maxWalkSpeed = maxWalkSpeed; }
}
