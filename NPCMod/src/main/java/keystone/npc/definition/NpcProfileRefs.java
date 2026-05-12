package keystone.npc.definition;

import com.google.gson.annotations.SerializedName;

public record NpcProfileRefs(
    String routine,
    String skills,
    @SerializedName("capabilities") String legacySkills,
    String actions,
    String movement,
    String navigation,
    String combat,
    String spawn,
    String structure,
    String persistence
) {

    public String resolveSkillsPath() {
        if (skills != null && !skills.isBlank()) {
            return skills;
        }
        if (legacySkills != null && !legacySkills.isBlank()) {
            return legacySkills;
        }
        return null;
    }

    public boolean usesLegacySkillsFallback() {
        boolean hasSkills = skills != null && !skills.isBlank();
        boolean hasCapabilities = legacySkills != null && !legacySkills.isBlank();
        return !hasSkills && hasCapabilities;
    }
}
