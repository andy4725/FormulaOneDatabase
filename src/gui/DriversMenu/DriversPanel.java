package gui.DriversMenu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
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
import model.Team;


/**
 * Drivers screen.
 *
 * Left:  list of drivers (number badge, name, team).
 * Right: large portrait card and four statistic bars.
 *
 * The drawing is done by small helper classes in this package:
 * DriverCellRenderer, PortraitCard, StatBar, PortraitLoader.
 */
public class DriversPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // The model holds the drivers; the JList only shows what is in the model.
    private final DefaultListModel<Driver> driverModel =
            new DefaultListModel<Driver>();
    private final JList<Driver> driverList =
            new JList<Driver>(driverModel);

    // Right side: the big card and one bar per statistic.
    private final Portraitcard portraitCard = new Portraitcard();
    private final StatBar experienceBar = new StatBar("EXPERIENCE");
    private final StatBar racecraftBar  = new StatBar("RACECRAFT");
    private final StatBar paceBar       = new StatBar("PACE");
    private final StatBar awarenessBar  = new StatBar("AWARENESS");

    // A field (not a local variable) because loadDrivers() adds the count to it later.
    private final JLabel subtitle = new JLabel("DRIVER DATABASE");

    // team_id -> team name; filled in the constructor.
    private final Map<Integer, String> teamNames =
            new HashMap<Integer, String>();
    private final Portraitloader portraitLoader = new Portraitloader();

    /** Builds the screen and fills the list with the drivers from data. */
    public DriversPanel(SimulationData data) {

        // Team ID -> team name, used by the list and the profile card.
        for (Team team : data.getTeams()) {
            teamNames.put(Integer.valueOf(team.getTeamId()),
                    team.getTeamName());
        }

        // Header on top (NORTH), list + details in the rest (CENTER),
        // 18 px between them and empty space around the edges.
        setLayout(new BorderLayout(0, 18));
        setBackground(DriverStyle.BACKGROUND);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadDrivers(data);
    }

    // =====================================================
    // LAYOUT
    // =====================================================

    /** Title, subtitle and a short red line. */
    private JPanel buildHeader() {
        JLabel heading = new JLabel("FORMULA 1  /  DRIVERS");
        heading.setFont(DriverStyle.font(Font.BOLD, 26));
        heading.setForeground(DriverStyle.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitle.setFont(DriverStyle.font(Font.PLAIN, 11));
        subtitle.setForeground(DriverStyle.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // The red line is just an empty 48x4 panel with a red background.
        JPanel redLine = new JPanel();
        redLine.setBackground(DriverStyle.RED);
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

    /** Driver list on the left, details on the right. */
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(buildDriverList(), BorderLayout.WEST);
        body.add(buildDetails(), BorderLayout.CENTER);
        return body;
    }

    /** The scrolling list of drivers; only one can be selected at a time. */
    private JScrollPane buildDriverList() {
        driverList.setBackground(DriverStyle.CARD);
        driverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Every row is 60 px, so the list does not have to measure each one.
        driverList.setFixedCellHeight(60);
        // The renderer paints the rows (badge, name, team).
        driverList.setCellRenderer(new DriverCellRenderer(teamNames));

        // Show the profile whenever another driver is selected.
        driverList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                // "Adjusting" is true while the mouse is still being dragged
                // over the list; wait until the selection is final.
                if (!event.getValueIsAdjusting()) {
                    Driver selected = driverList.getSelectedValue();
                    if (selected != null) {
                        displayDriver(selected);
                    }
                }
            }
        });

        // The list goes into a scroll pane: 280 px wide, only vertical scrolling.
        JScrollPane scroll = new JScrollPane(driverList);
        scroll.setBorder(BorderFactory.createLineBorder(DriverStyle.LINE));
        scroll.getViewport().setBackground(DriverStyle.CARD);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(280, 400));

        // Thin dark scroll bar; unit increment = scroll speed of the mouse wheel.
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new DarkScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(16);
        return scroll;
    }

    /** Portrait card with the statistics underneath. */
    private JPanel buildDetails() {
        JPanel details = new JPanel(new BorderLayout(0, 14));
        details.setOpaque(false);
        details.add(portraitCard, BorderLayout.CENTER);
        details.add(buildStats(), BorderLayout.SOUTH);
        return details;
    }

    /** The four statistic bars in a 2 x 2 grid on a card. */
    private JPanel buildStats() {
        JPanel stats = new JPanel(new GridLayout(2, 2, 28, 16));
        stats.setBackground(DriverStyle.CARD);
        stats.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DriverStyle.LINE),
                new EmptyBorder(18, 22, 18, 22)));

        stats.add(experienceBar);
        stats.add(racecraftBar);
        stats.add(paceBar);
        stats.add(awarenessBar);
        return stats;
    }

    // =====================================================
    // DATA
    // =====================================================

    /** Puts every driver into the list model (the list updates itself). */
    private void loadDrivers(SimulationData data) {
        for (Driver driver : data.getDrivers()) {
            driverModel.addElement(driver);
        }

        subtitle.setText("DRIVER DATABASE  /  "
                + driverModel.getSize() + " DRIVERS");

        // Selecting the first entry shows its profile.
        if (!driverModel.isEmpty()) {
            driverList.setSelectedIndex(0);
        }
    }

    /** Updates the portrait card and the four bars. */
    private void displayDriver(Driver driver) {
        // Unknown team: show a dash (\u2014) instead of an empty space.
        String team = teamNames.get(Integer.valueOf(driver.getTeamId()));
        if (team == null || team.trim().length() == 0) {
            team = "\u2014";
        }
        // The team colour is used for the card stripe, number and bars.
        Color accent = DriverStyle.teamColour(driver.getTeamId());

        // null if no portrait file was found; the card then shows a message.
        BufferedImage photo = portraitLoader.load(
                driver.getName(), driver.getPortraitPath());

        portraitCard.setDriver(photo, driver.getName(), team,
                driver.getCarNumber(), accent);

        experienceBar.setValue(driver.getExperience(), accent);
        racecraftBar.setValue(driver.getRacecraft(), accent);
        paceBar.setValue(driver.getPace(), accent);
        awarenessBar.setValue(driver.getAwareness(), accent);
    }
}