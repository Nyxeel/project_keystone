package keystone.npc.roles;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import keystone.npc.markers.MarkerType;

/** Runtime role configuration derived from loaded NPC JSON definitions. */
public record RoleDefinition(
    String roleId,
    String npcPluginRoleName,
    Set<MarkerType> requiredMarkers
) {
    public RoleDefinition {
        roleId = normalizeRoleId(roleId);
        npcPluginRoleName = Objects.requireNonNull(npcPluginRoleName, "npcPluginRoleName").trim();
        if (npcPluginRoleName.isEmpty()) {
            throw new IllegalArgumentException("npcPluginRoleName must not be blank");
        }

        requiredMarkers = Set.copyOf(Objects.requireNonNull(requiredMarkers, "requiredMarkers"));
    }

    public static String normalizeRoleId(String roleId) {
        Objects.requireNonNull(roleId, "roleId");
        String normalized = roleId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }
        return normalized;
    }
}
