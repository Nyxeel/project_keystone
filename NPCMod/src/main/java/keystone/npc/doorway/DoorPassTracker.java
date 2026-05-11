package keystone.npc.doorway;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.hypixel.hytale.protocol.BlockPosition;

import keystone.npc.markers.Vec3;

public final class DoorPassTracker {
    private final Map<String, Deque<ActiveDoorPass>> activeDoorPasses;

    public DoorPassTracker(Map<String, Deque<ActiveDoorPass>> activeDoorPasses) {
        this.activeDoorPasses = activeDoorPasses;
    }

    public void registerOpenedDoorForClose(String npcId, BlockPosition doorBlock, String doorMarkerId, Vec3 targetPos) {
        if (doorBlock == null) {
            return;
        }

        Deque<ActiveDoorPass> queue = activeDoorPasses.computeIfAbsent(npcId, key -> new ArrayDeque<>());
        for (ActiveDoorPass tracked : queue) {
            if (sameBlock(tracked.doorBlock(), doorBlock)) {
                return;
            }
        }

        ActiveDoorPass existing = queue.peekLast();
        Vec3 effectiveTarget = targetPos;
        if (effectiveTarget == null && existing != null) {
            effectiveTarget = existing.targetPosition();
        }

        queue.addLast(new ActiveDoorPass(doorBlock, doorMarkerId, effectiveTarget, System.currentTimeMillis()));
    }

    public ActiveDoorPass peekActiveDoorPass(String npcId) {
        Deque<ActiveDoorPass> queue = activeDoorPasses.get(npcId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }

        ActiveDoorPass head = queue.peekFirst();
        if (head == null && queue.isEmpty()) {
            activeDoorPasses.remove(npcId);
        }
        return head;
    }

    public void removeTrackedDoorPass(String npcId, BlockPosition doorBlock) {
        Deque<ActiveDoorPass> queue = activeDoorPasses.get(npcId);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        if (doorBlock == null) {
            queue.pollFirst();
        } else {
            queue.removeIf(pass -> sameBlock(pass.doorBlock(), doorBlock));
        }

        if (queue.isEmpty()) {
            activeDoorPasses.remove(npcId);
        }
    }

    private boolean sameBlock(BlockPosition a, BlockPosition b) {
        if (a == null || b == null) {
            return false;
        }
        return a.x == b.x && a.y == b.y && a.z == b.z;
    }
}
