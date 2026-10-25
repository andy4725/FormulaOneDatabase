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
 * Left:  list of teams (logo, name, driver names).
 * Right: team banner with logo, drivers and team principal, and the
 *        list of the team's sponsors.
 */
public class TeamsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // The model holds the teams; the JList only shows what is in the model.
    private final DefaultListModel<Team> teamModel =
            new DefaultListModel<Team>();
    private final JList<Team> teamList = new JList<Team>(teamModel);

    // Right side of the screen.
    private final TeamDetailCard detailCard = new TeamDetailCard();
    // A field because loadTeams() adds the number of teams to it later.
    private final JLabel subtitle = new JLabel("TEAM DATABASE");
    private final LogoLoader logoLoader = new LogoLoader();

    // Team ID -> its sponsors / logo / driver names.
    private final Map<Integer, List<SponsorInfo>> sponsorsByTeam =
            new HashMap<Integer, List<SponsorInfo>>();
    private final Map<Integer, BufferedImage> teamLogos =
            new HashMap<Integer, BufferedImage>();
    private final Map<Integer, List<String>> driverNames =
            new HashMap<Integer, List<String>>();

    /** Builds the screen and fills the list with the teams from data. */
    public TeamsPanel(SimulationData data) {

        // Prepare everything first (logos are read from disk only once),
        // so selecting a team later is instant.
        loadTeamLogos(data);
        groupSponsors(data);
        groupDrivers(data);

        // Header on top (NORTH), list + details in the rest (CENTER).
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
     * Loads every team's logo (from the logo_path column of the teams table).
     * A team without a readable logo gets null; the screen then shows its initials.
     */
    private void loadTeamLogos(SimulationData data) {
        for (Team team : data.getTeams()) {
            teamLogos.put(Integer.valueOf(team.getTeamId()),
                    logoLoader.load(team.getLogoPath(), team.getTeamName()));
        }
    }

    /**
     * Groups the sponsors (with their logos) by team.
     * This is the only place on the screen that uses the Sponsor model class;
     * the rest of the screen works with the simpler SponsorInfo.
     */
    private void groupSponsors(SimulationData data) {
        for (Sponsor sponsor : data.getSponsors()) {
            Integer teamId = Integer.valueOf(sponsor.getTeamId());

            // The first sponsor of a team creates that team's list.
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

    /** Collects the driver names of every team. */
    private void groupDrivers(SimulationData data) {
        for (Driver driver : data.getDrivers()) {
            Integer teamId = Integer.valueOf(driver.getTeamId());

            // The first driver of a team creates that team's list.
            List<String> list = driverNames.get(teamId);
            if (list == null) {
                list = new ArrayList<String>();
                driverNames.put(teamId, list);
            }
            list.add(driver.getName());
        }
    }

    /** Puts every team into the list model (the list updates itself). */
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

        // A team without sponsors gets an empty list (the card shows a message).
        List<SponsorInfo> sponsors = sponsorsByTeam.get(id);
        if (sponsors == null) {
            sponsors = new ArrayList<SponsorInfo>();
        }

        detailCard.showTeam(
                team.getTeamName(),
                TeamStyle.teamColour(team.getTeamId()),
                driverNames.get(id),
                TeamStyle.director(team.getTeamName()),
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

        // The red line is just an empty 48x4 panel with a red background.
        JPanel redLine = new JPanel();
        redLine.setBackground(TeamStyle.RED);
        redLine.setPreferredSize(new Dimension(48, 4));
        redLine.setMaximumSize(new Dimension(48, 4));
        redLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Everything under each other; struts are fixed gaps.
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

    /** The scrolling list of teams; only one can be selected at a time. */
    private JScrollPane buildTeamList() {
        teamList.setBackground(TeamStyle.CARD);
        teamList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Every row is 72 px, so the list does not have to measure each one.
        teamList.setFixedCellHeight(72);
        // The renderer paints the rows (logo, name, drivers).
        teamList.setCellRenderer(
                new TeamCellRenderer(driverNames, teamLogos));

        // Show the details whenever another team is selected.
        teamList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                // "Adjusting" is true while the mouse is still being dragged
                // over the list; wait until the selection is final.
                if (!event.getValueIsAdjusting()) {
                    Team selected = teamList.getSelectedValue();
                    if (selected != null) {
                        displayTeam(selected);
                    }
                }
            }
        });

        // The list goes into a scroll pane: 310 px wide, only vertical scrolling.
        JScrollPane scroll = new JScrollPane(teamList);
        scroll.setBorder(BorderFactory.createLineBorder(TeamStyle.LINE));
        scroll.getViewport().setBackground(TeamStyle.CARD);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(310, 400));

        // Thin dark scroll bar; unit increment = scroll speed of the mouse wheel.
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new TeamScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(16);
        return scroll;
    }
}