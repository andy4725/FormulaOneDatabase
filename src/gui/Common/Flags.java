package gui.Common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;

/**
 * Country flags for the circuits, read from assets/flags/country-name.png
 * ("United Kingdom" -> united-kingdom.png). To add a country, drop its
 * flag into that folder with the matching file name.
 */
public final class Flags {

    private static final File FOLDER = new File(findProjectRoot(), "assets/flags");

    // "country|height" -> icon (null if the country has no flag file).
    private static final Map<String, Icon> CACHE = new HashMap<String, Icon>();

    private Flags() {
        // Only static members; no instances needed.
    }

    /** The country's flag scaled to the given height, or null if there is none. */
    public static Icon get(String country, int height) {
        String key = country + "|" + height;
        // containsKey (not get() != null), so a missing flag is remembered too.
        if (!CACHE.containsKey(key)) {
            CACHE.put(key, load(country, height));
        }
        return CACHE.get(key);
    }

    /** Reads the file and scales it, keeping the flag's own width/height ratio. */
    private static Icon load(String country, int height) {
        if (country == null) {
            return null;
        }
        File file = new File(FOLDER, slug(country) + ".png");
        if (!file.isFile()) {
            return null;
        }
        BufferedImage source;
        try {
            source = ImageIO.read(file);
        } catch (IOException exception) {
            System.err.println("Failed to read flag: " + file);
            return null;
        }
        if (source == null) {
            return null;
        }

        int width = Math.max(1, source.getWidth() * height / source.getHeight());
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, width, height, null);
        // Thin dark edge, so white parts (Japan, Monaco...) don't melt into light text.
        g.setColor(new Color(0, 0, 0, 70));
        g.drawRect(0, 0, width - 1, height - 1);
        g.dispose();
        return new ImageIcon(scaled);
    }

    /** "United Kingdom" -> "united-kingdom"; accents are dropped. */
    private static String slug(String text) {
        String decomposed = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    /** Looks upwards from the current folder for one with "src" and "assets". */
    private static File findProjectRoot() {
        File current = new File("").getAbsoluteFile();
        File dir = current;
        while (dir != null) {
            if (new File(dir, "src").isDirectory()
                    && new File(dir, "assets").isDirectory()) {
                return dir;
            }
            dir = dir.getParentFile();
        }
        return current;
    }
}
