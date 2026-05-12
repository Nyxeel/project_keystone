package keystone.npc.skills;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SkillSet {
    private static final SkillSet EMPTY = new SkillSet(EnumSet.noneOf(NpcSkill.class));
    private final EnumSet<NpcSkill> enabled;

    private SkillSet(EnumSet<NpcSkill> enabled) {
        this.enabled = enabled;
    }

    public static SkillSet empty() {
        return EMPTY;
    }

    public static SkillSet fromBooleanMap(Map<String, Boolean> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }

        EnumSet<NpcSkill> enabled = EnumSet.noneOf(NpcSkill.class);
        for (Map.Entry<String, Boolean> entry : values.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }

            NpcSkill.tryParse(entry.getKey()).ifPresent(enabled::add);
        }
        return enabled.isEmpty() ? empty() : new SkillSet(enabled);
    }

    public boolean has(NpcSkill skill) {
        return enabled.contains(Objects.requireNonNull(skill, "skill"));
    }

    public Set<NpcSkill> allEnabled() {
        return Set.copyOf(enabled);
    }
}