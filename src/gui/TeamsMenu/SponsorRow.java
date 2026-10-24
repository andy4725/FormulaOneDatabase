package gui.TeamsMenu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * One sponsor in the team details: logo (or a coloured dot), name and a
 * row number.
 */
class SponsorRow extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final BufferedImage logo; // null = no logo, a dot is drawn instead
    private final Color accent;       // team colour, used for the dot
    private final Color plate; // fixed plate colour, or null
    private final String number;      // "01", "02", ...

    SponsorRow(String name, BufferedImage logo, Color accent, int number) {
        this.name = name == null ? "" : name;
        this.logo = logo;
        this.accent = accent;
        // Some logos need a specific background colour to be readable.
        this.plate = TeamStyle.plateOverride(name);
        // %02d = at least two digits, with a leading zero (1 -> "01").
        this.number = String.format("%02d", Integer.valueOf(number));

        // 72 px high; in the BoxLayout list it may grow as wide as the list.
        setOpaque(false);
        setPreferredSize(new Dimension(200, 72));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /** Draws the row: rounded background, logo or dot, number on the right, name. */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            TeamStyle.enableQuality(g2);
            // 1 px less, so the rounded shape fits inside the row.
            int w = getWidth() - 1;
            int h = getHeight() - 1;

            // Rounded row.
            g2.setColor(TeamStyle.CARD_SELECTED);
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            // Logo on a plate, or a coloured dot if there is no logo.
            // textX is where the name starts, right of the logo or dot.
            int textX;
            if (logo != null) {
                TeamStyle.drawLogo(g2, logo, 12, (h - 56) / 2, 120, 56, plate);
                textX = 148;
            } else {
                g2.setColor(accent);
                g2.fillOval(18, h / 2 - 5, 10, 10);
                textX = 42;
            }

            // Row number on the right.
            g2.setFont(TeamStyle.font(Font.BOLD, 12));
            g2.setColor(TeamStyle.MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int numberWidth = fm.stringWidth(number);
            g2.drawString(number, w - 18 - numberWidth,
                    (h + fm.getAscent() - fm.getDescent()) / 2);

            // Sponsor name, vertically centred, shortened with "..." if it
            // would run into the number.
            g2.setFont(TeamStyle.font(Font.BOLD, 14));
            g2.setColor(TeamStyle.TEXT);
            fm = g2.getFontMetrics();
            g2.drawString(
                    TeamStyle.fit(g2, name, w - textX - numberWidth - 36),
                    textX,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
        } finally {
            g2.dispose();
        }
    }
}