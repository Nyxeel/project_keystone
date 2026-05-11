package keystone.npc.routine.pathfinding;

import java.util.Objects;

import keystone.npc.domain.NpcRecord;
import keystone.npc.navigation.EngineNavigationController;
import keystone.npc.navigation.NavigationTarget;
import keystone.npc.markers.Vec3;

public final class PathfindingSupport {
    private final EngineNavigationController engineNavigation;

    public PathfindingSupport(EngineNavigationController engineNavigation) {
        this.engineNavigation = Objects.requireNonNull(engineNavigation);
    }

    public boolean hasActiveNavigation(NavigationTarget navState) {
        return navState.hasTarget();
    }

    public Vec3 resolveNavigationStartPosition(NpcRecord npc, Vec3 fallback) {
        Vec3 current = npc.currentPosition();
        if (current != null) {
            return current;
        }

        Vec3 live = engineNavigation.readCurrentPosition(npc.entityRef());
        if (live != null) {
            npc.currentPosition(live);
            return live;
        }

        npc.currentPosition(fallback);
        return fallback;
    }

    public double distanceSqToSegment(Vec3 point, Vec3 segmentStart, Vec3 segmentEnd) {
        double ax = segmentStart.x();
        double ay = segmentStart.y();
        double az = segmentStart.z();

        double bx = segmentEnd.x();
        double by = segmentEnd.y();
        double bz = segmentEnd.z();

        double px = point.x();
        double py = point.y();
        double pz = point.z();

        double abx = bx - ax;
        double aby = by - ay;
        double abz = bz - az;

        double apx = px - ax;
        double apy = py - ay;
        double apz = pz - az;

        double abLenSq = abx * abx + aby * aby + abz * abz;
        if (abLenSq <= 1.0E-6) {
            double dx = px - ax;
            double dy = py - ay;
            double dz = pz - az;
            return dx * dx + dy * dy + dz * dz;
        }

        double t = (apx * abx + apy * aby + apz * abz) / abLenSq;
        double clamped = Math.max(0.0, Math.min(1.0, t));

        double cx = ax + abx * clamped;
        double cy = ay + aby * clamped;
        double cz = az + abz * clamped;

        double dx = px - cx;
        double dy = py - cy;
        double dz = pz - cz;
        return dx * dx + dy * dy + dz * dz;
    }
}
