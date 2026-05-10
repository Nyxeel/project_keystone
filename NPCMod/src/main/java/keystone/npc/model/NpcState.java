package keystone.npc.model;

/**
 * MVP A: minimaler Zustandsautomat.
 * (Zustände können später zusammengelegt/feiner gemacht werden.)
 */
public enum NpcState {
    IDLE(StateType.IDLE, MarkerRole.NONE),

    WALKING_TO_DOOR(StateType.WALKING, MarkerRole.DOOR),
    OPENING_DOOR(StateType.IDLE, MarkerRole.DOOR),

    WALKING_TO_WORK(StateType.WALKING, MarkerRole.WORK),
    WORKING(StateType.IDLE, MarkerRole.WORK),

    WALKING_TO_BED(StateType.WALKING, MarkerRole.BED),
    SLEEPING(StateType.IDLE, MarkerRole.BED),

    PAUSED_MISSING_MARKER(StateType.PAUSED, MarkerRole.NONE);

    private final StateType type;
    private final MarkerRole markerRole;

    NpcState(StateType type, MarkerRole markerRole) {
        this.type = type;
        this.markerRole = markerRole;
    }

    public StateType type() {
        return type;
    }

    public MarkerRole markerRole() {
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
