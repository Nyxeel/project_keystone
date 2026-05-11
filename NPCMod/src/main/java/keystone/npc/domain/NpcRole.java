package keystone.npc.domain;

import java.util.EnumSet;
import java.util.Set;

import keystone.npc.markers.MarkerType;
import keystone.npc.roles.DailyRoutine;
import keystone.npc.roles.RoleDefinition;

/**
 * Built-in role defaults.
 *
 * RoleDefinitionRegistry loads these defaults and can override/extend them from JSON.
 */
public enum NpcRole {
    LUMBERJACK(
        "lumberjack",
        "Lumberjack",
        EnumSet.of(MarkerType.BED, MarkerType.WORK),
        new DailyRoutine(21, 7)
    );

    private final String roleId;
    private final String npcPluginRoleName;
    private final Set<MarkerType> requiredMarkers;
    private final DailyRoutine schedule;

    NpcRole(String roleId, String npcPluginRoleName, Set<MarkerType> requiredMarkers, DailyRoutine schedule) {
        this.roleId = roleId;
        this.npcPluginRoleName = npcPluginRoleName;
        this.requiredMarkers = Set.copyOf(requiredMarkers);
        this.schedule = schedule;
    }

    public String roleId() {
        return roleId;
    }

    public String npcPluginRoleName() {
        return npcPluginRoleName;
    }

    public Set<MarkerType> requiredMarkers() {
        return requiredMarkers;
    }

    public DailyRoutine schedule() {
        return schedule;
    }

    public RoleDefinition toDefinition() {
        return new RoleDefinition(roleId, npcPluginRoleName, requiredMarkers, schedule);
    }
}
