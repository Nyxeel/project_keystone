package keystone.npc.capabilities;

import java.util.Locale;
import java.util.Optional;

public enum NpcCapability {
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

    public static Optional<NpcCapability> tryParse(String raw) {
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
