package gui.RaceMenu;

import java.util.Locale;

/** Turns seconds into lap-time text. */
final class TimeFormat {

    private TimeFormat() {
    }

    /** 92.481 becomes "1:32.481". */
    static String lap(double seconds) {
        long millis = Math.round(seconds * 1000.0);
        long minutes = millis / 60000;
        double rest = (millis % 60000) / 1000.0;
        return String.format(Locale.ROOT, "%d:%06.3f", minutes, rest);
    }

    /** 0.234 becomes "+0.234". */
    static String gap(double seconds) {
        return String.format(Locale.ROOT, "+%.3f", seconds);
    }
}