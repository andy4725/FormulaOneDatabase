package gui.MainMenu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * Simple icons drawn with shapes, so no image files are needed.
 */
final class Icons {

    // The icon types. Section stores one of these numbers for each screen.
    static final int DRIVER    = 0;
    static final int TEAMS     = 1;
    static final int RACE      = 2;
    static final int STANDINGS = 3;
    static final int HOME      = 4;
    static final int RESULTS   = 5;

    private Icons() {
        // Only static methods; no instances needed.
    }

    /** Turns an icon into an ImageIcon (used by the sidebar buttons). */
    static ImageIcon makeIcon(int type, Color color) {
        // A transparent 34x34 picture (ARGB = with alpha), with the icon
        // drawn in its centre (17, 17).
        BufferedImage image = new BufferedImage(34, 34,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            Theme.enableQuality(g);
            g.setColor(color);
            drawIcon(g, type, 17, 17);
        } finally {
            g.dispose();
        }
        return new ImageIcon(image);
    }

    /**
     * Draws an icon centred on (cx, cy) in the current colour.
     * All icons are about 32 px wide, built from filled rectangles, ovals and arcs.
     */
    static void drawIcon(Graphics2D g, int type, int cx, int cy) {
        switch (type) {
        case DRIVER: // head and shoulders
            g.fillOval(cx - 7, cy - 15, 14, 14);
            g.fillArc(cx - 13, cy + 1, 26, 26, 0, 180);
            break;
        case TEAMS: // two people
            for (int dx = -9; dx <= 9; dx += 18) {
                g.fillOval(cx + dx - 5, cy - 13, 10, 10);
                g.fillArc(cx + dx - 10, cy - 1, 20, 20, 0, 180);
            }
            break;
        case RACE: // chequered flag
            g.fillRect(cx - 16, cy - 14, 2, 28); // the pole
            // 3 x 4 squares; only every second one is filled, like a chessboard.
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 4; c++) {
                    if ((r + c) % 2 == 0) {
                        g.fillRect(cx - 13 + c * 7, cy - 14 + r * 7, 7, 7);
                    }
                }
            }
            break;
        case HOME: // roof and walls
            Polygon roof = new Polygon();
            roof.addPoint(cx - 15, cy);
            roof.addPoint(cx, cy - 14);
            roof.addPoint(cx + 15, cy);
            g.fillPolygon(roof);
            g.fillRect(cx - 10, cy, 20, 13);
            break;
        case RESULTS: // list: three rows of a dot and a line
            for (int r = 0; r < 3; r++) {
                int y = cy - 12 + r * 10;
                g.fillRect(cx - 15, y, 5, 5);
                g.fillRect(cx - 7, y, 22, 5);
            }
            break;
        default: // STANDINGS: podium
            g.fillRect(cx - 16, cy, 10, 13);
            g.fillRect(cx - 5, cy - 11, 10, 24);
            g.fillRect(cx + 6, cy - 5, 10, 18);
            break;
        }
    }
}