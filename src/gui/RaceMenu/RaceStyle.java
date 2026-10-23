package gui.RaceMenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

/**
 * Colours, fonts and small helpers shared by the race weekend screen.
 * Same palette as the other screens.
 */
final class RaceStyle {

    static final Color BACKGROUND = new Color(15, 17, 22);
    static final Color CARD       = new Color(27, 30, 37);
    static final Color CARD_DARK  = new Color(21, 23, 29);
    static final Color LINE       = new Color(42, 46, 56);
    static final Color RED        = new Color(225, 6, 0);
    static final Color TEXT       = new Color(245, 246, 248);
    static final Color MUTED      = new Color(165, 170, 180);

    private RaceStyle() {
    }

    static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    static JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(style, size));
        label.setForeground(color);
        return label;
    }

    /** Smooth shapes, text and scaled images. */
    static void enableQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}