package keystone.npc.definition;

import java.util.Objects;

import keystone.npc.skills.SkillSet;

public record EffectiveNpcDefinition(
    NpcDefinition definition,
    SkillSet skills
) {
    public EffectiveNpcDefinition {
        definition = Objects.requireNonNull(definition, "definition");
        skills = Objects.requireNonNull(skills, "skills");
    }

    public String id() {
        return definition.id();
    }

    public String roleId() {
        return definition.effectiveRoleId();
    }

    public String displayName() {
        return definition.effectiveDisplayName();
    }
}
