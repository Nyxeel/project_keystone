package keystone.npc.definition;

public record NpcDebugDefinition(
    Boolean showMarkers,
    Boolean logRoutineChanges,
    Boolean logCapabilityChecks,
    Boolean logMotionChanges
) {
}
