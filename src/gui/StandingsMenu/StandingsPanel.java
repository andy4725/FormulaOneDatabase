package gui.StandingsMenu;

import gui.DriversMenu.DarkScrollBarUI;
import gui.MainMenu.Theme;
import gui.RaceMenu.Avatar;
import gui.RaceMenu.PortraitCache;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import model.Driver;
import model.DriverStanding;
import model.SimulationData;
import model.TeamStanding;
import repository.StandingsRepository;
import service.StandingsService;

/**
 * Standings screen.
 *
 * Left card:  the drivers' championship (portrait, name, team, points).
 * Right card: the teams' championship (name, points).
 *
 * The top 3 of both lists get a gold, silver or bronze row.
 * The points are read from the database (the saved race results of
 * the slot's season), so MainWindow calls showSlot() every time
 * the screen is opened. That way new races always show up.
 */
public class StandingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Medal colours for P1, P2 and P3.
    private static final Color GOLD   = new Color(212, 175, 55);
    private static final Color SILVER = new Color(192, 197, 206);
    private static final Color BRONZE = new Color(205, 127, 50);

    // The standings logic already exists in the service layer;
    // this screen only asks for the lists and shows them.
    private final StandingsService service = new StandingsService();
    private final StandingsRepository repository = new StandingsRepository();

    // The two lists. Rows are added to them in showDrivers() / showTeams().
    private final JPanel driverRows = new JPanel();
    private final JPanel teamRows = new JPanel();

    // DriverStanding only has the driver's id, but the portrait loader
    // needs the Driver object, so we keep the drivers by id.
    private final Map<Integer, Driver> driversById = new HashMap<Integer, Driver>();
    private final PortraitCache portraits = new PortraitCache();

    public StandingsPanel(SimulationData data) {
        for (Driver driver : data.getDrivers()) {
            driversById.put(Integer.valueOf(driver.getDriverId()), driver);
        }

        // Dark background with some space around the edges.
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.HOME_BG);
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // Title at the top.
        add(Theme.label("Standings", Font.BOLD, 26, Theme.TEXT), BorderLayout.NORTH);

        // The two cards side by side, same width.
        JPanel cards = new JPanel(new GridLayout(1, 2, 18, 0));
        cards.setOpaque(false);
        cards.add(card("Drivers", driverRows));
        cards.add(card("Teams", teamRows));
        add(cards, BorderLayout.CENTER);

        showMessage("Choose a save slot first.");
    }

    // =====================================================
    // Loading
    // =====================================================

    /** What the background loading returns: both lists at once. */
    private static class Loaded {
        List<DriverStanding> drivers;
        List<TeamStanding> teams;
    }

    /** Reloads the standings of the slot's season. */
    public void showSlot(final int slot) {
        if (slot <= 0) {
            showMessage("Choose a save slot first.");
            return;
        }
        showMessage("Loading...");

        // Reading the database can take a moment, so it runs in the
        // background (doInBackground) and the window is updated
        // afterwards on the Swing thread (done).
        new SwingWorker<Loaded, Void>() {
            @Override
            protected Loaded doInBackground() throws Exception {
                int seasonId = repository.findSeasonId(slot);
                if (seasonId <= 0) {
                    return null; // this slot has no season yet
                }
                Loaded loaded = new Loaded();
                loaded.drivers = service.getDriverStandings(seasonId);
                loaded.teams = service.getTeamStandings(seasonId);
                return loaded;
            }

            @Override
            protected void done() {
                try {
                    // get() gives back what doInBackground() returned,
                    // or throws the error that happened there.
                    Loaded loaded = get();
                    if (loaded == null) {
                        showMessage("This slot has no season yet.");
                    } else {
                        showDrivers(loaded.drivers);
                        showTeams(loaded.teams);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("The standings could not be loaded.");
                }
            }
        }.execute();
    }

    // =====================================================
    // Filling the two lists
    // =====================================================

    private void showDrivers(List<DriverStanding> standings) {
        driverRows.removeAll();
        for (DriverStanding s : standings) {
            // Portrait of the driver (initials are shown if there is no picture).
            Driver driver = driversById.get(Integer.valueOf(s.getDriverId()));
            Avatar avatar = new Avatar(driver == null ? null : portraits.get(driver),
                    s.getDriverName(), 44);

            driverRows.add(row(s.getPosition(), avatar, s.getDriverName(),
                    s.getTeamName(), s.getPoints()));
        }
        refresh(driverRows);
    }

    private void showTeams(List<TeamStanding> standings) {
        teamRows.removeAll();
        for (TeamStanding s : standings) {
            // Teams have no portrait and no second line.
            teamRows.add(row(s.getPosition(), null, s.getTeamName(), null, s.getPoints()));
        }
        refresh(teamRows);
    }

    /** Shows the same message in both cards (e.g. "Loading..."). */
    private void showMessage(String message) {
        driverRows.removeAll();
        teamRows.removeAll();
        driverRows.add(messageLabel(message));
        teamRows.add(messageLabel(message));
        refresh(driverRows);
        refresh(teamRows);
    }

    private static JLabel messageLabel(String message) {
        JLabel label = Theme.label(message, Font.PLAIN, 15, Theme.MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(40, 18, 0, 0));
        return label;
    }

    /** Swing has to be told to lay out and repaint after rows change. */
    private static void refresh(JPanel rows) {
        rows.revalidate();
        rows.repaint();
    }

    // =====================================================
    // Building blocks
    // =====================================================

    /**
     * A card with a title on top and a scrolling list below.
     * The list puts its rows under each other (BoxLayout, Y axis).
     */
    private static JPanel card(String title, JPanel rows) {
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        // The rows go into a holder at the top (NORTH), so a short list
        // stays at the top instead of being stretched over the whole card.
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(rows, BorderLayout.NORTH);

        // Scrolls up and down only, with the dark scroll bar of the Drivers screen.
        JScrollPane scroll = new JScrollPane(holder,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new DarkScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(16); // scroll speed of the mouse wheel

        JLabel heading = Theme.label(title, Font.BOLD, 18, Theme.TEXT);
        heading.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.TILE_BG);
        card.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        card.add(heading, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /**
     * One line of a list:
     *   [position] [portrait] [name / team underneath] ......... [points]
     *
     * avatar and team may be null (the team list has neither).
     * P1, P2 and P3 get a tinted background and a coloured stripe on the left.
     */
    private static JPanel row(int position, Avatar avatar, String name,
            String team, double points) {
        Color medal = medalColor(position); // null below P3
        boolean podium = medal != null;

        // BorderLayout: left part (WEST), name in the middle (CENTER), points (EAST).
        JPanel row = new JPanel(new BorderLayout(14, 0));

        if (podium) {
            // Medal colour mixed into the card colour (12%) as background,
            // plus a 4 px stripe on the left in the full medal colour.
            row.setOpaque(true);
            row.setBackground(Theme.blend(Theme.TILE_BG, medal, 0.12));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 4, 0, 0, medal),
                    BorderFactory.createEmptyBorder(12, 14, 12, 18)));
        } else {
            // Normal row: transparent, thin line on top to separate rows.
            row.setOpaque(false);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        }

        // Text colour: medal colour on the podium, white for the others.
        Color highlight = podium ? medal : Theme.TEXT;

        // ----- left: position number and portrait -----
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        JLabel pos = Theme.label(String.valueOf(position), Font.BOLD,
                podium ? 22 : 18, highlight);
        // Fixed width so all names start at the same place. Rows with a
        // portrait are as tall as the portrait (44 px), team rows are lower.
        pos.setPreferredSize(new Dimension(32, avatar != null ? 44 : 26));
        pos.setMinimumSize(pos.getPreferredSize());
        pos.setMaximumSize(pos.getPreferredSize());
        left.add(pos);
        if (avatar != null) {
            left.add(avatar);
        }
        row.add(left, BorderLayout.WEST);

        // ----- middle: name, with the team in grey underneath -----
        JPanel middle = new JPanel(new GridLayout(team == null ? 1 : 2, 1));
        middle.setOpaque(false);
        middle.add(Theme.label(name, Font.BOLD, podium ? 17 : 15, Theme.TEXT));
        if (team != null) {
            middle.add(Theme.label(team, Font.PLAIN, 13, Theme.MUTED));
        }
        row.add(middle, BorderLayout.CENTER);

        // ----- right: points -----
        row.add(Theme.label(formatPoints(points) + " pts", Font.BOLD,
                podium ? 17 : 15, highlight), BorderLayout.EAST);

        // Stop BoxLayout from making rows taller than they need to be.
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Gold, silver and bronze for P1-P3, null for everyone else. */
    private static Color medalColor(int position) {
        if (position == 1) {
            return GOLD;
        }
        if (position == 2) {
            return SILVER;
        }
        if (position == 3) {
            return BRONZE;
        }
        return null;
    }

    /** 25.0 becomes "25"; 12.5 stays "12.5". */
    private static String formatPoints(double points) {
        if (points == Math.floor(points)) {
            return String.valueOf((long) points);
        }
        return String.valueOf(points);
    }
}
