package keystone.npc.routine;

public record RoutineEntry(
    String time,
    String targetMarker,
    String state,
    String action,
    Integer durationMinutes
) {
}
