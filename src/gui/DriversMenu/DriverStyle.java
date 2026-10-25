package gui.DriversMenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

/**
 * Colours, fonts and small helpers shared by the drivers screen.
 */
final class DriverStyle {

    static final Color BACKGROUND    = new Color(15, 17, 22);    // screen background
    static final Color CARD          = new Color(27, 30, 37);    // cards and list rows
    static final Color CARD_SELECTED = new Color(38, 42, 52);    // the selected list row
    static final Color LINE          = new Color(42, 46, 56);    // thin dividers
    static final Color RED           = new Color(225, 6, 0);     // F1 red accent
    static final Color TEXT          = new Color(245, 246, 248); // normal text
    static final Color MUTED         = new Color(165, 170, 180); // grey secondary text

    // Real team colours, keyed by team_id from the teams table.
    // To add a team, add one more put(...) line.
    private static final Map<Integer, Color> TEAM_COLOURS =
            new HashMap<Integer, Color>();
    // A static block runs once, when the class is first used.
    static {
        TEAM_COLOURS.put(1,  new Color(255, 128, 0));   // McLaren (papaya)
        TEAM_COLOURS.put(2,  new Color(232, 0, 45));    // Ferrari (rosso)
        TEAM_COLOURS.put(3,  new Color(39, 244, 210));  // Mercedes (teal)
        TEAM_COLOURS.put(4,  new Color(54, 113, 198));  // Red Bull Racing (blue)
        TEAM_COLOURS.put(5,  new Color(102, 146, 255)); // Racing Bulls (light blue)
        TEAM_COLOURS.put(6,  new Color(24, 104, 219));  // Williams (blue)
        TEAM_COLOURS.put(7,  new Color(34, 153, 113));  // Aston Martin (green)
        TEAM_COLOURS.put(8,  new Color(255, 135, 188)); // Alpine (pink)
        TEAM_COLOURS.put(9,  new Color(182, 186, 189)); // Haas (grey)
        TEAM_COLOURS.put(10, new Color(255, 45, 0));    // Audi (red-orange)
        TEAM_COLOURS.put(11, new Color(212, 175, 55));  // Cadillac (gold)
    }

    private DriverStyle() {
        // Only static members; no instances needed.
    }

    /** The team's colour, or grey for a team that is not in the list. */
    static Color teamColour(int teamId) {
        Color colour = TEAM_COLOURS.get(teamId);
        return colour != null ? colour : MUTED;
    }

    /** The screen's font; style is Font.PLAIN, Font.BOLD or Font.ITALIC. */
    static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    /** Smooth shapes, text and scaled images. */
    static void enableQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Mixes two colours. 0.0 returns base, 1.0 returns accent. */
    static Color blend(Color base, Color accent, double amount) {
        int r = (int) (base.getRed() * (1.0 - amount) + accent.getRed() * amount);
        int g = (int) (base.getGreen() * (1.0 - amount) + accent.getGreen() * amount);
        int b = (int) (base.getBlue() * (1.0 - amount) + accent.getBlue() * amount);
        return new Color(r, g, b);
    }
}