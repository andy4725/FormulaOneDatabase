package gui.TeamsMenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Colours, fonts and small helpers shared by the teams screen.
 */
final class TeamStyle {

    static final Color BACKGROUND    = new Color(15, 17, 22);    // screen background
    static final Color CARD          = new Color(27, 30, 37);    // cards and list rows
    static final Color CARD_SELECTED = new Color(38, 42, 52);    // selected row, sponsor rows
    static final Color LINE          = new Color(42, 46, 56);    // thin dividers
    static final Color RED           = new Color(225, 6, 0);     // F1 red accent
    static final Color TEXT          = new Color(245, 246, 248); // normal text
    static final Color MUTED         = new Color(165, 170, 180); // grey secondary text

    // Real team colours by team ID (same as on the drivers screen).
    // Also used as the background behind each team logo.
    private static final Map<Integer, Color> TEAM_COLOURS =
            new HashMap<Integer, Color>();
    // A static block runs once, when the class is first used.
    static {
        TEAM_COLOURS.put(1,  new Color(255, 128, 0));   // McLaren (papaya)
        TEAM_COLOURS.put(2,  new Color(232, 0, 45));    // Ferrari (rosso)
        TEAM_COLOURS.put(3,  new Color(39, 244, 210));  // Mercedes (teal)
        TEAM_COLOURS.put(4,  new Color(54, 113, 198));  // Red Bull Racing (blue)
        TEAM_COLOURS.put(5,  new Color(102, 146, 255)); // Racing Bulls (light blue)
        TEAM_COLOURS.put(6,  new Color(24, 104, 219));  // Williams (blue)
        TEAM_COLOURS.put(7,  new Color(34, 153, 113));  // Aston Martin (green)
        TEAM_COLOURS.put(8,  new Color(255, 135, 188)); // Alpine (pink)
        TEAM_COLOURS.put(9,  new Color(182, 186, 189)); // Haas (grey)
        TEAM_COLOURS.put(10, new Color(255, 45, 0));    // Audi (red-orange)
        TEAM_COLOURS.put(11, new Color(212, 175, 55));  // Cadillac (gold)
    }

    private TeamStyle() {
        // Only static members; no instances needed.
    }

    /** The team's colour, or grey for a team that is not in the list. */
    static Color teamColour(int teamId) {
        Color colour = TEAM_COLOURS.get(teamId);
        return colour != null ? colour : MUTED;
    }

    /** The screen's font; style is Font.PLAIN, Font.BOLD or Font.ITALIC. */
    static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    /** Smooth shapes, text and scaled images. */
    static void enableQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Mixes two colours. 0.0 returns base, 1.0 returns accent. */
    static Color blend(Color base, Color accent, double amount) {
        int r = (int) (base.getRed() * (1.0 - amount) + accent.getRed() * amount);
        int g = (int) (base.getGreen() * (1.0 - amount) + accent.getGreen() * amount);
        int b = (int) (base.getBlue() * (1.0 - amount) + accent.getBlue() * amount);
        return new Color(r, g, b);
    }

    /** "Red Bull Racing" becomes "RB", "Ferrari" becomes "FE". */
    static String initials(String name) {
        if (name == null || name.trim().length() == 0) {
            return "";
        }
        // Split on any amount of spaces. One word: its first two letters;
        // more words: the first letter of the first two words.
        String[] words = name.trim().split("\\s+");
        String result;
        if (words.length == 1) {
            result = words[0].substring(0, Math.min(2, words[0].length()));
        } else {
            result = words[0].substring(0, 1) + words[1].substring(0, 1);
        }
        return result.toUpperCase(Locale.ROOT);
    }

    /** Shortens text with "..." until it fits the width. */
    static String fit(Graphics2D g2, String text, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String result = text;
        // Remove one letter at a time until the text plus "..." fits.
        while (result.length() > 3 && fm.stringWidth(result) > maxWidth) {
            result = result.substring(0, result.length() - 1);
            if (fm.stringWidth(result + "...") <= maxWidth) {
                return result.trim() + "...";
            }
        }
        return result;
    }

    // Team principal (director) of every team. Key: team name, lower case,
    // no spaces. To change a director, edit the matching put(...) line.
    private static final Map<String, String> DIRECTORS =
            new HashMap<String, String>();
    static {
        DIRECTORS.put("mclaren", "Andrea Stella");
        DIRECTORS.put("ferrari", "Fred Vasseur");
        DIRECTORS.put("mercedes", "Toto Wolff");
        DIRECTORS.put("redbullracing", "Laurent Mekies");
        DIRECTORS.put("racingbulls", "Alan Permane");
        DIRECTORS.put("williams", "James Vowles");
        DIRECTORS.put("astonmartin", "Adrian Newey");
        DIRECTORS.put("alpine", "Flavio Briatore");
        DIRECTORS.put("haas", "Ayao Komatsu");
        DIRECTORS.put("audi", "Jonathan Wheatley");
        DIRECTORS.put("cadillac", "Graeme Lowdon");
    }

    /** Director of the team, or "-" if the team is not in the list. */
    static String director(String teamName) {
        if (teamName == null) {
            return "-";
        }
        // "Red Bull Racing" -> "redbullracing", the form used as key above.
        String key = teamName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        String result = DIRECTORS.get(key);
        return result == null ? "-" : result;
    }

    /** "Lando Norris  /  Oscar Piastri", or "-" without drivers. */
    static String joinNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "-";
        }
        return String.join("  /  ", names);
    }

    // Sponsors that always get a fixed plate colour (instead of the
    // automatic one). Key: part of the sponsor name, lower case, no spaces.
    // To add another sponsor, add one more put(...) line.
    private static final Map<String, Color> PLATE_OVERRIDES =
            new HashMap<String, Color>();
    static {
        PLATE_OVERRIDES.put("microsoft", new Color(82, 86, 96)); // grey
    }

    /** Fixed plate colour for this sponsor, or null for the automatic one. */
    static Color plateOverride(String name) {
        if (name == null) {
            return null;
        }
        // contains(), not equals(): "Microsoft Azure" also matches "microsoft".
        String key = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        for (Map.Entry<String, Color> entry : PLATE_OVERRIDES.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    // Remembers the plate colour chosen for each logo, so the pixels are
    // only checked once. "Weak": an entry disappears by itself when its
    // logo is no longer used anywhere, so this map cannot grow forever.
    private static final Map<BufferedImage, Color> PLATE_COLOURS =
            new WeakHashMap<BufferedImage, Color>();

    /**
     * Picks a plate colour that makes the logo visible:
     * - logo with its own solid background: the plate takes that colour,
     *   so the background blends in instead of showing as a box;
     * - light logo on a transparent background: dark plate;
     * - any other logo: light plate.
     */
    private static Color plateColour(BufferedImage logo) {
        Color cached = PLATE_COLOURS.get(logo);
        if (cached != null) {
            return cached;
        }

        int w = logo.getWidth();
        int h = logo.getHeight();

        // A solid background means all four corners are opaque.
        int[] corners = {
            logo.getRGB(0, 0), logo.getRGB(w - 1, 0),
            logo.getRGB(0, h - 1), logo.getRGB(w - 1, h - 1)
        };
        // A pixel is one int: alpha, red, green, blue (8 bits each).
        // ">>> 24" moves the alpha byte down, so it is 0 (transparent) to 255 (solid).
        boolean solidBackground = true;
        for (int i = 0; i < corners.length; i++) {
            if ((corners[i] >>> 24) < 250) {
                solidBackground = false;
            }
        }

        Color result;
        if (solidBackground) {
            // "& 0xFFFFFF" keeps only red, green and blue (drops alpha).
            result = new Color(corners[0] & 0xFFFFFF);
        } else {
            // Average brightness of the visible pixels (sampled).
            // Only about every 20th pixel in each direction is checked,
            // which is enough and much faster than checking all of them.
            long sum = 0;
            int count = 0;
            int step = Math.max(1, Math.min(w, h) / 20);
            for (int y = 0; y < h; y += step) {
                for (int x = 0; x < w; x += step) {
                    int argb = logo.getRGB(x, y);
                    // Only pixels that are at least half visible count.
                    // 0.299/0.587/0.114 is the standard formula for how bright
                    // a colour looks to the eye (green looks brightest).
                    if ((argb >>> 24) >= 128) {
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;
                        sum += (int) (0.299 * r + 0.587 * g + 0.114 * b);
                        count++;
                    }
                }
            }
            // Brightness is 0-255; above 170 counts as a light logo.
            boolean lightLogo = count > 0 && sum / count > 170;
            result = lightLogo ? new Color(20, 22, 28)
                               : new Color(245, 246, 248);
        }

        PLATE_COLOURS.put(logo, result);
        return result;
    }

    /**
     * Draws a logo centred inside a rounded plate. The plate colour is
     * chosen so the logo is always visible (see plateColour).
     */
    static void drawLogo(Graphics2D g, BufferedImage logo,
            int x, int y, int w, int h) {
        drawLogo(g, logo, x, y, w, h, null);
    }

    /** Same, but with a fixed plate colour (null = choose automatically). */
    static void drawLogo(Graphics2D g, BufferedImage logo,
            int x, int y, int w, int h, Color plate) {
        g.setColor(plate != null ? plate : plateColour(logo));
        g.fillRoundRect(x, y, w, h, 12, 12);

        // Thin light outline, so a dark plate does not vanish into the card.
        g.setColor(new Color(255, 255, 255, 45));
        g.drawRoundRect(x, y, w - 1, h - 1, 12, 12);

        // Keep some padding inside the plate, then scale the logo to fit
        // completely (the smaller scale), keeping its proportions, and centre it.
        int pad = Math.max(3, Math.min(w, h) / 10);
        double scale = Math.min(
                (double) (w - 2 * pad) / logo.getWidth(),
                (double) (h - 2 * pad) / logo.getHeight());
        int dw = Math.max(1, (int) Math.round(logo.getWidth() * scale));
        int dh = Math.max(1, (int) Math.round(logo.getHeight() * scale));
        g.drawImage(logo, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }
}