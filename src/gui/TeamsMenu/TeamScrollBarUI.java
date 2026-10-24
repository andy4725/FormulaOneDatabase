package gui.TeamsMenu;

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
 * The same as DarkScrollBarUI on the Drivers screen, but with the
 * colours of TeamStyle.
 *
 * Track = the long background strip; thumb = the part you drag.
 */
class TeamScrollBarUI extends BasicScrollBarUI {

    /** The arrow button at the top: replaced by an invisible one. */
    protected JButton createDecreaseButton(int orientation) {
        return emptyButton();
    }

    /** The arrow button at the bottom: replaced by an invisible one. */
    protected JButton createIncreaseButton(int orientation) {
        return emptyButton();
    }

    /** Colours used by the parent class. */
    protected void configureScrollBarColors() {
        thumbColor = new Color(82, 86, 96);
        trackColor = TeamStyle.CARD;
    }

    /** The track is a plain rectangle in the card colour, so it blends in. */
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        g.setColor(TeamStyle.CARD);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /** Rounded thumb: grey normally, dark red under the mouse, red while dragged. */
    protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
        // Nothing to draw if everything fits (no scrolling needed).
        if (bounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            TeamStyle.enableQuality(g2);
            // isDragging and isThumbRollover() come from BasicScrollBarUI.
            if (isDragging) {
                g2.setColor(TeamStyle.RED);
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

    /** A 0x0 button that can never get focus, i.e. a button nobody can see. */
    private JButton emptyButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setFocusable(false);
        return button;
    }
}