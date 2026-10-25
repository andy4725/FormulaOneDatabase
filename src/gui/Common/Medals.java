package gui.Common;

import java.awt.Color;

/** Gold, silver and bronze for the top 3, the same on every screen. */
public final class Medals {

    public static final Color GOLD   = new Color(212, 175, 55);
    public static final Color SILVER = new Color(192, 197, 206);
    public static final Color BRONZE = new Color(205, 127, 50);

    private Medals() {
        // Only static members; no instances needed.
    }

    /** The medal colour for P1-P3, null for everyone else. */
    public static Color forPosition(int position) {
        if (position == 1) {
            return GOLD;
        }
        if (position == 2) {
            return SILVER;
        }
        if (position == 3) {
            return BRONZE;
        }
        return null;
    }
}
