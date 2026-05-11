package keystone.npc.movement;

import java.util.List;

public record MovementProfile(
    String id,
    Integer version,
    List<MotionControllerDefinition> motionControllerList,
    List<InstructionDefinition> instructions
) {
    public MovementProfile {
        motionControllerList = motionControllerList == null ? List.of() : List.copyOf(motionControllerList);
        instructions = instructions == null ? List.of() : List.copyOf(instructions);
    }
}
