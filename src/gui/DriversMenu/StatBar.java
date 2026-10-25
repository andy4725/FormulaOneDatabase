package gui.DriversMenu;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 * One statistic: title, number and a coloured bar.
 *
 * The bar is also a slider: click or drag on it to change the value.
 * The screen is told about every change through the Listener.
 */
class StatBar extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Highest possible stat value. Change this if your stats use another scale. */
    static final int MAX_VALUE = 100;

    // Where the bar is drawn inside the component.
    private static final int BAR_Y = 24;
    private static final int BAR_H = 8;
    private static final int KNOB = 16; // diameter of the round handle

    /** Told when the player drags the bar to a new value. */
    interface Listener {
        void valueChanged(StatBar bar, int value);
    }

    private final String title;            // e.g. "PACE"
    private int value = -1;                // -1 = nothing selected yet
    private boolean changed;               // true = differs from the saved value
    private Color accent = DriverStyle.RED; // colour of the bar (the team colour)
    private Listener listener;

    StatBar(String title) {
        this.title = title;
        // Not opaque: the card behind shows through around the bar.
        setOpaque(false);
        setPreferredSize(new Dimension(200, 44));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Drag to change " + title.toLowerCase());

        // A click jumps to that spot; dragging keeps following the mouse.
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragTo(e.getX());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                dragTo(e.getX());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    int getValue() {
        return value;
    }

    /** Shows a new value in the given colour (called when another driver is selected). */
    void setValue(int value, Color accent) {
        this.value = value;
        this.accent = accent;
        this.changed = false;
        repaint(); // ask Swing to call paintComponent() again
    }

    /** Marks the number as edited (shown in the team colour) or saved (white). */
    void setChanged(boolean changed) {
        this.changed = changed;
        repaint();
    }

    /** Turns a mouse x position into a value 0..MAX_VALUE and reports it. */
    private void dragTo(int x) {
        // Nothing selected yet: there is no driver to change.
        if (value < 0 || !isEnabled()) {
            return;
        }
        int usable = Math.max(1, getWidth() - KNOB);
        int newValue = (int) Math.round((double) (x - KNOB / 2) * MAX_VALUE / usable);
        newValue = Math.max(0, Math.min(MAX_VALUE, newValue));
        if (newValue != value) {
            value = newValue;
            repaint();
            if (listener != null) {
                listener.valueChanged(this, value);
            }
        }
    }

    /** Draws title, number, the grey track, the filled part and the handle. */
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

            // Number on the right (a dash — while no driver is selected).
            // Right-aligned: x = full width minus the width of the text.
            // An unsaved number is shown in the team colour.
            String text = value < 0 ? "—" : Integer.toString(value);
            g2.setFont(DriverStyle.font(Font.BOLD, 14));
            g2.setColor(changed ? accent : DriverStyle.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, w - fm.stringWidth(text), 14);

            // Bar track.
            g2.setColor(new Color(45, 49, 59));
            g2.fillRoundRect(0, BAR_Y, w, BAR_H, 8, 8);

            if (value < 0) {
                return;
            }

            // Filled part up to the middle of the handle. The handle moves
            // between KNOB/2 and w - KNOB/2, so it never sticks out of the bar.
            // The gradient goes from a darker team colour to the full team colour.
            double ratio = Math.min(1.0, (double) value / MAX_VALUE);
            int knobX = (int) Math.round((w - KNOB) * ratio);
            int fill = Math.max(BAR_H, knobX + KNOB / 2);
            g2.setPaint(new GradientPaint(0, 0,
                    DriverStyle.blend(accent, Color.BLACK, 0.35),
                    w, 0, accent));
            g2.fillRoundRect(0, BAR_Y, fill, BAR_H, 8, 8);

            // Round white handle with a team-coloured ring.
            int knobY = BAR_Y + BAR_H / 2 - KNOB / 2;
            g2.setColor(accent);
            g2.fillOval(knobX, knobY, KNOB, KNOB);
            g2.setColor(DriverStyle.TEXT);
            g2.fillOval(knobX + 3, knobY + 3, KNOB - 6, KNOB - 6);
        } finally {
            g2.dispose();
        }
    }
}
