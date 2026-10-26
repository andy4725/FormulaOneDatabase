package gui.DriversMenu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicButtonUI;

import model.Driver;
import model.SimulationData;
import model.Team;


/**
 * Drivers screen.
 *
 * Left:  list of drivers (number badge, name, team).
 * Right: large portrait card and four statistic bars. The bars are sliders;
 *        "Apply ratings" uses the changes for the current app session.
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

    // Rating edits update the shared in-memory driver objects, not reference data in SQLite.
    private final JButton saveButton = new JButton("Apply ratings");
    private final JButton resetButton = new JButton("Reset");
    private final JLabel hint = new JLabel(" "); // left of the buttons; see updateButtons()
    private Driver current;   // the driver shown on the right, null = none yet

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
                    if (selected != null && selected != current) {
                        askToSaveChanges();
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

    /** The four statistic sliders in a 2 x 2 grid, with Save / Reset under them, on a card. */
    private JPanel buildStats() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 28, 16));
        grid.setOpaque(false);

        // Every slider reports its changes to barChanged().
        StatBar.Listener onChange = new StatBar.Listener() {
            public void valueChanged(StatBar bar, int value) {
                barChanged(bar, value);
            }
        };
        StatBar[] bars = { experienceBar, racecraftBar, paceBar, awarenessBar };
        for (StatBar bar : bars) {
            bar.setListener(onChange);
            grid.add(bar);
        }

        // Hint on the left, buttons on the right.
        hint.setFont(DriverStyle.font(Font.PLAIN, 11));
        hint.setForeground(DriverStyle.MUTED);

        styleButton(resetButton, DriverStyle.CARD, DriverStyle.MUTED);
        resetButton.setBorder(BorderFactory.createLineBorder(DriverStyle.LINE));
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (current != null) {
                    displayDriver(current); // back to the saved values
                }
            }
        });
        styleButton(saveButton, DriverStyle.RED, Color.WHITE);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyRatings();
            }
        });

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.add(resetButton);
        buttons.add(saveButton);

        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);
        actions.add(hint, BorderLayout.CENTER);
        actions.add(buttons, BorderLayout.EAST);

        JPanel stats = new JPanel(new BorderLayout(0, 16));
        stats.setBackground(DriverStyle.CARD);
        stats.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DriverStyle.LINE),
                new EmptyBorder(18, 22, 18, 22)));
        stats.add(grid, BorderLayout.CENTER);
        stats.add(actions, BorderLayout.SOUTH);

        updateButtons();
        return stats;
    }

    /**
     * Flat button. BasicButtonUI removes the operating system's look so
     * the colours are used as they are.
     */
    private static void styleButton(JButton button, Color background, Color foreground) {
        button.setUI(new BasicButtonUI());
        button.setFont(DriverStyle.font(Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 34));
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

        current = driver;
        experienceBar.setValue(driver.getExperience(), accent);
        racecraftBar.setValue(driver.getRacecraft(), accent);
        paceBar.setValue(driver.getPace(), accent);
        awarenessBar.setValue(driver.getAwareness(), accent);
        updateButtons();
    }

    // =====================================================
    // EDITING THE RATINGS
    // =====================================================

    /** A slider was dragged: mark it as changed if it differs from the saved value. */
    private void barChanged(StatBar bar, int value) {
        bar.setChanged(value != savedValue(bar));
        updateButtons();
    }

    /** The value of this bar's rating as it is saved for the current driver. */
    private int savedValue(StatBar bar) {
        if (bar == experienceBar) {
            return current.getExperience();
        }
        if (bar == racecraftBar) {
            return current.getRacecraft();
        }
        if (bar == paceBar) {
            return current.getPace();
        }
        return current.getAwareness();
    }

    /** True if any slider differs from the current driver's saved ratings. */
    private boolean hasChanges() {
        return current != null
                && (experienceBar.getValue() != current.getExperience()
                || racecraftBar.getValue() != current.getRacecraft()
                || paceBar.getValue() != current.getPace()
                || awarenessBar.getValue() != current.getAwareness());
    }

    /**
     * Save and Reset are only shown when there is something to save (a
     * disabled flat button is hard to read). GridLayout keeps their space,
     * so nothing jumps around when they appear.
     */
    private void updateButtons() {
        boolean changed = hasChanges();
        saveButton.setVisible(changed);
        resetButton.setVisible(changed);
        saveButton.setEnabled(true);
        saveButton.setText("Apply ratings");
        hint.setText(changed ? "Changes apply for this session"
                : "Drag a bar to change a rating");
    }

    /** Before another driver is shown: offer to apply the pending changes. */
    private void askToSaveChanges() {
        if (!hasChanges()) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Apply the changed ratings of " + current.getName()
                        + " for this session?",
                "Unapplied changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            applyRatings();
        }
    }

    /** Applies the four slider values to the shared in-memory driver for this session. */
    private void applyRatings() {
        if (!hasChanges()) {
            return;
        }
        final Driver driver = current;
        driver.setRatings(experienceBar.getValue(), racecraftBar.getValue(),
                paceBar.getValue(), awarenessBar.getValue());
        experienceBar.setChanged(false);
        racecraftBar.setChanged(false);
        paceBar.setChanged(false);
        awarenessBar.setChanged(false);
        updateButtons();
    }
}
