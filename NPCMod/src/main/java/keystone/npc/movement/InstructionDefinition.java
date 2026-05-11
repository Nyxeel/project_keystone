package keystone.npc.movement;

import java.util.Map;

public record InstructionDefinition(
    Map<String, Object> sensor,
    Map<String, Object> bodyMotion
) {
}
