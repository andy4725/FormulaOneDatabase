package gui.DriversMenu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Finds and reads driver portrait images.
 *
 * Search order:
 * 1. The path stored in the driver data.
 * 2. The same file name inside assets/driver-portraits.
 * 3. A file in that folder whose name matches the driver's name.
 */
class Portraitloader {

    private final File projectRoot;       // the folder with "src" and "assets"
    private final File portraitDirectory; // assets/driver-portraits inside it

    Portraitloader() {
        projectRoot = findProjectRoot();
        portraitDirectory = new File(projectRoot, "assets/driver-portraits");
    }

    /** Returns the portrait, or null if no usable image is found. */
    BufferedImage load(String driverName, String portraitPath) {
        BufferedImage image = loadFromStoredPath(portraitPath);
        if (image == null) {
            image = loadByName(driverName);
        }
        if (image == null) {
            System.err.println("No readable portrait for " + driverName);
        }
        return image;
    }

    // ---------------- 1 and 2: stored path ----------------

    /** Tries the path from the database, then its file name in the portrait folder. */
    private BufferedImage loadFromStoredPath(String portraitPath) {
        // No path stored for this driver.
        if (portraitPath == null || portraitPath.trim().length() == 0) {
            return null;
        }

        File stored = new File(portraitPath);

        // Relative paths are resolved against the project root.
        File fromProject = stored.isAbsolute()
                ? stored
                : new File(projectRoot, portraitPath);

        // Also try the same file name inside the portrait folder.
        File fromFolder = new File(portraitDirectory, stored.getName());

        // The first file that can be read wins.
        File[] candidates = { fromProject, fromFolder };
        for (int i = 0; i < candidates.length; i++) {
            BufferedImage image = readImage(candidates[i]);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    // ---------------- 3: match by driver name ----------------

    /** Looks through every file in the portrait folder for one named after the driver. */
    private BufferedImage loadByName(String driverName) {
        // listFiles() returns null if the folder does not exist.
        File[] files = portraitDirectory.listFiles();
        if (files == null) {
            System.err.println("Portrait directory not found: "
                    + portraitDirectory);
            return null;
        }

        String wanted = normalize(driverName);

        // If several files match, keep the largest image.
        BufferedImage best = null;
        long bestPixels = 0L;

        for (int i = 0; i < files.length; i++) {
            // Split "max-verstappen.webp" into "max-verstappen" and ".webp".
            String filename = files[i].getName();
            int dot = filename.lastIndexOf('.');
            String base = dot < 0 ? filename : filename.substring(0, dot);
            String ext = dot < 0 ? ""
                    : filename.substring(dot).toLowerCase(Locale.ROOT);

            // .webp works because lib/webp-imageio adds WebP support to
            // ImageIO. All the portraits in assets/driver-portraits are .webp,
            // so without it this name search would never find anything.
            boolean supported = ext.equals(".png") || ext.equals(".jpg")
                    || ext.equals(".jpeg") || ext.equals(".webp");
            if (!supported || !normalize(base).equals(wanted)) {
                continue;
            }

            BufferedImage image = readImage(files[i]);
            if (image == null) {
                continue;
            }
            long pixels = (long) image.getWidth() * (long) image.getHeight();
            if (pixels > bestPixels) {
                bestPixels = pixels;
                best = image;
            }
        }
        return best;
    }

    // ---------------- Helpers ----------------

    /** Reads one file; returns null if it is missing or unreadable. */
    private BufferedImage readImage(File file) {
        if (!file.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (IOException exception) {
            System.err.println("Failed to read portrait: " + file);
            return null;
        }
    }

    /** "Max Verstappen" and "max_verstappen.png" both become "maxverstappen". */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        // NFD splits letters like "u with umlaut" into "u" + a separate accent
        // mark; \p{M} then removes those marks. After that only a-z and 0-9 stay.
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    /** Looks upwards from the current folder for one with "src" and "assets". */
    private File findProjectRoot() {
        // new File("") is the folder the program was started from. This way
        // the portraits are found even when it is started from bin/ or out/.
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