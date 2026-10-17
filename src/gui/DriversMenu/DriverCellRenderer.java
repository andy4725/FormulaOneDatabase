package gui.DriversMenu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import model.Driver;

/**
 * Paints one row of the driver list: team-coloured stripe, number badge,
 * driver name and team name.
 */
class DriverCellRenderer extends JPanel implements ListCellRenderer<Driver> {

    private static final long serialVersionUID = 1L;

    private final Map<Integer, String> teamNames;

    private String name = "";
    private String team = "";
    private String number = "";
    private Color accent = DriverStyle.RED;
    private boolean selected = false;

    DriverCellRenderer(Map<Integer, String> teamNames) {
        this.teamNames = teamNames;
        setOpaque(true);
    }

    /** Swing calls this for every row; we only store the values to paint. */
    public Component getListCellRendererComponent(
            JList<? extends Driver> list, Driver driver, int index,
            boolean isSelected, boolean cellHasFocus) {

        name = driver.getName();
        number = Integer.toString(driver.getCarNumber());

        String teamName = teamNames.get(Integer.valueOf(driver.getTeamId()));
        team = teamName == null ? "" : teamName;

        accent = DriverStyle.teamColour(driver.getTeamId());
        selected = isSelected;
        return this;
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 60);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            DriverStyle.enableQuality(g2);
            int w = getWidth();
            int h = getHeight();

            // Row background.
            g2.setColor(selected
                    ? DriverStyle.blend(DriverStyle.CARD_SELECTED, accent, 0.12)
                    : DriverStyle.CARD);
            g2.fillRect(0, 0, w, h);

            // Team stripe on the left.
            g2.setColor(selected
                    ? accent
                    : DriverStyle.blend(DriverStyle.CARD, accent, 0.55));
            g2.fillRect(0, 0, 4, h);

            // Thin line under the row.
            g2.setColor(DriverStyle.LINE);
            g2.drawLine(0, h - 1, w, h - 1);

            // Number badge.
            int badgeW = 44;
            int badgeH = 30;
            int badgeX = 18;
            int badgeY = (h - badgeH) / 2;
            g2.setColor(DriverStyle.blend(DriverStyle.CARD, accent,
                    selected ? 0.45 : 0.25));
            g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 10, 10);

            g2.setFont(DriverStyle.font(Font.BOLD, 14));
            g2.setColor(DriverStyle.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(number,
                    badgeX + (badgeW - fm.stringWidth(number)) / 2,
                    badgeY + (badgeH + fm.getAscent() - fm.getDescent()) / 2);

            // Name and team.
            int textX = badgeX + badgeW + 14;
            int maxWidth = w - textX - 10;

            g2.setFont(DriverStyle.font(Font.BOLD, 14));
            g2.setColor(selected ? Color.WHITE : DriverStyle.TEXT);
            g2.drawString(fit(g2, name, maxWidth), textX, h / 2 - 2);

            g2.setFont(DriverStyle.font(Font.PLAIN, 11));
            g2.setColor(DriverStyle.MUTED);
            g2.drawString(fit(g2, team, maxWidth), textX, h / 2 + 14);
        } finally {
            g2.dispose();
        }
    }

    /** Shortens text with "..." until it fits the width. */
    private String fit(Graphics2D g2, String text, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String result = text;
        while (result.length() > 3 && fm.stringWidth(result) > maxWidth) {
            result = result.substring(0, result.length() - 1);
            if (fm.stringWidth(result + "...") <= maxWidth) {
                return result.trim() + "...";
            }
        }
        return result;
    }
}