package keystone.npc.role;

/**
 * Per-role day/night schedule.
 *
 * sleepStartHour and wakeHour are in [0..23].
 * Example: sleepStart=21, wake=7 means sleeping from 21:00-06:59.
 */
public record RoleSchedule(int sleepStartHour, int wakeHour) {

    public static final RoleSchedule DEFAULT = new RoleSchedule(21, 7);

    public RoleSchedule {
        if (sleepStartHour < 0 || sleepStartHour > 23) {
            throw new IllegalArgumentException("sleepStartHour out of range: " + sleepStartHour);
        }
        if (wakeHour < 0 || wakeHour > 23) {
            throw new IllegalArgumentException("wakeHour out of range: " + wakeHour);
        }
    }

    public boolean isSleepingHour(int hour) {
        if (hour < 0 || hour > 23) {
            return false;
        }

        // Handles overnight windows (e.g., 21->7) and same-day windows.
        if (sleepStartHour <= wakeHour) {
            return hour >= sleepStartHour && hour < wakeHour;
        }

        return hour >= sleepStartHour || hour < wakeHour;
    }
}
