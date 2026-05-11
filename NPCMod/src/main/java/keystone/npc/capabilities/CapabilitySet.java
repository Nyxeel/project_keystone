package keystone.npc.capabilities;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CapabilitySet {
    private static final CapabilitySet EMPTY = new CapabilitySet(EnumSet.noneOf(NpcCapability.class));
    private final EnumSet<NpcCapability> enabled;

    private CapabilitySet(EnumSet<NpcCapability> enabled) {
        this.enabled = enabled;
    }

    public static CapabilitySet empty() {
        return EMPTY;
    }

    public static CapabilitySet fromBooleanMap(Map<String, Boolean> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }

        EnumSet<NpcCapability> enabled = EnumSet.noneOf(NpcCapability.class);
        for (Map.Entry<String, Boolean> entry : values.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }

            NpcCapability.tryParse(entry.getKey()).ifPresent(enabled::add);
        }
        return enabled.isEmpty() ? empty() : new CapabilitySet(enabled);
    }

    public boolean has(NpcCapability capability) {
        return enabled.contains(Objects.requireNonNull(capability, "capability"));
    }

    public Set<NpcCapability> allEnabled() {
        return Set.copyOf(enabled);
    }
}
