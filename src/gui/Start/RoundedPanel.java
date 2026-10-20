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

    private final Color fill;
    private final Color border;
    private final int arc;

    RoundedPanel(LayoutManager layout, Color fill, Color border, int arc) {
        super(layout);
        this.fill = fill;
        this.border = border;
        this.arc = arc;
        setOpaque(false);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
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