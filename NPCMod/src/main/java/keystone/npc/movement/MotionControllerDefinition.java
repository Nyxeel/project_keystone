package keystone.npc.movement;

public record MotionControllerDefinition(
    String type,
    Double maxWalkSpeed,
    Double gravity,
    Double maxFallSpeed,
    Double acceleration
) {
}
