package keystone.npc.doorway;

import com.hypixel.hytale.protocol.BlockPosition;

public record PendingDoorAttempt(BlockPosition doorBlock, String doorMarkerId, long startedAtMs) {
}
