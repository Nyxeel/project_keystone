package keystone.npc.definition;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public record NpcDefinition(
    String id,
    Integer version,
    String type,
    String template,
    String displayName,
    String nameTranslationKey,
    String npcType,
    String faction,
    String role,
    NpcAppearanceDefinition appearance,
    NpcStatsDefinition stats,
    NpcDropsDefinition drops,
    NpcAttitudeDefinition attitude,
    NpcProfileRefs profiles,
    List<String> requiredMarkers,
    Map<String, String> markerRoles,
    List<NpcMotionControllerDefinition> motionControllerList,
    List<NpcInstructionDefinition> instructions,
    String defaultState,
    NpcDebugDefinition debug
) {

    public NpcDefinition {
        if (id != null && !id.isBlank()) {
            id = normalizeId(id);
        }
        requiredMarkers = requiredMarkers == null ? List.of() : List.copyOf(requiredMarkers);
        markerRoles = markerRoles == null ? Map.of() : Map.copyOf(markerRoles);
        motionControllerList = motionControllerList == null ? List.of() : List.copyOf(motionControllerList);
        instructions = instructions == null ? List.of() : List.copyOf(instructions);
    }

    public static String normalizeId(String rawId) {
        Objects.requireNonNull(rawId, "rawId");
        String normalized = rawId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return normalized;
    }

    public String effectiveRoleId() {
        if (role != null && !role.isBlank()) {
            return normalizeId(role);
        }
        return id;
    }

    public String effectiveDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        if (nameTranslationKey != null && !nameTranslationKey.isBlank()) {
            return nameTranslationKey;
        }
        return id;
    }
}
