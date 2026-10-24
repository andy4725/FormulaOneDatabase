package gui.ResultsMenu;

import gui.RaceMenu.TimeFormat;
import java.util.List;

import model.RaceResultEntry;

/**
 * The text rules of the Results screen, without any Swing.
 * Because nothing here draws, these can be checked on their own.
 */
final class ResultsFormat {

    private ResultsFormat() {
        // Only static methods; no instances needed.
    }

    /** Readable text for a race that cannot be opened yet (season_events.status). */
    static String statusText(String status) {
        if ("RACE_IN_PROGRESS".equals(status)) {
            return "Race in progress";
        }
        if (status != null && status.startsWith("QUALIFYING")) {
            return "Qualifying";
        }
        return "Not raced yet";
    }

    /** True for a driver who finished and has a position (not DNF or DSQ). */
    static boolean isClassified(RaceResultEntry entry) {
        return "FINISHED".equals(entry.getStatus()) && entry.getFinishingPosition() > 0;
    }

    /** The first classified finisher, or null if nobody finished. Gaps are measured to them. */
    static RaceResultEntry winner(List<RaceResultEntry> entries) {
        for (RaceResultEntry entry : entries) {
            if (isClassified(entry)) {
                return entry;
            }
        }
        return null;
    }

    /** "3", or the status (DNF / DSQ) when the driver has no position. */
    static String position(RaceResultEntry entry) {
        int position = entry.getFinishingPosition();
        return position > 0 ? String.valueOf(position) : entry.getStatus();
    }

    /** Race time for the winner, "+gap" or "+N laps" for the rest, status for DNF/DSQ. */
    static String timeText(RaceResultEntry entry, RaceResultEntry winner) {
        if (!isClassified(entry)) {
            return entry.getStatus();
        }
        if (entry.getLapCount() == 0) {
            return "-";
        }
        if (entry == winner) {
            return TimeFormat.total(entry.getTotalTimeSeconds());
        }
        int lapsDown = winner.getLapCount() - entry.getLapCount();
        if (lapsDown > 0) {
            return "+" + lapsDown + (lapsDown == 1 ? " lap" : " laps");
        }
        return TimeFormat.gap(entry.getTotalTimeSeconds() - winner.getTotalTimeSeconds());
    }

    /**
     * Places gained since the start: positive = gained, negative = lost,
     * 0 = same place or unknown grid / finishing position.
     */
    static int placesGained(RaceResultEntry entry) {
        int grid = entry.getGridPosition();
        int finish = entry.getFinishingPosition();
        if (grid <= 0 || finish <= 0) {
            return 0;
        }
        return grid - finish;
    }
}
