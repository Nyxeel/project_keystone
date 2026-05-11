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
    String worldId,
    PersistedVec3 currentPosition,
    String homeInstanceId,
    String workInstanceId,
    String bedMarkerId,
    String doorMarkerId,
    String workMarkerId,
    String entityUuid,
    PersistedNavigation navigation
) {
}

record PersistedNavigation(
    PersistedVec3 targetPosition,
    String targetState,
    long remainingMs
) {
}

record PersistedVec3(double x, double y, double z) {
}
