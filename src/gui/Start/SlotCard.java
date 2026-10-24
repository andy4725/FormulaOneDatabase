package gui.Start;

import gui.MainMenu.Theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * One save slot: red disk icon, "SLOT n" and its status ("Empty" ...).
 *
 * - Click the card: start the game with this slot.
 * - Click the bin in the top-right corner (only on a used slot): ask for
 *   confirmation, then delete the slot and its saved results.
 */
class SlotCard extends JPanel {

    private static final long serialVersionUID = 1L;

    private final int slot;  // 1, 2 or 3
    private boolean used;    // true if the slot holds a game (then the bin is shown)
    private String status;   // second line, e.g. "Empty" or "Last played: ..."

    // Mouse state; paintComponent() uses these to choose the colours.
    private boolean hover = false;           // mouse is over the card
    private boolean pressed = false;         // mouse button held down on the card
    private boolean pressedOnDelete = false; // mouse button held down on the bin
    private boolean deleteHover = false;     // mouse is over the bin

    /** listener (the main window) is told when this slot is chosen. */
    SlotCard(final int slot, boolean used, String status,
            final IntroListener listener) {
        this.slot = slot;
        this.used = used;
        this.status = status;

        // Not opaque: the rounded corners show the box behind the card.
        setOpaque(false);
        setPreferredSize(new Dimension(240, 124));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // One MouseAdapter handles both kinds of events: clicks/enter/exit
        // (MouseListener) and movement (MouseMotionListener, for mouseMoved).
        MouseAdapter mouse = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                deleteHover = false;
                repaint();
            }

            // Highlight the bin (and show a tooltip) only while the mouse
            // is over it; repaint only when that actually changes.
            public void mouseMoved(MouseEvent e) {
                boolean over = overDelete(e);
                if (over != deleteHover) {
                    deleteHover = over;
                    setToolTipText(over ? "Delete this slot" : null);
                    repaint();
                }
            }

            // A click counts only if the mouse is pressed AND released on the
            // same spot. Remember here where the press started (bin or card),
            // and decide what to do in mouseReleased.
            public void mousePressed(MouseEvent e) {
                pressedOnDelete = overDelete(e);
                pressed = !pressedOnDelete;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                // Released on the bin: ask before deleting.
                if (pressedOnDelete) {
                    pressedOnDelete = false;
                    if (overDelete(e)) {
                        confirmDelete();
                    }
                    repaint();
                    return;
                }

                // hover is false if the mouse was dragged off the card before
                // releasing; then it was not a real click.
                boolean click = pressed && hover;
                pressed = false;
                repaint();
                if (click) {
                    listener.onSlotChosen(slot);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // =====================================================
    // DELETE
    // =====================================================

    /** Area of the bin icon in the top-right corner. */
    private Rectangle deleteArea() {
        // 32x32, 8 px from the top and right edges.
        return new Rectangle(getWidth() - 40, 8, 32, 32);
    }

    /** True if the mouse is on the bin (an empty slot has no bin). */
    private boolean overDelete(MouseEvent e) {
        return used && deleteArea().contains(e.getPoint());
    }

    /** Asks the player, and deletes the slot only after "Delete". */
    private void confirmDelete() {
        Object[] options = { "Delete", "Cancel" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Delete slot " + slot + "?\n"
                        + "All results saved in it will be lost.",
                "Delete slot",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]);   // "Cancel" is the default answer

        // 0 is the first option ("Delete"); Cancel or closing the dialog stops here.
        if (choice != 0) {
            return;
        }

        if (SaveSlots.delete(slot)) {
            // Show the card as empty straight away, without reloading the screen.
            used = false;
            status = "Empty";
            deleteHover = false;
            setToolTipText(null);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                    "The slot could not be deleted. Check the database.",
                    "Delete slot", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================
    // PAINTING
    // =====================================================

    /** Draws the card: background, border, disk icon, texts and the bin. */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            // 1 px less, so the border fits inside the card.
            int w = getWidth() - 1;
            int h = getHeight() - 1;

            // Card background and border (tinted red on hover, more when pressed).
            Color bg = Theme.HOME_BG;
            if (pressed) {
                bg = Theme.blend(bg, Theme.F1_RED, 0.18);
            } else if (hover) {
                bg = Theme.blend(bg, Theme.F1_RED, 0.08);
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            g2.setColor(hover ? Theme.F1_RED : new Color(62, 66, 78));
            g2.drawRoundRect(0, 0, w, h, 12, 12);

            // Icon, title and status, placed at 30%, 62% and 82% of the height.
            drawDisk(g2, w / 2, (int) (h * 0.30));

            g2.setFont(Theme.font(Font.BOLD, 18));
            g2.setColor(Theme.TEXT);
            centred(g2, "SLOT " + slot, w / 2, (int) (h * 0.62));

            g2.setFont(Theme.font(Font.PLAIN, 13));
            g2.setColor(Theme.MUTED);
            centred(g2, status, w / 2, (int) (h * 0.82));

            if (used) {
                drawBin(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    /** Draws text with its middle at centreX; baseline is the bottom of the letters. */
    private void centred(Graphics2D g2, String text, int centreX,
            int baseline) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, centreX - fm.stringWidth(text) / 2, baseline);
    }

    /** Small floppy-disk icon in red, centred on (cx, cy). */
    private void drawDisk(Graphics2D g2, int cx, int cy) {
        // Own copy of the Graphics, so the thicker line (stroke) does not stay set.
        Graphics2D icon = (Graphics2D) g2.create();
        try {
            icon.setStroke(new BasicStroke(2.2f)); // lines 2.2 px thick
            icon.setColor(Theme.F1_RED);

            int x = cx - 13;
            int y = cy - 13;
            icon.drawRoundRect(x, y, 26, 26, 6, 6);          // outer shape
            icon.drawRect(x + 7, y, 11, 8);                  // metal shutter
            icon.drawRoundRect(x + 5, y + 15, 16, 11, 3, 3); // label
        } finally {
            icon.dispose();
        }
    }

    /** Small bin icon; grey, red when the pointer is over it. */
    private void drawBin(Graphics2D g2) {
        Rectangle area = deleteArea();
        Graphics2D icon = (Graphics2D) g2.create();
        try {
            icon.setStroke(new BasicStroke(1.8f));
            icon.setColor(deleteHover ? Theme.F1_RED : Theme.MUTED);

            int x = area.x + 8;
            int y = area.y + 6;
            icon.drawLine(x - 1, y + 3, x + 17, y + 3);       // lid
            icon.drawRect(x + 5, y, 7, 3);                    // handle
            icon.drawRoundRect(x + 1, y + 6, 14, 13, 3, 3);   // body
            // two vertical stripes on the body
            icon.drawLine(x + 6, y + 9, x + 6, y + 16);
            icon.drawLine(x + 10, y + 9, x + 10, y + 16);
        } finally {
            icon.dispose();
        }
    }
}