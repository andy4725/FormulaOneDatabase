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

    private final Color base;

    ActionButton(String text, Color base) {
        super(text);
        this.base = base;

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(Theme.font(Font.BOLD, 16));
        setForeground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setPreferredSize(new Dimension(200, 56));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            int w = getWidth();
            int h = getHeight();

            ButtonModel model = getModel();
            Color colour = base;
            if (model.isPressed()) {
                colour = Theme.blend(base, Color.BLACK, 0.25);
            } else if (model.isRollover()) {
                colour = Theme.blend(base, Color.WHITE, 0.15);
            }

            g2.setColor(colour);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

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