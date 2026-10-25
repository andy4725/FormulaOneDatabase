package gui.Start;

import gui.MainMenu.Theme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Panel with a rounded, filled background and an optional border line.
 */
class RoundedPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color fill;   // background colour
    private final Color border; // line colour, or null for no line
    private final int arc;      // how round the corners are (bigger = rounder)

    /** layout is used for the children, like with a normal JPanel. */
    RoundedPanel(LayoutManager layout, Color fill, Color border, int arc) {
        super(layout);
        this.fill = fill;
        this.border = border;
        this.arc = arc;
        // Not opaque: Swing must not fill the square corners outside the rounding.
        setOpaque(false);
    }

    /** Draws the rounded background; the children are drawn on top by Swing. */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            // 1 px less, so the border line fits inside the panel.
            int w = getWidth() - 1;
            int h = getHeight() - 1;

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w, h, arc, arc);
            }
        } finally {
            g2.dispose();
        }
    }
}