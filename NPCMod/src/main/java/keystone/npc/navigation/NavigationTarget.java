package keystone.npc.navigation;

import keystone.npc.domain.NpcState;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;

/**
 * Tracks the current navigation state of an NPC.
 * - Manages movement from one position to another
 * - Handles interpolation and timing
 */
public final class NavigationTarget {

    private Vec3 startPosition;
    private Vec3 targetPosition;
    private long startTimeMs;
    private long durationMs;
    private NpcState targetState;
    private MarkerType targetMarkerType;
    private String targetMarkerId;
    private String targetMarkerName;
    private String targetActionId;

    public NavigationTarget() {
    }

    /**
     * Start navigating from start to target over duration.
     */
    public void startNavigation(Vec3 start, Vec3 target, long durationMs, NpcState targetState) {
        startNavigation(start, target, durationMs, targetState, null, null, null, null);
    }

    /**
     * Start navigating from start to target over duration and store target marker identity.
     */
    public void startNavigation(
        Vec3 start,
        Vec3 target,
        long durationMs,
        NpcState targetState,
        MarkerType targetMarkerType,
        String targetMarkerId
    ) {
        startNavigation(start, target, durationMs, targetState, targetMarkerType, targetMarkerId, null, null);
    }

    /**
     * Start navigating from start to target over duration and store target marker/action identity.
     */
    public void startNavigation(
        Vec3 start,
        Vec3 target,
        long durationMs,
        NpcState targetState,
        MarkerType targetMarkerType,
        String targetMarkerId,
        String targetMarkerName,
        String targetActionId
    ) {
        if (start == null || target == null || targetState == null || durationMs <= 0) {
            clear();
            return;
        }

        this.startPosition = start;
        this.targetPosition = target;
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = Math.max(1L, durationMs);
        this.targetState = targetState;
        this.targetMarkerType = targetMarkerType;
        this.targetMarkerId = targetMarkerId;
        this.targetMarkerName = targetMarkerName;
        this.targetActionId = targetActionId;
    }

    /**
     * Resume navigation from current position with remaining duration.
     */
    public void resumeNavigation(Vec3 currentPosition, Vec3 target, long remainingMs, NpcState targetState) {
        resumeNavigation(currentPosition, target, remainingMs, targetState, null, null, null, null);
    }

    /**
     * Resume navigation from current position with remaining duration and marker identity.
     */
    public void resumeNavigation(
        Vec3 currentPosition,
        Vec3 target,
        long remainingMs,
        NpcState targetState,
        MarkerType targetMarkerType,
        String targetMarkerId
    ) {
        resumeNavigation(currentPosition, target, remainingMs, targetState, targetMarkerType, targetMarkerId, null, null);
    }

    /**
     * Resume navigation from current position with remaining duration and marker/action identity.
     */
    public void resumeNavigation(
        Vec3 currentPosition,
        Vec3 target,
        long remainingMs,
        NpcState targetState,
        MarkerType targetMarkerType,
        String targetMarkerId,
        String targetMarkerName,
        String targetActionId
    ) {
        if (currentPosition == null || target == null || targetState == null || remainingMs <= 0) {
            clear();
            return;
        }

        this.startPosition = currentPosition;
        this.targetPosition = target;
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = Math.max(1L, remainingMs);
        this.targetState = targetState;
        this.targetMarkerType = targetMarkerType;
        this.targetMarkerId = targetMarkerId;
        this.targetMarkerName = targetMarkerName;
        this.targetActionId = targetActionId;
    }

    /**
     * Calculate current position based on elapsed time.
     * Returns null if navigation is complete.
     */
    public Vec3 getCurrentPosition() {
        if (startPosition == null || targetPosition == null) {
            return null;
        }
        if (durationMs <= 0) {
            return null;
        }

        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        if (elapsedMs >= durationMs) {
            // Navigation complete
            return null;
        }

        double progress = (double) elapsedMs / durationMs;
        return interpolate(startPosition, targetPosition, progress);
    }

    /**
     * Check if navigation is complete.
     */
    public boolean isComplete() {
        if (startPosition == null || targetPosition == null) {
            return true;
        }
        if (durationMs <= 0) {
            return true;
        }
        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        return elapsedMs >= durationMs;
    }

    /**
     * Whether a navigation target is currently set.
     */
    public boolean hasTarget() {
        return targetPosition != null;
    }

    /**
     * Remaining duration in milliseconds for the active route.
     */
    public long getRemainingTimeMs() {
        if (startPosition == null || targetPosition == null || durationMs <= 0) {
            return 0L;
        }

        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        return Math.max(0L, durationMs - elapsedMs);
    }

    /**
     * Get the target state to set when navigation is complete.
     */
    public NpcState getTargetState() {
        return targetState;
    }

    /**
     * Get the marker type of the current target, if known.
     */
    public MarkerType getTargetMarkerType() {
        return targetMarkerType;
    }

    /**
     * Get the marker id of the current target, if known.
     */
    public String getTargetMarkerId() {
        return targetMarkerId;
    }

    /**
     * Get the marker name of the current target, if known.
     */
    public String getTargetMarkerName() {
        return targetMarkerName;
    }

    /**
     * Get the action id of the current target, if known.
     */
    public String getTargetActionId() {
        return targetActionId;
    }

    /**
     * Alias for action identity of the current target.
     */
    public String getTargetAction() {
        return targetActionId;
    }

    /**
     * Get target position.
     */
    public Vec3 getTargetPosition() {
        return targetPosition;
    }

    /**
     * Clear navigation state.
     */
    public void clear() {
        startPosition = null;
        targetPosition = null;
        startTimeMs = 0;
        durationMs = 0;
        targetState = null;
        targetMarkerType = null;
        targetMarkerId = null;
        targetMarkerName = null;
        targetActionId = null;
    }

    /**
     * Linear interpolation between two positions.
     */
    private Vec3 interpolate(Vec3 start, Vec3 target, double progress) {
        double x = start.x() + (target.x() - start.x()) * progress;
        double y = start.y() + (target.y() - start.y()) * progress;
        double z = start.z() + (target.z() - start.z()) * progress;
        return new Vec3(x, y, z);
    }
}
