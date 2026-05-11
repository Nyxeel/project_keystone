package keystone.npc.persistence.profile;

public record PersistenceProfile(
    String id,
    Integer version,
    Boolean persistent,
    Boolean savePosition,
    Boolean saveState,
    Boolean saveHome,
    Boolean saveRoutineProgress,
    Boolean respawnAfterRestart,
    Boolean despawnWhenFarAway
) {
}
