package keystone.npc.routine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RoutineRunner {
    public Optional<RoutineEntry> findActiveEntry(RoutineDefinition routineDefinition, int minuteOfDay) {
        if (routineDefinition == null || routineDefinition.schedule().isEmpty()) {
            return Optional.empty();
        }

        List<EntrySlot> slots = new ArrayList<>();
        for (RoutineEntry entry : routineDefinition.schedule()) {
            if (entry == null || entry.time() == null || entry.time().isBlank()) {
                continue;
            }
            parseTimeToMinuteOfDay(entry.time()).ifPresent(minute -> slots.add(new EntrySlot(minute, entry)));
        }

        if (slots.isEmpty()) {
            return Optional.empty();
        }

        slots.sort(Comparator.comparingInt(EntrySlot::startMinute));
        EntrySlot active = slots.get(slots.size() - 1);
        for (EntrySlot slot : slots) {
            if (slot.startMinute() <= minuteOfDay) {
                active = slot;
            } else {
                break;
            }
        }

        return Optional.of(active.entry());
    }

    private Optional<Integer> parseTimeToMinuteOfDay(String raw) {
        String[] parts = raw.trim().split(":");
        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return Optional.empty();
            }
            return Optional.of((hour * 60) + minute);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private record EntrySlot(int startMinute, RoutineEntry entry) {
    }
}
