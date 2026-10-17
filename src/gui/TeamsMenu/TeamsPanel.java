package gui.TeamsMenu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.Driver;
import model.SimulationData;
import model.Sponsor;
import model.Team;

/**
 * Teams screen.
 *
 * Left:  list of teams (logo, name, sponsor count).
 * Right: team banner with logo and the list of the team's sponsors.
 */
public class TeamsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DefaultListModel<Team> teamModel =
            new DefaultListModel<Team>();
    private final JList<Team> teamList = new JList<Team>(teamModel);

    private final TeamDetailCard detailCard = new TeamDetailCard();
    private final JLabel subtitle = new JLabel("TEAM DATABASE");
    private final LogoLoader logoLoader = new LogoLoader();

    // Team ID -> its sponsors / logo / number of drivers.
    private final Map<Integer, List<SponsorInfo>> sponsorsByTeam =
            new HashMap<Integer, List<SponsorInfo>>();
    private final Map<Integer, BufferedImage> teamLogos =
            new HashMap<Integer, BufferedImage>();
    private final Map<Integer, Integer> driverCounts =
            new HashMap<Integer, Integer>();

    public TeamsPanel(SimulationData data) {

        loadTeamLogos(data);
        groupSponsors(data);
        countDrivers(data);

        setLayout(new BorderLayout(0, 18));
        setBackground(TeamStyle.BACKGROUND);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadTeams(data);
    }

    // =====================================================
    // DATA
    // =====================================================

    /**
     * Loads every team's logo.
     *
     * >>> I guessed that Team has getLogoPath(). Rename it if needed. <<<
     */
    private void loadTeamLogos(SimulationData data) {
        for (Team team : data.getTeams()) {
            teamLogos.put(Integer.valueOf(team.getTeamId()),
                    logoLoader.load(team.getLogoPath(), team.getTeamName()));
        }
    }

    /**
     * Groups the sponsors (with their logos) by team.
     *
     * >>> This is the only place that uses the Sponsor class. <<<
     * I had to guess these calls. If one does not compile, rename it to
     * match your classes:
     *   data.getSponsors()    - list of all sponsors in SimulationData
     *   sponsor.getTeamId()   - ID of the team the sponsor belongs to
     *   sponsor.getName()     - sponsor name
     *   sponsor.getLogoPath() - path of the sponsor's logo
     */
    private void groupSponsors(SimulationData data) {
        for (Sponsor sponsor : data.getSponsors()) {
            Integer teamId = Integer.valueOf(sponsor.getTeamId());

            List<SponsorInfo> list = sponsorsByTeam.get(teamId);
            if (list == null) {
                list = new ArrayList<SponsorInfo>();
                sponsorsByTeam.put(teamId, list);
            }

            BufferedImage logo = logoLoader.load(
                    sponsor.getLogoPath(), sponsor.getName());
            list.add(new SponsorInfo(sponsor.getName(), logo));
        }
    }

    /** Counts the drivers of every team. */
    private void countDrivers(SimulationData data) {
        for (Driver driver : data.getDrivers()) {
            Integer teamId = Integer.valueOf(driver.getTeamId());
            Integer old = driverCounts.get(teamId);
            driverCounts.put(teamId,
                    Integer.valueOf(old == null ? 1 : old.intValue() + 1));
        }
    }

    private void loadTeams(SimulationData data) {
        for (Team team : data.getTeams()) {
            teamModel.addElement(team);
        }

        subtitle.setText("TEAM DATABASE  /  "
                + teamModel.getSize() + " TEAMS");

        // Selecting the first entry shows its details.
        if (!teamModel.isEmpty()) {
            teamList.setSelectedIndex(0);
        }
    }

    /** Shows the selected team and its sponsors. */
    private void displayTeam(Team team) {
        Integer id = Integer.valueOf(team.getTeamId());

        List<SponsorInfo> sponsors = sponsorsByTeam.get(id);
        if (sponsors == null) {
            sponsors = new ArrayList<SponsorInfo>();
        }

        Integer drivers = driverCounts.get(id);

        detailCard.showTeam(
                team.getTeamName(),
                TeamStyle.teamColour(team.getTeamId()),
                drivers == null ? 0 : drivers.intValue(),
                teamLogos.get(id),
                sponsors);
    }

    // =====================================================
    // LAYOUT
    // =====================================================

    /** Title, subtitle and a short red line. */
    private JPanel buildHeader() {
        JLabel heading = new JLabel("FORMULA 1  /  TEAMS");
        heading.setFont(TeamStyle.font(Font.BOLD, 26));
        heading.setForeground(TeamStyle.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitle.setFont(TeamStyle.font(Font.PLAIN, 11));
        subtitle.setForeground(TeamStyle.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel redLine = new JPanel();
        redLine.setBackground(TeamStyle.RED);
        redLine.setPreferredSize(new Dimension(48, 4));
        redLine.setMaximumSize(new Dimension(48, 4));
        redLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(heading);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(10));
        header.add(redLine);
        return header;
    }

    /** Team list on the left, details on the right. */
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(buildTeamList(), BorderLayout.WEST);
        body.add(detailCard, BorderLayout.CENTER);
        return body;
    }

    private JScrollPane buildTeamList() {
        teamList.setBackground(TeamStyle.CARD);
        teamList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        teamList.setFixedCellHeight(72);
        teamList.setCellRenderer(
                new TeamCellRenderer(sponsorsByTeam, teamLogos));

        // Show the details whenever another team is selected.
        teamList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    Team selected = teamList.getSelectedValue();
                    if (selected != null) {
                        displayTeam(selected);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(teamList);
        scroll.setBorder(BorderFactory.createLineBorder(TeamStyle.LINE));
        scroll.getViewport().setBackground(TeamStyle.CARD);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(310, 400));

        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new TeamScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(16);
        return scroll;
    }
}