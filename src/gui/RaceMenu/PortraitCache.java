package gui.RaceMenu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import model.Driver;

/**
 * Loads driver portraits once and keeps them in memory.
 *
 * Search order:
 * 1. The portrait path stored for the driver (relative to the project).
 * 2. assets/driver-portraits/first-last.webp (or .png / .jpg).
 */
public final class PortraitCache {

    // File types tried in step 2, in this order.
    private static final String[] EXTENSIONS = { ".webp", ".png", ".jpg" };

    private final File projectRoot = findProjectRoot();
    // driver_id -> portrait (null if none was found).
    private final Map<Integer, BufferedImage> cache =
            new HashMap<Integer, BufferedImage>();

    /** Returns the portrait, or null if none could be read. */
    public BufferedImage get(Driver driver) {
        Integer id = Integer.valueOf(driver.getDriverId());
        // containsKey (not get() != null), so "no portrait" is remembered
        // too and the disk is not searched again for that driver.
        if (!cache.containsKey(id)) {
            cache.put(id, load(driver));
        }
        return cache.get(id);
    }

    /** Reads the portrait from disk, following the search order above. */
    private BufferedImage load(Driver driver) {
        // 1. The stored path; a relative one is resolved against the project folder.
        String stored = driver.getPortraitPath();
        if (stored != null && stored.trim().length() > 0) {
            File file = new File(stored);
            if (!file.isAbsolute()) {
                file = new File(projectRoot, stored);
            }
            BufferedImage image = read(file);
            if (image != null) {
                return image;
            }
        }

        // 2. A file named after the driver in the portrait folder.
        File folder = new File(projectRoot, "assets/driver-portraits");
        String slug = slug(driver.getName());
        for (String extension : EXTENSIONS) {
            BufferedImage image = read(new File(folder, slug + extension));
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    /** Reads one file; returns null if it is missing or unreadable. */
    private BufferedImage read(File file) {
        if (!file.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            return null;
        }
    }

    /** "Nico Hülkenberg" becomes "nico-hulkenberg". */
    private String slug(String name) {
        String plain = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return plain.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    /** Looks upwards from the current folder for one with "src" and "assets". */
    private static File findProjectRoot() {
        File current = new File("").getAbsoluteFile();
        for (File dir = current; dir != null; dir = dir.getParentFile()) {
            if (new File(dir, "src").isDirectory()
                    && new File(dir, "assets").isDirectory()) {
                return dir;
            }
        }
        return current;
    }
}