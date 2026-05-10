package keystone.npc.navigation;

import keystone.npc.world.Vec3;

/**
 * Utility for NPC navigation calculations.
 */
public final class NpcNavigation {

    private NpcNavigation() {
    }

    /**
     * Calculate distance between two positions.
     */
    public static double distance(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculate travel time (duration in ms) based on distance.
     * MVP A: 1 block per ~500ms (slow walking)
     */
    public static long calculateDurationMs(Vec3 from, Vec3 to) {
        double dist = distance(from, to);
        // ~2 blocks per second = 500ms per block
        return (long) (dist * 500);
    }
}
