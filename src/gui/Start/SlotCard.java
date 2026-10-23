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

    private final int slot;
    private boolean used;
    private String status;

    private boolean hover = false;
    private boolean pressed = false;
    private boolean pressedOnDelete = false;
    private boolean deleteHover = false;

    SlotCard(final int slot, boolean used, String status,
            final IntroListener listener) {
        this.slot = slot;
        this.used = used;
        this.status = status;

        setOpaque(false);
        setPreferredSize(new Dimension(240, 124));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
        return new Rectangle(getWidth() - 40, 8, 32, 32);
    }

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

        if (choice != 0) {
            return;
        }

        if (SaveSlots.delete(slot)) {
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

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            int w = getWidth() - 1;
            int h = getHeight() - 1;

            // Card background and border.
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

            // Icon, title and status.
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

    private void centred(Graphics2D g2, String text, int centreX,
            int baseline) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, centreX - fm.stringWidth(text) / 2, baseline);
    }

    /** Small floppy-disk icon in red, centred on (cx, cy). */
    private void drawDisk(Graphics2D g2, int cx, int cy) {
        Graphics2D icon = (Graphics2D) g2.create();
        try {
            icon.setStroke(new BasicStroke(2.2f));
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
            icon.drawLine(x + 6, y + 9, x + 6, y + 16);
            icon.drawLine(x + 10, y + 9, x + 10, y + 16);
        } finally {
            icon.dispose();
        }
    }
}