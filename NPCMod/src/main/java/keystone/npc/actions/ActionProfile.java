package keystone.npc.actions;

import java.util.Map;

public record ActionProfile(
    String id,
    Integer version,
    Map<String, ActionDefinition> actions
) {
}
