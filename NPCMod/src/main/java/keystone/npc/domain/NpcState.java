package keystone.npc.domain;

/**
 * MVP A: minimaler Zustandsautomat.
 * (Zustände können später zusammengelegt/feiner gemacht werden.)
 */
public enum NpcState {
    IDLE(StateType.IDLE, TargetRole.NONE),

    WALKING_TO_DOOR(StateType.WALKING, TargetRole.DOOR),
    OPENING_DOOR(StateType.IDLE, TargetRole.DOOR),

    WALKING_TO_CHEST(StateType.WALKING, TargetRole.CHEST),
    USING_CHEST(StateType.IDLE, TargetRole.CHEST),

    WALKING_TO_FOOD(StateType.WALKING, TargetRole.FOOD),
    EATING(StateType.IDLE, TargetRole.FOOD),

    WALKING_TO_WORK(StateType.WALKING, TargetRole.WORK),
    WORKING(StateType.IDLE, TargetRole.WORK),
    PATROLLING(StateType.IDLE, TargetRole.WORK),

    WALKING_TO_CHILL(StateType.WALKING, TargetRole.CHILL),
    CHILLING(StateType.IDLE, TargetRole.CHILL),

    WALKING_TO_BED(StateType.WALKING, TargetRole.BED),
    SLEEPING(StateType.IDLE, TargetRole.BED),

    PAUSED_MISSING_MARKER(StateType.PAUSED, TargetRole.NONE);

    private final StateType type;
    private final TargetRole markerRole;

    NpcState(StateType type, TargetRole markerRole) {
        this.type = type;
        this.markerRole = markerRole;
    }

    public StateType type() {
        return type;
    }

    public TargetRole markerRole() {
        return markerRole;
    }

    public boolean isWalking() {
        return type == StateType.WALKING;
    }

    public boolean isIdle() {
        return type == StateType.IDLE;
    }

    public boolean isPaused() {
        return type == StateType.PAUSED;
    }
}
