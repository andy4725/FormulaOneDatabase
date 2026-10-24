package gui.RaceMenu;

import java.util.Locale;

/**
 * Turns seconds into lap-time text.
 *
 * All methods first round to whole milliseconds, so 92.4809 shows as
 * 1:32.481. Locale.ROOT makes sure the decimal sign is always a dot,
 * also on computers set to a language that uses a comma.
 */
public final class TimeFormat {

    private TimeFormat() {
        // Only static methods; no instances needed.
    }

    /** 92.481 becomes "1:32.481". */
    public static String lap(double seconds) {
        // %06.3f = seconds with 3 decimals, padded to 6 characters ("05.123").
        long millis = Math.round(seconds * 1000.0);
        long minutes = millis / 60000;
        double rest = (millis % 60000) / 1000.0;
        return String.format(Locale.ROOT, "%d:%06.3f", minutes, rest);
    }

    /** 0.234 becomes "+0.234". */
    public static String gap(double seconds) {
        return String.format(Locale.ROOT, "+%.3f", seconds);
    }

    /** 5532.481 becomes "1:32:12.481" (race time with hours). */
    public static String total(double seconds) {
        long millis = Math.round(seconds * 1000.0);
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        double rest = (millis % 60000) / 1000.0;
        return String.format(Locale.ROOT, "%d:%02d:%06.3f", hours, minutes, rest);
    }
}
