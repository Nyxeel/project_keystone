package keystone.npc.roles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import keystone.npc.markers.MarkerType;

final class RoleDefinitionParsingSupport {
    DailyRoutine mergeSchedule(RoleDefinition base, PersistedSchedule override) {
        int sleepStart = base != null ? base.schedule().sleepStartHour() : DailyRoutine.DEFAULT.sleepStartHour();
        int wakeHour = base != null ? base.schedule().wakeHour() : DailyRoutine.DEFAULT.wakeHour();

        if (override != null) {
            if (override.sleepStartHour() != null) {
                sleepStart = override.sleepStartHour();
            }
            if (override.wakeHour() != null) {
                wakeHour = override.wakeHour();
            }
        }

        return new DailyRoutine(sleepStart, wakeHour);
    }

    Set<MarkerType> parseMarkers(List<String> rawMarkers) {
        if (rawMarkers == null) {
            return null;
        }

        if (rawMarkers.isEmpty()) {
            return Collections.emptySet();
        }

        EnumSet<MarkerType> markers = EnumSet.noneOf(MarkerType.class);
        for (String marker : rawMarkers) {
            if (marker == null || marker.isBlank()) {
                continue;
            }
            try {
                markers.add(MarkerType.valueOf(marker.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                System.err.println("[KeystoneNPC] Ignoring unknown marker type in role file: " + marker);
            }
        }
        return markers;
    }

    String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "Unknown";
    }

    String toTitleCase(String roleId) {
        String[] parts = roleId.split("[_-]");
        List<String> out = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            out.add(part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1));
        }
        return out.stream().collect(Collectors.joining(" "));
    }
}
