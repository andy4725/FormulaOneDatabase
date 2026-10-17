package gui.TeamsMenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Coloured banner at the top of the team details: team logo (or big
 * initials), team name and a short summary.
 */
class TeamBanner extends JPanel {

    private static final long serialVersionUID = 1L;

    private String name = "SELECT A TEAM";
    private String summary = "";
    private Color accent = TeamStyle.RED;
    private BufferedImage logo;

    TeamBanner() {
        setOpaque(true);
        setBackground(TeamStyle.CARD);
        setPreferredSize(new Dimension(100, 210));
    }

    void setTeam(String name, Color accent, int drivers, int sponsors,
            BufferedImage logo) {
        this.name = name == null ? "" : name;
        this.accent = accent;
        this.logo = logo;
        this.summary = TeamStyle.plural(drivers, "DRIVER")
                + "   /   " + TeamStyle.plural(sponsors, "SPONSOR");
        repaint();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            TeamStyle.enableQuality(g2);
            int w = getWidth();
            int h = getHeight();

            // Background gradient from the team colour into the card colour.
            g2.setPaint(new GradientPaint(0, 0,
                    TeamStyle.blend(TeamStyle.CARD, accent, 0.55),
                    w, h, TeamStyle.CARD));
            g2.fillRect(0, 0, w, h);

            // Team logo on the right, or big faint initials without a logo.
            int reserved = 0;
            if (logo != null) {
                TeamStyle.drawLogo(g2, logo, w - 28 - 210, 28, 210, 126);
                reserved = 250;
            } else {
                String initials = TeamStyle.initials(name);
                g2.setFont(TeamStyle.font(Font.BOLD | Font.ITALIC, 120));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 70));
                FontMetrics big = g2.getFontMetrics();
                g2.drawString(initials, w - 28 - big.stringWidth(initials),
                        h - 24);
            }

            // Stripe on the left and line at the bottom.
            g2.setColor(accent);
            g2.fillRect(0, 0, 5, h);
            g2.setColor(TeamStyle.LINE);
            g2.drawLine(0, h - 1, w, h - 1);

            // Team name, shrunk until it fits (and clears the logo).
            int size = 32;
            FontMetrics fm;
            do {
                g2.setFont(TeamStyle.font(Font.BOLD, size));
                fm = g2.getFontMetrics();
                size -= 2;
            } while (fm.stringWidth(name) > w - 60 - reserved && size > 16);
            g2.setColor(Color.WHITE);
            g2.drawString(name, 28, h - 56);

            // Summary line.
            g2.setFont(TeamStyle.font(Font.PLAIN, 12));
            g2.setColor(TeamStyle.MUTED);
            g2.drawString(summary, 28, h - 30);
        } finally {
            g2.dispose();
        }
    }
}