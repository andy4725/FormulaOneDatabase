package gui.TeamsMenu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Finds and reads team and sponsor logos.
 *
 * Search order:
 * 1. The path stored in the database (relative to the project folder).
 * 2. The same file name inside the logo folders listed below.
 * 3. A file in those folders whose name matches the team/sponsor name.
 *
 * Steps 1 and 2 read whatever ImageIO can read: PNG, JPG, GIF, and also
 * WebP thanks to lib/webp-imageio (the team logos in the database are .webp).
 * Step 3 only looks at PNG, JPG and GIF files. SVG cannot be read at all;
 * then initials or a dot are shown instead.
 */
class LogoLoader {

    private final File projectRoot; // the folder with "src" and "assets"
    private final File[] folders;   // where to look for logos, in this order

    // Remembers results so a logo is read from disk only once.
    private final Map<String, BufferedImage> cache =
            new HashMap<String, BufferedImage>();

    LogoLoader() {
        projectRoot = findProjectRoot();

        // Add or rename folders here if your logos are somewhere else.
        folders = new File[] {
            new File(projectRoot, "assets/team-logos"),
            new File(projectRoot, "assets/sponsor-logos"),
            new File(projectRoot, "assets/logos"),
            new File(projectRoot, "assets")
        };
    }

    /** Returns the logo, or null if no usable image is found. */
    BufferedImage load(String storedPath, String name) {
        // containsKey (not get() != null), so a logo that was NOT found is
        // remembered too and not searched for again.
        String key = storedPath + "|" + name;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        BufferedImage image = loadFromStoredPath(storedPath);
        if (image == null) {
            image = loadByName(name);
        }
        cache.put(key, image);
        return image;
    }

    // ---------------- 1 and 2: stored path ----------------

    /** Tries the path from the database, then its file name in each logo folder. */
    private BufferedImage loadFromStoredPath(String storedPath) {
        // No path stored.
        if (storedPath == null || storedPath.trim().length() == 0) {
            return null;
        }

        // Relative paths are resolved against the project root.
        File stored = new File(storedPath);
        File fromProject = stored.isAbsolute()
                ? stored
                : new File(projectRoot, storedPath);

        BufferedImage image = readImage(fromProject);
        if (image != null) {
            return image;
        }

        // Same file name inside each logo folder.
        for (int i = 0; i < folders.length; i++) {
            image = readImage(new File(folders[i], stored.getName()));
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    // ---------------- 3: match by name ----------------

    /** Looks through every logo folder for a file named after the team/sponsor. */
    private BufferedImage loadByName(String name) {
        String wanted = normalize(name);
        if (wanted.length() == 0) {
            return null;
        }

        for (int f = 0; f < folders.length; f++) {
            // listFiles() returns null if the folder does not exist; skip it.
            File[] files = folders[f].listFiles();
            if (files == null) {
                continue;
            }
            for (int i = 0; i < files.length; i++) {
                // Split "red-bull.png" into "red-bull" and ".png";
                // files without an extension are skipped.
                String filename = files[i].getName();
                int dot = filename.lastIndexOf('.');
                if (dot < 0) {
                    continue;
                }
                String ext = filename.substring(dot).toLowerCase(Locale.ROOT);
                boolean supported = ext.equals(".png") || ext.equals(".jpg")
                        || ext.equals(".jpeg") || ext.equals(".gif");

                if (supported
                        && normalize(filename.substring(0, dot)).equals(wanted)) {
                    BufferedImage image = readImage(files[i]);
                    if (image != null) {
                        return image;
                    }
                }
            }
        }
        return null;
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
            System.err.println("Failed to read logo: " + file);
            return null;
        }
    }

    /** "Red Bull" and "red_bull.png" both become "redbull". */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        // NFD splits accented letters into the letter plus a separate accent
        // mark; \p{M} then removes those marks. After that only a-z and 0-9 stay.
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    /** Looks upwards from the current folder for one with "src" and "assets". */
    private File findProjectRoot() {
        // new File("") is the folder the program was started from.
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