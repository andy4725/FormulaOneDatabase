package gui.TeamsMenu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import model.Team;

/**
 * Paints one row of the team list: coloured stripe, logo (or initials
 * badge), team name and driver names.
 *
 * A JList does not make a component for every row. It uses this one
 * object as a "stamp": for each row it calls
 * getListCellRendererComponent() to fill in that row's values, then
 * paints this panel at the row's position.
 */
class TeamCellRenderer extends JPanel implements ListCellRenderer<Team> {

    private static final long serialVersionUID = 1L;

    // Prepared by TeamsPanel, keyed by team_id.
    private final Map<Integer, List<String>> driverNames;
    private final Map<Integer, BufferedImage> teamLogos;

    // The values of the row that is being painted right now.
    private String name = "";
    private String initials = "";        // shown when there is no logo
    private String info = "";            // the driver names, in one line
    private BufferedImage logo;
    private Color accent = TeamStyle.RED; // the team colour
    private boolean selected = false;

    TeamCellRenderer(Map<Integer, List<String>> driverNames,
            Map<Integer, BufferedImage> teamLogos) {
        this.driverNames = driverNames;
        this.teamLogos = teamLogos;
        setOpaque(true);
    }

    /** Swing calls this for every row; we only store the values to paint. */
    public Component getListCellRendererComponent(
            JList<? extends Team> list, Team team, int index,
            boolean isSelected, boolean cellHasFocus) {

        Integer id = Integer.valueOf(team.getTeamId());

        name = team.getTeamName() == null ? "" : team.getTeamName();
        initials = TeamStyle.initials(name);
        logo = teamLogos.get(id);

        info = TeamStyle.joinNames(driverNames.get(id));

        accent = TeamStyle.teamColour(team.getTeamId());
        selected = isSelected;
        return this;
    }

    /** Every row is 72 px high (the list stretches the width). */
    public Dimension getPreferredSize() {
        return new Dimension(200, 72);
    }

    /** Paints the row with the values stored by getListCellRendererComponent(). */
    protected void paintComponent(Graphics g) {
        // Draw on a copy of the Graphics; it is released in finally.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            TeamStyle.enableQuality(g2);
            int w = getWidth();
            int h = getHeight();

            // Row background.
            g2.setColor(selected
                    ? TeamStyle.blend(TeamStyle.CARD_SELECTED, accent, 0.12)
                    : TeamStyle.CARD);
            g2.fillRect(0, 0, w, h);

            // Team stripe on the left.
            g2.setColor(selected
                    ? accent
                    : TeamStyle.blend(TeamStyle.CARD, accent, 0.55));
            g2.fillRect(0, 0, 4, h);

            // Thin line under the row.
            g2.setColor(TeamStyle.LINE);
            g2.drawLine(0, h - 1, w, h - 1);

            // Logo, or an initials badge if there is no logo.
            int badgeW = 80;
            int badgeH = 48;
            int badgeX = 18;
            int badgeY = (h - badgeH) / 2;
            if (logo != null) {
                TeamStyle.drawLogo(g2, logo, badgeX, badgeY, badgeW, badgeH, accent);
            } else {
                g2.setColor(TeamStyle.blend(TeamStyle.CARD, accent,
                        selected ? 0.45 : 0.25));
                g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 10, 10);

                g2.setFont(TeamStyle.font(Font.BOLD, 14));
                g2.setColor(TeamStyle.TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials,
                        badgeX + (badgeW - fm.stringWidth(initials)) / 2,
                        badgeY + (badgeH + fm.getAscent() - fm.getDescent()) / 2);
            }

            // Name and driver names, right of the logo. Long texts are
            // shortened with "..." so they never run past the right edge.
            int textX = badgeX + badgeW + 14;
            int maxWidth = w - textX - 10;

            g2.setFont(TeamStyle.font(Font.BOLD, 14));
            g2.setColor(selected ? Color.WHITE : TeamStyle.TEXT);
            g2.drawString(TeamStyle.fit(g2, name, maxWidth),
                    textX, h / 2 - 2);

            g2.setFont(TeamStyle.font(Font.PLAIN, 11));
            g2.setColor(TeamStyle.MUTED);
            g2.drawString(TeamStyle.fit(g2, info, maxWidth),
                    textX, h / 2 + 14);
        } finally {
            g2.dispose();
        }
    }
}