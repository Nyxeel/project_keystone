package keystone.npc.combat;

public record CombatProfile(
    String id,
    Integer version,
    String mode,
    Double aggroRange,
    Double leashRange,
    String behaviorProfile,
    Double guardRadius,
    String patrolMode
) {
}