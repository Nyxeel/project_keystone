package keystone.npc.routine;

import java.util.List;

public record RoutineDefinition(
    String id,
    Integer version,
    List<RoutineEntry> schedule
) {
    public RoutineDefinition {
        schedule = schedule == null ? List.of() : List.copyOf(schedule);
    }
}
