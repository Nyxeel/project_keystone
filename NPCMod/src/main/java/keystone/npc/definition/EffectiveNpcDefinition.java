package keystone.npc.definition;

import java.util.Objects;

import keystone.npc.capabilities.CapabilitySet;

public record EffectiveNpcDefinition(
    NpcDefinition definition,
    CapabilitySet capabilities
) {
    public EffectiveNpcDefinition {
        definition = Objects.requireNonNull(definition, "definition");
        capabilities = Objects.requireNonNull(capabilities, "capabilities");
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
