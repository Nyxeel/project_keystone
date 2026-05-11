package keystone.npc.definition;

public record NpcMotionControllerDefinition(
    String type,
    Double maxWalkSpeed,
    Double gravity,
    Double maxFallSpeed,
    Double acceleration
) {
}
