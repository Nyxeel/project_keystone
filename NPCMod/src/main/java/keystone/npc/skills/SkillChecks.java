package keystone.npc.skills;

import java.util.Objects;

import keystone.npc.definition.NpcTemplateResolver;

public final class SkillChecks {
    private final NpcTemplateResolver templateResolver;

    public SkillChecks(NpcTemplateResolver templateResolver) {
        this.templateResolver = Objects.requireNonNull(templateResolver, "templateResolver");
    }

    public boolean hasOrDefault(String definitionId, NpcSkill skill, boolean defaultValue) {
        return templateResolver.resolveById(definitionId)
            .map(effective -> effective.skills().has(skill))
            .orElse(defaultValue);
    }
}