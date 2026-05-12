package keystone.npc.skills;

import java.util.Locale;
import java.util.Optional;

public enum NpcSkill {
    OPEN_DOORS,
    SWIM,
    USE_TOOLS,
    USE_CHEST,
    USE_BED,
    TRADE,
    FOLLOW_ROUTINE,
    ATTACK_MELEE,
    ATTACK_RANGED,
    FLEE,
    GUARD_AREA,
    PATROL;

    public static Optional<NpcSkill> tryParse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}