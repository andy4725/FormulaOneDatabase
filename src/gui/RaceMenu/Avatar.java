package gui.RaceMenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/** Portrait cropped to a circle; initials if there is no picture. */
public class Avatar extends JComponent {

    private static final long serialVersionUID = 1L;

    private final int size;
    private final BufferedImage image;
    private final String initials;

    public Avatar(BufferedImage image, String name, int size) {
        this.image = image;
        this.initials = initials(name);
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        // Draw on a copy of the Graphics, so the clip set below does not
        // leak into other components. finally makes sure the copy is
        // released even if drawing throws an exception.
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            RaceStyle.enableQuality(g);
            Ellipse2D circle = new Ellipse2D.Double(0, 0, size, size);

            g.setColor(new Color(58, 61, 70));
            g.fill(circle);

            if (image != null) {
                // Anything drawn from here on is cut to the circle.
                g.setClip(circle);
                // Fill the circle, keeping the face (top part of the photo).
                double scale = Math.max((double) size / image.getWidth(),
                        (double) size / image.getHeight());
                int w = (int) Math.round(image.getWidth() * scale);
                int h = (int) Math.round(image.getHeight() * scale);
                g.drawImage(image, (size - w) / 2, 0, w, h, null);
            } else {
                g.setColor(RaceStyle.MUTED);
                g.setFont(RaceStyle.font(Font.BOLD, 15));
                int textW = g.getFontMetrics().stringWidth(initials);
                g.drawString(initials, (size - textW) / 2, size / 2 + 5);
            }
        } finally {
            g.dispose();
        }
    }

    private static String initials(String name) {
        StringBuilder out = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (part.length() > 0 && out.length() < 2) {
                out.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return out.toString();
    }
}
