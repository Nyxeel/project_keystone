package keystone.npc.doorway;

import com.hypixel.hytale.protocol.BlockPosition;

import keystone.npc.markers.Vec3;

public record ActiveDoorPass(BlockPosition doorBlock, String doorMarkerId, Vec3 targetPosition, long openedAtMs) {
}
