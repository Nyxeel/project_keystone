package keystone.npc.definition;

import java.util.Map;

public record NpcInstructionDefinition(
    Map<String, Object> sensor,
    Map<String, Object> bodyMotion
) {
}
