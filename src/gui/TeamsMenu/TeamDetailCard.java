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
    // The sponsor rows go in here; it is emptied and refilled for every team.
    private final JPanel sponsorList = new JPanel();

    TeamDetailCard() {
        // Banner on top (NORTH), sponsors in the rest (CENTER).
        super(new BorderLayout());
        setBackground(TeamStyle.CARD);
        setBorder(BorderFactory.createLineBorder(TeamStyle.LINE));

        // Sponsor rows under each other.
        sponsorList.setLayout(new BoxLayout(sponsorList, BoxLayout.Y_AXIS));
        sponsorList.setBackground(TeamStyle.CARD);
        sponsorList.setBorder(new EmptyBorder(4, 22, 18, 22));

        // Scrolls when a team has more sponsors than fit; vertical only.
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

        // The lower part: "SPONSORS" heading above the scrolling list.
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

        // Throw away the rows of the previous team, then add the new ones
        // (numbered from 1, with 8 px between rows).
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
        // Glue fills the leftover space, so the rows stay at the top.
        sponsorList.add(Box.createVerticalGlue());

        // Swing has to be told to lay out and repaint after rows change.
        sponsorList.revalidate();
        sponsorList.repaint();
    }
}