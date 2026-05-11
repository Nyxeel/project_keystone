package keystone.npc.capabilities;

import java.util.Objects;

import keystone.npc.definition.NpcTemplateResolver;

public final class CapabilityChecks {
    private final NpcTemplateResolver templateResolver;

    public CapabilityChecks(NpcTemplateResolver templateResolver) {
        this.templateResolver = Objects.requireNonNull(templateResolver, "templateResolver");
    }

    public boolean hasOrDefault(String definitionId, NpcCapability capability, boolean defaultValue) {
        return templateResolver.resolveById(definitionId)
            .map(effective -> effective.capabilities().has(capability))
            .orElse(defaultValue);
    }
}