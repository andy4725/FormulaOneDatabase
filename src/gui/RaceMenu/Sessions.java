package gui.RaceMenu;

/** The three qualifying sessions and their display names. */
final class Sessions {

    // The codes as stored in the database (qualifying_laps.session).
    // The index in this array (0, 1, 2) is used everywhere on the screen.
    static final String[] CODES = { "Q1", "Q2", "Q3" };

    private Sessions() {
        // Only static members; no instances needed.
    }

    /** 0 becomes "Quali 1". */
    static String name(int index) {
        return "Quali " + (index + 1);
    }

    /** "Q2" becomes 1. */
    static int indexOf(String code) {
        for (int i = 0; i < CODES.length; i++) {
            if (CODES[i].equals(code)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown session " + code);
    }
}