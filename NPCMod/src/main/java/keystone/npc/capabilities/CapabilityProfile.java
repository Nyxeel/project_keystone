package keystone.npc.capabilities;

import java.util.Map;

record CapabilityProfile(
    String id,
    Integer version,
    Map<String, Boolean> capabilities
) {
}
