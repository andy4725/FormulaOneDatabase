package gui.Start;

import gui.MainMenu.Theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ButtonModel;
import javax.swing.JButton;

/**
 * Flat rounded button with hover and pressed colours (red for START).
 */
class ActionButton extends JButton {

    private static final long serialVersionUID = 1L;

    // The normal colour; hover and pressed colours are made from it.
    private final Color base;

    ActionButton(String text, Color base) {
        super(text);
        this.base = base;

        // Switch off everything Swing normally draws for a button
        // (background, border, focus box); paintComponent() draws it instead.
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true); // lets the model know when the mouse is over it
        setFont(Theme.font(Font.BOLD, 16));
        setForeground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 56 px high; in a BoxLayout it may grow as wide as its column.
        setPreferredSize(new Dimension(200, 56));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /** Draws the rounded background and the centred text. */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            int w = getWidth();
            int h = getHeight();

            // The button model knows if the button is pressed or hovered:
            // pressed = 25% darker, hovered = 15% lighter.
            ButtonModel model = getModel();
            Color colour = base;
            if (model.isPressed()) {
                colour = Theme.blend(base, Color.BLACK, 0.25);
            } else if (model.isRollover()) {
                colour = Theme.blend(base, Color.WHITE, 0.15);
            }

            g2.setColor(colour);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Centre the text: FontMetrics gives its width and height.
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(),
                    (w - fm.stringWidth(getText())) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
        } finally {
            g2.dispose();
        }
    }
}