package gui.Start;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import repository.SaveSlotRepository;

/**
 * What the screens use to work with the save slots. It calls the
 * database (SaveSlotRepository) and turns errors into safe values, so
 * the screens never have to deal with SQLException.
 */
public final class SaveSlots {

    /** Number of slots shown on the slot screen. */
    static final int COUNT = 3;

    private static final SaveSlotRepository REPOSITORY =
            new SaveSlotRepository();

    private SaveSlots() {
    }

    /** True if the slot holds a game. */
    static boolean isUsed(int slot) {
        try {
            return REPOSITORY.lastPlayed(slot) != null;
        } catch (SQLException exception) {
            report("read slot " + slot, exception);
            return false;
        }
    }

    /** "Empty", or when the slot was last played. */
    static String status(int slot) {
        try {
            String raw = REPOSITORY.lastPlayed(slot);
            return raw == null ? "Empty" : "Last played: " + niceDate(raw);
        } catch (SQLException exception) {
            report("read slot " + slot, exception);
            return "Database unavailable";
        }
    }

    /** Remembers that the slot was chosen (creates it if it was empty). */
    public static void markUsed(int slot) {
        try {
            REPOSITORY.touch(slot);
        } catch (SQLException exception) {
            report("open slot " + slot, exception);
        }
    }

    /**
     * Saves a result (for example a race) in the slot.
     * Returns false if the database could not be written.
     */
    public static boolean saveResult(int slot, String title, String details) {
        try {
            REPOSITORY.saveResult(slot, title, details);
            return true;
        } catch (SQLException exception) {
            report("save result in slot " + slot, exception);
            return false;
        }
    }

    /** Results saved in the slot, newest first: {title, details, savedAt}. */
    public static List<String[]> listResults(int slot) {
        try {
            return REPOSITORY.listResults(slot);
        } catch (SQLException exception) {
            report("read results of slot " + slot, exception);
            return new ArrayList<String[]>();
        }
    }

    /** Deletes the slot and its results. Returns false on a database error. */
    static boolean delete(int slot) {
        try {
            REPOSITORY.delete(slot);
            return true;
        } catch (SQLException exception) {
            report("delete slot " + slot, exception);
            return false;
        }
    }

    // ---------------- Helpers ----------------

    /** "2026-10-20 11:08:31" becomes "20 Oct 2026". */
    private static String niceDate(String raw) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat out =
                    new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            return out.format(in.parse(raw));
        } catch (ParseException exception) {
            return raw;
        }
    }

    private static void report(String action, SQLException exception) {
        System.err.println("Could not " + action + ": "
                + exception.getMessage());
    }
}