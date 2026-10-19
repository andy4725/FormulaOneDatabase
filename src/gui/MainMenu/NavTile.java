package gui.MainMenu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Tall rounded card on the home screen: picture (or big icon) on top,
 * title, description and a round arrow button. Highlights on hover and
 * opens its section when clicked.
 *
 * Optional picture: assets/drivers.jpg, teams.jpg, race.jpg, standings.jpg
 */
class NavTile extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String description; // "|" marks a line break
    private final Color accent;
    private final int iconType;
    private final BufferedImage picture;

    private boolean hover = false;
    private boolean pressed = false;

    NavTile(final Navigator navigator, final Section section,
            String title, String description, Color accent) {
        this.title = title;
        this.description = description;
        this.accent = accent;
        this.iconType = section.icon;
        this.picture = Theme.loadAny(section.imageName());

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                boolean click = pressed && hover;
                pressed = false;
                repaint();
                if (click) {
                    navigator.showSection(section);
                }
            }
        });
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            int w = getWidth() - 1;
            int h = getHeight() - 1;

            // Everything inside the card is clipped to rounded corners.
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));

            Color bg = cardColour();
            g2.setColor(bg);
            g2.fillRect(0, 0, w + 1, h + 1);

            paintArtwork(g2, w, h, bg);

            // Coloured stripe on the left edge.
            g2.setColor(accent);
            g2.fillRect(0, 0, 5, h + 1);

            g2.setClip(null);

            // Border.
            g2.setColor(hover ? accent : Theme.blend(Theme.TILE_BG, accent, 0.35));
            g2.drawRoundRect(0, 0, w, h, 18, 18);

            paintText(g2, h);
            paintArrow(g2, w, h);
        } finally {
            g2.dispose();
        }
    }

    /** Card colour: slightly tinted on hover, more when pressed. */
    private Color cardColour() {
        if (pressed) {
            return Theme.blend(Theme.TILE_BG, accent, 0.18);
        }
        if (hover) {
            return Theme.blend(Theme.TILE_BG, accent, 0.08);
        }
        return Theme.TILE_BG;
    }

    /** Top part: the picture, or a coloured gradient with a big icon. */
    private void paintArtwork(Graphics2D g2, int w, int h, Color bg) {
        int artH = (int) (h * 0.62);

        if (picture != null) {
            Theme.drawCover(g2, picture, 0, 0, w, artH);
        } else {
            g2.setPaint(new GradientPaint(0, 0,
                    Theme.blend(Theme.TILE_BG, accent, 0.45), 0, artH, bg));
            g2.fillRect(0, 0, w + 1, artH);

            Graphics2D big = (Graphics2D) g2.create();
            big.translate(w / 2, artH * 0.45);
            big.scale(2.6, 2.6);
            big.setColor(Theme.blend(Theme.TILE_BG, accent, 0.55));
            Icons.drawIcon(big, iconType, 0, 0);
            big.dispose();
        }

        // Fade the artwork into the card body.
        g2.setPaint(new GradientPaint(0, artH * 0.45f,
                new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0),
                0, artH, bg));
        g2.fillRect(0, 0, w + 1, artH + 1);
    }

    /** Small icon, title and description, positioned from the bottom. */
    private void paintText(Graphics2D g2, int h) {
        String[] lines = description.split("\\|");
        int lineHeight = 18;
        int lastBase = h - 78;
        int firstBase = lastBase - (lines.length - 1) * lineHeight;
        int titleBase = firstBase - 24;

        Graphics2D small = (Graphics2D) g2.create();
        small.translate(44, titleBase - 46);
        small.scale(0.9, 0.9);
        small.setColor(accent);
        Icons.drawIcon(small, iconType, 0, 0);
        small.dispose();

        g2.setFont(Theme.font(Font.BOLD, 18));
        g2.setColor(Theme.TEXT);
        g2.drawString(title, 28, titleBase);

        g2.setFont(Theme.font(Font.PLAIN, 13));
        g2.setColor(Theme.MUTED);
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], 28, firstBase + i * lineHeight);
        }
    }

    /** Round arrow button in the bottom-right corner (filled on hover). */
    private void paintArrow(Graphics2D g2, int w, int h) {
        int d = 36;
        int x = w - 24 - d;
        int y = h - 24 - d;

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(accent);
        if (hover) {
            g2.fillOval(x, y, d, d);
        } else {
            g2.drawOval(x, y, d, d);
        }

        g2.setFont(Theme.font(Font.BOLD, 18));
        g2.setColor(hover ? Color.WHITE : Theme.TEXT);
        FontMetrics fm = g2.getFontMetrics();
        String arrow = "\u2192";
        g2.drawString(arrow,
                x + (d - fm.stringWidth(arrow)) / 2,
                y + (d + fm.getAscent() - fm.getDescent()) / 2);
    }
}