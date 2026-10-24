package gui.Start;

import gui.MainMenu.Theme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Picture on the start screen, with rounded corners.
 *
 * Put your picture in assets/start.jpg (or start.png). If it is missing,
 * assets/hero.jpg is used, and without any picture a dark red design
 * with assets/f1-logo.png (or a big "F1" text) is drawn.
 */
class ArtPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // The picture to show, or null if neither start nor hero exists.
    private final BufferedImage picture;

    // Only used by the fallback design; null if there is no logo file.
    private final BufferedImage logo = Theme.loadImage("f1-logo.png");

    ArtPanel() {
        // Try start.jpg/png first, then hero.jpg/png.
        BufferedImage image = Theme.loadAny("start");
        if (image == null) {
            image = Theme.loadAny("hero");
        }
        picture = image;

        // Not opaque, so the card behind shows in the rounded corners.
        setOpaque(false);
        setMinimumSize(new Dimension(300, 200));
    }

    /** Draws the picture (or the fallback design) inside rounded corners. */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            int w = getWidth();
            int h = getHeight();

            // Everything drawn after this is cut to a rounded rectangle.
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

            if (picture != null) {
                Theme.drawCover(g2, picture, 0, 0, w, h);
            } else {
                paintFallback(g2, w, h);
            }
        } finally {
            g2.dispose();
        }
    }

    /** Simple design used when no picture file exists. */
    private void paintFallback(Graphics2D g2, int w, int h) {
        // Almost black in the top left, dark red in the bottom right.
        g2.setPaint(new GradientPaint(0, 0, new Color(8, 9, 12),
                w, h, new Color(60, 8, 10)));
        g2.fillRect(0, 0, w, h);

        // Red triangle in the top-right corner (alpha 170 = slightly see-through)...
        g2.setColor(new Color(225, 6, 0, 170));
        Polygon corner = new Polygon();
        corner.addPoint(w - 200, 0);
        corner.addPoint(w, 0);
        corner.addPoint(w, 200);
        g2.fillPolygon(corner);

        // ...and a fainter diagonal band next to it.
        g2.setColor(new Color(225, 6, 0, 70));
        Polygon band = new Polygon();
        band.addPoint(w - 300, 0);
        band.addPoint(w - 240, 0);
        band.addPoint(w, 240);
        band.addPoint(w, 300);
        g2.fillPolygon(band);

        // Logo in the middle, or "F1" text if there is no logo file.
        if (logo != null) {
            // At most 3/5 of the panel wide; the height keeps the logo's proportions.
            int lw = Math.min(logo.getWidth(), w * 3 / 5);
            int lh = logo.getHeight() * lw / logo.getWidth();
            g2.drawImage(logo, (w - lw) / 2, (h - lh) / 2, lw, lh, null);
            return;
        }

        String text = "F1";
        g2.setFont(Theme.font(Font.BOLD | Font.ITALIC, 150));
        g2.setColor(Theme.F1_RED);
        // Centred horizontally with the text width from FontMetrics.
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (w - fm.stringWidth(text)) / 2,
                h / 2 + fm.getAscent() / 3);
    }
}