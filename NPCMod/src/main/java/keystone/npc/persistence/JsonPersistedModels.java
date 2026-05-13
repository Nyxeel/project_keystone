package keystone.npc.persistence;

import java.util.List;
import java.util.Map;

record PersistedState(
    List<PersistedMarker> markers,
    List<PersistedNpc> npcs,
    Map<String, String> activeMarkerIds
) {
}

record PersistedMarker(String markerId, String worldId, PersistedVec3 position, String type) {
}

record PersistedNpc(
    String npcId,
    String npcName,
    String role,
    String state,
    String entityStatus,
    String worldId,
    PersistedVec3 currentPosition,
    String homeInstanceId,
    String workInstanceId,
    String bedMarkerId,
    String doorMarkerId,
    String chestMarkerId,
    String foodMarkerId,
    String workMarkerId,
    String chillMarkerId,
    Map<String, PersistedMarkerAssignment> markerAssignments,
    String entityUuid,
    PersistedNavigation navigation
) {
}

record PersistedMarkerAssignment(String markerId, String markerType) {
}

record PersistedNavigation(
    PersistedVec3 targetPosition,
    String targetState,
    long remainingMs,
    String markerType,
    String markerId
) {
}

record PersistedVec3(double x, double y, double z) {
}
