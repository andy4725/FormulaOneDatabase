package gui.MainMenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

/**
 * Colours, fonts and small drawing/loading helpers shared by all screens.
 */
final class Theme {

    // ---------------- Colours ----------------
    static final Color HOME_BG    = new Color(15, 17, 22);
    static final Color SIDEBAR_BG = new Color(18, 20, 26);
    static final Color TILE_BG    = new Color(27, 30, 37);
    static final Color LINE       = new Color(42, 46, 56);
    static final Color NAV_HOVER  = new Color(32, 36, 45);
    static final Color F1_RED     = new Color(225, 6, 0);
    static final Color TEXT       = new Color(245, 246, 248);
    static final Color MUTED      = new Color(165, 170, 180);

    private Theme() {
        // Only static members; no instances needed.
    }

    // ---------------- Fonts and labels ----------------

    static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    static JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(style, size));
        label.setForeground(color);
        return label;
    }

    // ---------------- Colours and drawing ----------------

    /** 0.0 returns base, 1.0 returns accent. */
    static Color blend(Color base, Color accent, double amount) {
        int r = (int) (base.getRed() * (1.0 - amount) + accent.getRed() * amount);
        int g = (int) (base.getGreen() * (1.0 - amount) + accent.getGreen() * amount);
        int b = (int) (base.getBlue() * (1.0 - amount) + accent.getBlue() * amount);
        return new Color(r, g, b);
    }

    /** Smooth shapes, text and scaled images. */
    static void enableQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /** Draws an image so it fills the rectangle (cropping the extra). */
    static void drawCover(Graphics2D g, BufferedImage img,
            int x, int y, int w, int h) {
        double scale = Math.max((double) w / img.getWidth(),
                (double) h / img.getHeight());
        int dw = (int) Math.round(img.getWidth() * scale);
        int dh = (int) Math.round(img.getHeight() * scale);

        Shape oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        g.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
        g.setClip(oldClip);
    }

    // ---------------- Images ----------------

    /** Reads assets/name; returns null if it is missing or unreadable. */
    static BufferedImage loadImage(String name) {
        try {
            File file = new File("assets/" + name);
            if (!file.isFile()) {
                return null;
            }
            return ImageIO.read(file);
        } catch (Exception e) {
            return null;
        }
    }

    /** Tries assets/key.jpg, then assets/key.png. */
    static BufferedImage loadAny(String key) {
        BufferedImage image = loadImage(key + ".jpg");
        if (image == null) {
            image = loadImage(key + ".png");
        }
        return image;
    }
}