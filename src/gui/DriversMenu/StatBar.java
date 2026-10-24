package gui.DriversMenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * One statistic: title, number and a coloured bar.
 */
class StatBar extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Highest possible stat value. Change this if your stats use another scale. */
    static final int MAX_VALUE = 100;

    private final String title;            // e.g. "PACE"
    private int value = -1;                // -1 = nothing selected yet
    private Color accent = DriverStyle.RED; // colour of the bar (the team colour)

    StatBar(String title) {
        this.title = title;
        // Not opaque: the card behind shows through around the bar.
        setOpaque(false);
        setPreferredSize(new Dimension(200, 44));
    }

    /** Shows a new value in the given colour (called when another driver is selected). */
    void setValue(int value, Color accent) {
        this.value = value;
        this.accent = accent;
        repaint(); // ask Swing to call paintComponent() again
    }

    /** Draws title, number, the grey track and the filled part on top of it. */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            DriverStyle.enableQuality(g2);
            int w = getWidth();

            // Title on the left.
            g2.setFont(DriverStyle.font(Font.BOLD, 10));
            g2.setColor(DriverStyle.MUTED);
            g2.drawString(title, 0, 12);

            // Number on the right (a dash \u2014 while no driver is selected).
            // Right-aligned: x = full width minus the width of the text.
            String text = value < 0 ? "\u2014" : Integer.toString(value);
            g2.setFont(DriverStyle.font(Font.BOLD, 14));
            g2.setColor(DriverStyle.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, w - fm.stringWidth(text), 14);

            // Bar track.
            int barY = 24;
            int barH = 8;
            g2.setColor(new Color(45, 49, 59));
            g2.fillRoundRect(0, barY, w, barH, 8, 8);

            // Filled part: value / MAX_VALUE of the width (e.g. 80 -> 80%),
            // never wider than the track and never shorter than its own height,
            // so even a small value shows as a round dot.
            // The gradient goes from a darker team colour to the full team colour.
            if (value > 0) {
                double ratio = Math.min(1.0, (double) value / MAX_VALUE);
                int fill = Math.max(barH, (int) Math.round(w * ratio));
                g2.setPaint(new GradientPaint(0, 0,
                        DriverStyle.blend(accent, Color.BLACK, 0.35),
                        w, 0, accent));
                g2.fillRoundRect(0, barY, fill, barH, 8, 8);
            }
        } finally {
            g2.dispose();
        }
    }
}