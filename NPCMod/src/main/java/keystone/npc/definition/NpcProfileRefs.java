package keystone.npc.definition;

public record NpcProfileRefs(
    String routine,
    String capabilities,
    String actions,
    String movement,
    String navigation,
    String combat,
    String spawn,
    String structure,
    String persistence
) {
}
