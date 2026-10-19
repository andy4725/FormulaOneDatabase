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
class Statbar extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Highest possible stat value. Change this if your stats use another scale. */
    static final int MAX_VALUE = 100;

    private final String title;
    private int value = -1; // -1 = nothing selected yet
    private Color accent = DriverStyle.RED;

    Statbar(String title) {
        this.title = title;
        setOpaque(false);
        setPreferredSize(new Dimension(200, 44));
    }

    void setValue(int value, Color accent) {
        this.value = value;
        this.accent = accent;
        repaint();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            DriverStyle.enableQuality(g2);
            int w = getWidth();

            // Title on the left.
            g2.setFont(DriverStyle.font(Font.BOLD, 10));
            g2.setColor(DriverStyle.MUTED);
            g2.drawString(title, 0, 12);

            // Number on the right.
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

            // Filled part.
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