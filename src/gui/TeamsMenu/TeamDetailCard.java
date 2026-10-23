package gui.TeamsMenu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

/**
 * Right side of the Teams screen: team banner on top and the scrollable
 * list of sponsors below it.
 */
class TeamDetailCard extends JPanel {

    private static final long serialVersionUID = 1L;

    private final TeamBanner banner = new TeamBanner();
    private final JPanel sponsorList = new JPanel();

    TeamDetailCard() {
        super(new BorderLayout());
        setBackground(TeamStyle.CARD);
        setBorder(BorderFactory.createLineBorder(TeamStyle.LINE));

        sponsorList.setLayout(new BoxLayout(sponsorList, BoxLayout.Y_AXIS));
        sponsorList.setBackground(TeamStyle.CARD);
        sponsorList.setBorder(new EmptyBorder(4, 22, 18, 22));

        JScrollPane scroll = new JScrollPane(sponsorList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TeamStyle.CARD);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new TeamScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(16);

        JLabel title = new JLabel("SPONSORS");
        title.setFont(TeamStyle.font(Font.BOLD, 11));
        title.setForeground(TeamStyle.MUTED);
        title.setBorder(new EmptyBorder(18, 22, 8, 22));

        JPanel lower = new JPanel(new BorderLayout());
        lower.setOpaque(false);
        lower.add(title, BorderLayout.NORTH);
        lower.add(scroll, BorderLayout.CENTER);

        add(banner, BorderLayout.NORTH);
        add(lower, BorderLayout.CENTER);
    }

    /** Shows one team, its logo, drivers, director and sponsors. */
    void showTeam(String teamName, Color accent, List<String> drivers,
            String director, BufferedImage teamLogo,
            List<SponsorInfo> sponsors) {

        banner.setTeam(teamName, accent, drivers, director, teamLogo);

        sponsorList.removeAll();
        if (sponsors.isEmpty()) {
            JLabel none = new JLabel("No sponsors found for this team.");
            none.setFont(TeamStyle.font(Font.PLAIN, 13));
            none.setForeground(TeamStyle.MUTED);
            sponsorList.add(none);
        } else {
            for (int i = 0; i < sponsors.size(); i++) {
                SponsorInfo sponsor = sponsors.get(i);
                sponsorList.add(new SponsorRow(
                        sponsor.name, sponsor.logo, accent, i + 1));
                sponsorList.add(Box.createVerticalStrut(8));
            }
        }
        sponsorList.add(Box.createVerticalGlue());

        sponsorList.revalidate();
        sponsorList.repaint();
    }
}