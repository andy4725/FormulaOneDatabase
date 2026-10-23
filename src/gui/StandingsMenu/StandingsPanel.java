package gui.StandingsMenu;

import gui.DriversMenu.DarkScrollBarUI;
import gui.MainMenu.Theme;
import gui.RaceMenu.Avatar;
import gui.RaceMenu.PortraitCache;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import model.Driver;
import model.DriverStanding;
import model.SimulationData;
import model.TeamStanding;
import repository.StandingsRepository;
import service.StandingsService;

/**
 * Standings screen: drivers' championship on the left,
 * constructors' championship on the right. The top 3 get
 * gold, silver and bronze rows; drivers are shown with their portrait.
 * The points come from the saved race results of the current slot's season,
 * so call showSlot() every time the screen is opened.
 */
public class StandingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color GOLD   = new Color(212, 175, 55);
    private static final Color SILVER = new Color(192, 197, 206);
    private static final Color BRONZE = new Color(205, 127, 50);

    private final StandingsService service = new StandingsService();
    private final StandingsRepository repository = new StandingsRepository();

    private final JPanel driverRows = rowsPanel();
    private final JPanel teamRows = rowsPanel();

    private final PortraitCache portraits = new PortraitCache();
    private final Map<Integer, Driver> driversById = new HashMap<Integer, Driver>();

    public StandingsPanel(SimulationData data) {
        for (Driver driver : data.getDrivers()) {
            driversById.put(Integer.valueOf(driver.getDriverId()), driver);
        }

        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.HOME_BG);
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        add(Theme.label("Standings", Font.BOLD, 26, Theme.TEXT), BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(1, 2, 18, 0));
        tables.setOpaque(false);
        tables.add(card("Drivers", driverRows));
        tables.add(card("Teams", teamRows));
        add(tables, BorderLayout.CENTER);

        showMessage("Choose a save slot first.");
    }

    /** Reloads the standings of the slot's season in the background. */
    public void showSlot(final int slot) {
        if (slot <= 0) {
            showMessage("Choose a save slot first.");
            return;
        }
        showMessage("Loading...");

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                int seasonId = repository.findSeasonId(slot);
                if (seasonId <= 0) {
                    return null;
                }
                return new Object[] {
                        service.getDriverStandings(seasonId),
                        service.getTeamStandings(seasonId) };
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    if (result == null) {
                        showMessage("This slot has no season yet.");
                    } else {
                        showDrivers((List<DriverStanding>) result[0]);
                        showTeams((List<TeamStanding>) result[1]);
                    }
                } catch (Exception e) {
                    Throwable cause = e instanceof ExecutionException && e.getCause() != null
                            ? e.getCause() : e;
                    cause.printStackTrace();
                    showMessage("The standings could not be loaded.");
                }
            }
        }.execute();
    }

    private void showDrivers(List<DriverStanding> standings) {
        driverRows.removeAll();
        for (DriverStanding s : standings) {
            Driver driver = driversById.get(Integer.valueOf(s.getDriverId()));
            int size = s.getPosition() <= 3 ? 52 : 40;
            Avatar avatar = new Avatar(driver == null ? null : portraits.get(driver),
                    s.getDriverName(), size);
            driverRows.add(row(s.getPosition(), avatar, s.getDriverName(),
                    s.getTeamName(), s.getPoints()));
        }
        finish(driverRows);
    }

    private void showTeams(List<TeamStanding> standings) {
        teamRows.removeAll();
        for (TeamStanding s : standings) {
            teamRows.add(row(s.getPosition(), null, s.getTeamName(), "", s.getPoints()));
        }
        finish(teamRows);
    }

    private void showMessage(String message) {
        for (JPanel rows : new JPanel[] { driverRows, teamRows }) {
            rows.removeAll();
            JLabel text = Theme.label(message, Font.PLAIN, 15, Theme.MUTED);
            text.setAlignmentX(CENTER_ALIGNMENT);
            rows.add(Box.createVerticalStrut(60));
            rows.add(text);
            finish(rows);
        }
    }

    private static void finish(JPanel rows) {
        rows.add(Box.createVerticalGlue());
        rows.revalidate();
        rows.repaint();
    }

    // =====================================================
    // Building blocks
    // =====================================================

    private static JPanel rowsPanel() {
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        return rows;
    }

    /** Card with a title and a scrolling list of rows. */
    private static JPanel card(String title, JPanel rows) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.TILE_BG);
        card.setBorder(BorderFactory.createLineBorder(Theme.LINE));

        JLabel heading = Theme.label(title, Font.BOLD, 18, Theme.TEXT);
        heading.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.add(heading, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(rows,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new DarkScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** Gold, silver, bronze for the top 3, null for everyone else. */
    private static Color medal(int position) {
        switch (position) {
            case 1: return GOLD;
            case 2: return SILVER;
            case 3: return BRONZE;
            default: return null;
        }
    }

    /** One line: position, portrait (may be null), name, team (may be empty), points. */
    private static JPanel row(int position, Avatar avatar, String name, String team,
            double points) {
        final Color medal = medal(position);
        final boolean podium = medal != null;

        // Podium rows: a soft tint of the medal colour and a stripe on the left.
        JPanel row = new JPanel(new GridBagLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (podium) {
                    g.setColor(Theme.blend(Theme.TILE_BG, medal, 0.12));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(medal);
                    g.fillRect(0, 0, 4, getHeight());
                }
            }
        };
        row.setOpaque(false);
        int pad = podium ? 14 : 10;
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(pad, 18, pad, 18)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 14);

        Color posColor = podium ? medal : Theme.TEXT;
        JLabel pos = Theme.label(String.valueOf(position), Font.BOLD,
                podium ? 22 : 18, posColor);
        pos.setPreferredSize(new Dimension(30, 24));
        c.gridx = 0;
        row.add(pos, c);

        if (avatar != null) {
            c.gridx = 1;
            row.add(avatar, c);
        }

        // The name takes the free space; the team column has a fixed width,
        // so the columns line up from row to row.
        c.gridx = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel nameLabel = Theme.label(name, Font.BOLD, podium ? 17 : 15, Theme.TEXT);
        nameLabel.setPreferredSize(new Dimension(10, 24));
        row.add(nameLabel, c);

        if (team.length() > 0) {
            c.gridx = 3;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            JLabel teamLabel = Theme.label(team, Font.PLAIN, 13, Theme.MUTED);
            teamLabel.setPreferredSize(new Dimension(110, 24));
            row.add(teamLabel, c);
        }

        c.gridx = 4;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 0, 0, 0);
        JLabel pts = Theme.label(formatPoints(points) + " pts", Font.BOLD,
                podium ? 17 : 15, podium ? medal : Theme.TEXT);
        pts.setHorizontalAlignment(SwingConstants.RIGHT);
        pts.setPreferredSize(new Dimension(70, 24));
        row.add(pts, c);

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** 25.0 becomes "25", 12.5 stays "12.5". */
    private static String formatPoints(double points) {
        return points == Math.rint(points)
                ? String.valueOf((long) points) : String.valueOf(points);
    }
}
