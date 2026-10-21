package gui.RaceMenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * One line of the ranking: position, round portrait, driver, team, time.
 */
class ResultRow extends JPanel {

    private static final long serialVersionUID = 1L;

    ResultRow(int position, String driverName, String teamName,
            BufferedImage portrait, String time) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RaceStyle.LINE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 14);

        JLabel pos = RaceStyle.label(String.valueOf(position), Font.BOLD, 22,
                position == 1 ? RaceStyle.RED : RaceStyle.TEXT);
        pos.setPreferredSize(new Dimension(36, 30));
        c.gridx = 0;
        add(pos, c);

        c.gridx = 1;
        add(new Avatar(portrait, driverName), c);

        // Same tiny preferred width for name and team, so both columns get
        // an equal share of the space and line up from row to row.
        c.gridx = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel name = RaceStyle.label(driverName, Font.BOLD, 17, RaceStyle.TEXT);
        name.setPreferredSize(new Dimension(10, 30));
        add(name, c);

        c.gridx = 3;
        JLabel team = RaceStyle.label(teamName, Font.PLAIN, 15, RaceStyle.MUTED);
        team.setPreferredSize(new Dimension(10, 30));
        add(team, c);

        c.gridx = 4;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 0, 0, 0);
        JLabel timeLabel = RaceStyle.label(time, Font.BOLD, 17, RaceStyle.TEXT);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setPreferredSize(new Dimension(110, 30));
        add(timeLabel, c);
    }

    /** Portrait cropped to a circle; initials if there is no picture. */
    private static class Avatar extends JComponent {

        private static final long serialVersionUID = 1L;
        private static final int SIZE = 44;

        private final BufferedImage image;
        private final String initials;

        Avatar(BufferedImage image, String name) {
            this.image = image;
            this.initials = initials(name);
            setPreferredSize(new Dimension(SIZE, SIZE));
            setMinimumSize(new Dimension(SIZE, SIZE));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            // Draw on a copy of the Graphics, so the clip set below does not
            // leak into other components. finally makes sure the copy is
            // released even if drawing throws an exception.
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                RaceStyle.enableQuality(g);
                Ellipse2D circle = new Ellipse2D.Double(0, 0, SIZE, SIZE);

                g.setColor(new Color(58, 61, 70));
                g.fill(circle);

                if (image != null) {
                    // Anything drawn from here on is cut to the circle.
                    g.setClip(circle);
                    // Fill the circle, keeping the face (top part of the photo).
                    double scale = Math.max((double) SIZE / image.getWidth(),
                            (double) SIZE / image.getHeight());
                    int w = (int) Math.round(image.getWidth() * scale);
                    int h = (int) Math.round(image.getHeight() * scale);
                    g.drawImage(image, (SIZE - w) / 2, 0, w, h, null);
                } else {
                    g.setColor(RaceStyle.MUTED);
                    g.setFont(RaceStyle.font(Font.BOLD, 15));
                    int textW = g.getFontMetrics().stringWidth(initials);
                    g.drawString(initials, (SIZE - textW) / 2, SIZE / 2 + 5);
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
}