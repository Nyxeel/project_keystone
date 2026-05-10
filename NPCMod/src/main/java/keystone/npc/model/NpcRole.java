package keystone.npc.model;

import java.util.EnumSet;
import java.util.Set;
import keystone.npc.role.RoleDefinition;
import keystone.npc.role.RoleSchedule;
import keystone.npc.world.MarkerType;

/**
 * Built-in role defaults.
 *
 * RoleDefinitionRegistry loads these defaults and can override/extend them from JSON.
 */
public enum NpcRole {
    LUMBERJACK(
        "lumberjack",
        "Lumberjack",
        EnumSet.of(MarkerType.BED, MarkerType.DOOR, MarkerType.WORK),
        new RoleSchedule(21, 7)
    );

    private final String roleId;
    private final String npcPluginRoleName;
    private final Set<MarkerType> requiredMarkers;
    private final RoleSchedule schedule;

    NpcRole(String roleId, String npcPluginRoleName, Set<MarkerType> requiredMarkers, RoleSchedule schedule) {
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

    public RoleSchedule schedule() {
        return schedule;
    }

    public RoleDefinition toDefinition() {
        return new RoleDefinition(roleId, npcPluginRoleName, requiredMarkers, schedule);
    }
}
