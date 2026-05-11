package keystone.npc.roles;

import java.util.List;

record PersistedRoleFile(List<PersistedRole> roles) {
}

record PersistedRole(
    String roleId,
    String npcPluginRoleName,
    List<String> requiredMarkers,
    PersistedSchedule schedule
) {
}

record PersistedSchedule(
    Integer sleepStartHour,
    Integer wakeHour
) {
}
