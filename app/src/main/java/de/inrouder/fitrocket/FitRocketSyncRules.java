package de.inrouder.fitrocket;

/** Idempotent Health Connect imports: compare workout timestamps, type and duration. */
public final class FitRocketSyncRules {
    private FitRocketSyncRules() {}

    public static boolean isDuplicate(String savedDay, String importedDay, int savedType, int importedType,
                                      int savedMinutes, int importedMinutes, long savedStartMs, long importedStartMs) {
        if (savedDay == null || !savedDay.equals(importedDay)) return false;
        if (savedMinutes <= 0 || importedMinutes <= 0) return false;
        if (savedStartMs > 0 && importedStartMs > 0) {
            return Math.abs(savedStartMs - importedStartMs) < 12L * 60_000
                    && Math.abs(savedMinutes - importedMinutes) < 12;
        }
        return savedType == importedType && Math.abs(savedMinutes - importedMinutes) <= 5;
    }

    public static boolean isSameWeek(String day, String monday, String followingMonday) {
        return day != null && day.compareTo(monday) >= 0 && day.compareTo(followingMonday) < 0;
    }
}
