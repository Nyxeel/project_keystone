package keystone.npc.navigation;

import keystone.npc.world.Vec3;
import keystone.npc.model.NpcState;

/**
 * Tracks the current navigation state of an NPC.
 * - Manages movement from one position to another
 * - Handles interpolation and timing
 */
public final class NavigationState {

    private Vec3 startPosition;
    private Vec3 targetPosition;
    private long startTimeMs;
    private long durationMs;
    private NpcState targetState;

    public NavigationState() {
    }

    /**
     * Start navigating from start to target over duration.
     */
    public void startNavigation(Vec3 start, Vec3 target, long durationMs, NpcState targetState) {
        this.startPosition = start;
        this.targetPosition = target;
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = durationMs;
        this.targetState = targetState;
    }

    /**
     * Calculate current position based on elapsed time.
     * Returns null if navigation is complete.
     */
    public Vec3 getCurrentPosition() {
        if (startPosition == null || targetPosition == null) {
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
        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        return elapsedMs >= durationMs;
    }

    /**
     * Get the target state to set when navigation is complete.
     */
    public NpcState getTargetState() {
        return targetState;
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
