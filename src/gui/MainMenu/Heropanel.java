package gui.MainMenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Banner at the top of the home screen: logo, title, tagline and
 * red diagonal shapes. Optional images: assets/f1-logo.png, assets/hero.jpg.
 *
 * Nothing on it is a Swing component: everything is drawn by hand
 * in paintComponent(), which Swing calls whenever the banner must be shown.
 */
class Heropanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Loaded once when the banner is made; null if the file is missing.
    private final BufferedImage logo = Theme.loadImage("f1-logo.png");
    private final BufferedImage picture = Theme.loadAny("hero");

    Heropanel() {
        setOpaque(true);
        setBackground(Theme.HOME_BG);
    }

    /** Fixed banner height, so the cards below can never squash it. */
    public Dimension getPreferredSize() {
        return new Dimension(100, 220);
    }

    /** Same as the preferred size: the banner never gets lower than 220 px. */
    public Dimension getMinimumSize() {
        return new Dimension(100, 220);
    }

    /** Draws the whole banner, back to front: background, red shapes, texts. */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics, so our settings (quality, colours)
        // do not leak into other components. finally makes sure the copy
        // is released even if drawing throws an exception.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Theme.enableQuality(g2);
            int w = getWidth();
            int h = getHeight();

            paintBackground(g2, w, h);
            paintRedShapes(g2, w);
            paintTexts(g2);
        } finally {
            g2.dispose();
        }
    }

    /** Dark gradient (towards red in the bottom right), with the optional photo on top. */
    private void paintBackground(Graphics2D g2, int w, int h) {
        // The gradient goes from the top-left corner (0, 0) to the bottom-right (w, h).
        g2.setPaint(new GradientPaint(0, 0, new Color(12, 14, 19),
                w, h, new Color(38, 10, 12)));
        g2.fillRect(0, 0, w, h);

        if (picture != null) {
            Theme.drawCover(g2, picture, 0, 0, w, h);
            // Fade the left side so the text stays readable.
            // The last colour has alpha 0, so the fade ends fully transparent.
            g2.setPaint(new GradientPaint(0, 0, Theme.HOME_BG,
                    w * 0.75f, 0, new Color(15, 17, 22, 0)));
            g2.fillRect(0, 0, w, h);
        }
    }

    /** A red triangle in the top-right corner and a lighter diagonal band next to it. */
    private void paintRedShapes(Graphics2D g2, int w) {
        // F1 red; the fourth number (alpha) makes it slightly see-through.
        g2.setColor(new Color(225, 6, 0, 200));
        Polygon corner = new Polygon();
        corner.addPoint(w - 170, 0);
        corner.addPoint(w, 0);
        corner.addPoint(w, 170);
        g2.fillPolygon(corner);

        // A wider, much more transparent stripe parallel to the triangle.
        g2.setColor(new Color(225, 6, 0, 80));
        Polygon band = new Polygon();
        band.addPoint(w - 260, 0);
        band.addPoint(w - 205, 0);
        band.addPoint(w, 205);
        band.addPoint(w, 260);
        g2.fillPolygon(band);
    }

    /** Logo, big title and the tagline under it, all 40 px from the left. */
    private void paintTexts(Graphics2D g2) {
        int x = 40;

        // Logo, or "F1" text if there is no image.
        if (logo != null) {
            // 56 px high; the width keeps the logo's own proportions.
            int lh = 56;
            int lw = logo.getWidth() * lh / logo.getHeight();
            g2.drawImage(logo, x, 28, lw, lh, null);
        } else {
            g2.setFont(Theme.font(Font.BOLD | Font.ITALIC, 54));
            g2.setColor(Theme.F1_RED);
            g2.drawString("F1", x, 80);
        }

        // drawString's y is the baseline of the text (the bottom of the letters).
        g2.setFont(Theme.font(Font.BOLD, 42));
        g2.setColor(Theme.TEXT);
        g2.drawString("F1 2026 SIMULATOR", x, 150);

        g2.setFont(Theme.font(Font.PLAIN, 13));
        g2.setColor(Theme.MUTED);
        g2.drawString("BUILD TEAMS   /   MANAGE DRIVERS   /   RACE   /   WIN",
                x + 2, 182);
    }
}
