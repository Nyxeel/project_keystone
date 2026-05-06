package keystone.npc.model;

/**
 * MVP A: minimaler Zustandsautomat.
 * (Zustände können später zusammengelegt/feiner gemacht werden.)
 */
public enum NpcState {
    IDLE,

    WALKING_TO_DOOR,
    WALKING_TO_WORK,
    WORKING,

    WALKING_TO_BED,
    SLEEPING,

    PAUSED_MISSING_MARKER
}
