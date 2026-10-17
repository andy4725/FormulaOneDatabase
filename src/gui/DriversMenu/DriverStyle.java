package gui.DriversMenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Colours, fonts and small helpers shared by the drivers screen.
 */
final class DriverStyle {

    static final Color BACKGROUND    = new Color(15, 17, 22);
    static final Color CARD          = new Color(27, 30, 37);
    static final Color CARD_SELECTED = new Color(38, 42, 52);
    static final Color LINE          = new Color(42, 46, 56);
    static final Color RED           = new Color(225, 6, 0);
    static final Color TEXT          = new Color(245, 246, 248);
    static final Color MUTED         = new Color(165, 170, 180);

    // Stand-in team colours: every team ID always gets the same colour.
    // If your Team class has a real colour, return that in teamColour().
    private static final Color[] TEAM_COLOURS = {
        new Color(225, 6, 0),
        new Color(54, 113, 198),
        new Color(0, 190, 170),
        new Color(255, 128, 0),
        new Color(34, 153, 84),
        new Color(155, 90, 255),
        new Color(255, 135, 188),
        new Color(100, 196, 255),
        new Color(230, 190, 40),
        new Color(182, 186, 189)
    };

    private DriverStyle() {
    }

    static Color teamColour(int teamId) {
        return TEAM_COLOURS[Math.abs(teamId) % TEAM_COLOURS.length];
    }

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

    /** 0.0 returns base, 1.0 returns accent. */
    static Color blend(Color base, Color accent, double amount) {
        int r = (int) (base.getRed() * (1.0 - amount) + accent.getRed() * amount);
        int g = (int) (base.getGreen() * (1.0 - amount) + accent.getGreen() * amount);
        int b = (int) (base.getBlue() * (1.0 - amount) + accent.getBlue() * amount);
        return new Color(r, g, b);
    }
}