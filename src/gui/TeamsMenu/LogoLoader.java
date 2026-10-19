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
 * PNG, JPG and GIF are supported. SVG and WebP cannot be read by plain
 * Java, so those logos are skipped (initials or a dot are shown instead).
 */
class LogoLoader {

    private final File projectRoot;
    private final File[] folders;

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

    private BufferedImage loadFromStoredPath(String storedPath) {
        if (storedPath == null || storedPath.trim().length() == 0) {
            return null;
        }

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

    private BufferedImage loadByName(String name) {
        String wanted = normalize(name);
        if (wanted.length() == 0) {
            return null;
        }

        for (int f = 0; f < folders.length; f++) {
            File[] files = folders[f].listFiles();
            if (files == null) {
                continue;
            }
            for (int i = 0; i < files.length; i++) {
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
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    /** Looks upwards from the current folder for one with "src" and "assets". */
    private File findProjectRoot() {
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