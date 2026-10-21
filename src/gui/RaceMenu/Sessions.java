package gui.RaceMenu;

/** The three qualifying sessions and their display names. */
final class Sessions {

    static final String[] CODES = { "Q1", "Q2", "Q3" };

    private Sessions() {
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