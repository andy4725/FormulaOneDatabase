package gui.DriversMenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Thin dark scroll bar with a rounded thumb and no arrow buttons.
 */
class DarkScrollBarUI extends BasicScrollBarUI {

    protected JButton createDecreaseButton(int orientation) {
        return emptyButton();
    }

    protected JButton createIncreaseButton(int orientation) {
        return emptyButton();
    }

    protected void configureScrollBarColors() {
        thumbColor = new Color(82, 86, 96);
        trackColor = DriverStyle.CARD;
    }

    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        g.setColor(DriverStyle.CARD);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
        if (bounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            DriverStyle.enableQuality(g2);
            if (isDragging) {
                g2.setColor(DriverStyle.RED);
            } else if (isThumbRollover()) {
                g2.setColor(new Color(190, 20, 16));
            } else {
                g2.setColor(thumbColor);
            }
            g2.fillRoundRect(bounds.x + 2, bounds.y + 2,
                    bounds.width - 4, bounds.height - 4, 8, 8);
        } finally {
            g2.dispose();
        }
    }

    private JButton emptyButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setFocusable(false);
        return button;
    }
}