package keystone.npc.actions;

public record ActionDefinition(
    String animation,
    Boolean loop,
    String sound,
    Double soundIntervalSeconds,
    String requiresCapability
) {
}
