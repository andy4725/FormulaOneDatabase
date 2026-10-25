package gui.ResultsMenu;

import gui.Common.BackgroundLoader;
import gui.Common.Flags;
import gui.MainMenu.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.RaceResultEntry;
import model.RaceSummary;
import model.SimulationData;

/**
 * Results screen, three levels deep:
 *
 *   1. Every race of the season          (RaceListView)
 *   2. Click a race: its result          (RaceResultView)
 *   3. Click a driver: their lap times   (LapTimesView)
 *
 * This class only does the navigation: the header with the Back button,
 * which level is shown, and asking ResultsData for what a level needs.
 * How each level looks is up to its view class.
 *
 * MainWindow calls showSlot() every time the screen is opened, so new
 * races always show up.
 */
public class ResultsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Keys of the three levels in the CardLayout.
    private static final String RACES  = "races";
    private static final String RESULT = "result";
    private static final String LAPS   = "laps";

    private final ResultsData data;
    private final BackgroundLoader loader = new BackgroundLoader();

    // The three levels.
    private final RaceListView raceList;
    private final RaceResultView raceResult;
    private final LapTimesView lapTimes = new LapTimesView();

    private final CardLayout views = new CardLayout();
    private final JPanel viewHolder = new JPanel(views);
    private String currentView = RACES;

    // Header: back button, title and a grey line under it.
    private final JButton back = backButton();
    private final JLabel title = Theme.label("Race Results", Font.BOLD, 26, Theme.TEXT);
    private final JLabel subtitle = Theme.label(" ", Font.PLAIN, 14, Theme.MUTED);

    // The race that is open in level 2 (and 3), for the titles.
    private RaceSummary openRace;

    /** The screen as used by the app: data comes from the database. */
    public ResultsPanel(SimulationData simulation) {
        this(simulation, new DatabaseResultsData());
    }

    /** Any ResultsData can be passed in, e.g. fixed test data. */
    ResultsPanel(SimulationData simulation, ResultsData data) {
        this.data = data;

        // The views only report clicks; deciding what happens next is done here.
        raceList = new RaceListView(new RaceListView.Listener() {
            public void raceSelected(RaceSummary race) {
                openRace(race);
            }
        });
        raceResult = new RaceResultView(simulation.getDrivers(), new RaceResultView.Listener() {
            public void driverSelected(RaceResultEntry entry) {
                openDriver(entry);
            }
        });

        // Header on top (NORTH), the current level in the rest (CENTER).
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.HOME_BG);
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        add(buildHeader(), BorderLayout.NORTH);

        viewHolder.setOpaque(false);
        viewHolder.add(raceList, RACES);
        viewHolder.add(raceResult, RESULT);
        viewHolder.add(lapTimes, LAPS);
        add(viewHolder, BorderLayout.CENTER);

        showView(RACES);
        raceList.showMessage("Choose a save slot first.");
    }

    // =====================================================
    // The three levels
    // =====================================================

    /** Level 1: reloads the races of the slot's season and goes back to the list. */
    public void showSlot(int slot) {
        loadRaces(slot, 0);
    }

    /**
     * Like showSlot(), then opens one race's result straight away (used by the
     * race weekend's "View race results" button). Back still leads to the list.
     */
    public void showRace(int slot, int seasonEventId) {
        loadRaces(slot, seasonEventId);
    }

    /** Loads the race list; openEventId > 0 also opens that race once it is loaded. */
    private void loadRaces(final int slot, final int openEventId) {
        showView(RACES);
        if (slot <= 0) {
            loader.cancel();
            raceList.showMessage("Choose a save slot first.");
            return;
        }
        raceList.showMessage("Loading...");

        loader.load(new Callable<List<RaceSummary>>() {
            public List<RaceSummary> call() throws Exception {
                int seasonId = data.findSeasonId(slot);
                return seasonId <= 0 ? null : data.getRaces(seasonId);
            }
        }, new BackgroundLoader.Callback<List<RaceSummary>>() {
            public void loaded(List<RaceSummary> races) {
                if (races == null) {
                    raceList.showMessage("This slot has no season yet.");
                } else {
                    raceList.showRaces(races);
                    openRaceById(races, openEventId);
                }
            }

            public void failed(Exception error) {
                error.printStackTrace();
                raceList.showMessage("The races could not be loaded.");
            }
        });
    }

    /** Opens the finished race with this id; nothing happens if it is not in the list. */
    private void openRaceById(List<RaceSummary> races, int seasonEventId) {
        for (RaceSummary race : races) {
            if (race.getSeasonEventId() == seasonEventId && race.isComplete()) {
                openRace(race);
                return;
            }
        }
    }

    /** Level 2: shows the clicked race and loads its result. */
    private void openRace(final RaceSummary race) {
        openRace = race;
        showView(RESULT);
        raceResult.showMessage("Loading...");

        loader.load(new Callable<List<RaceResultEntry>>() {
            public List<RaceResultEntry> call() throws Exception {
                return data.getRaceResults(race.getSeasonEventId());
            }
        }, new BackgroundLoader.Callback<List<RaceResultEntry>>() {
            public void loaded(List<RaceResultEntry> entries) {
                raceResult.showResults(entries);
            }

            public void failed(Exception error) {
                error.printStackTrace();
                raceResult.showMessage("The result could not be loaded.");
            }
        });
    }

    /** Level 3: shows the clicked driver's totals at once, then loads the laps. */
    private void openDriver(final RaceResultEntry entry) {
        final RaceSummary race = openRace;
        title.setText(entry.getDriverName());
        subtitle.setText(race.getGrandPrixName() + "  \u2022  " + entry.getTeamName());
        subtitle.setIcon(null);
        lapTimes.showDriver(entry);
        showViewOnly(LAPS);

        loader.load(new Callable<List<Double>>() {
            public List<Double> call() throws Exception {
                return data.getLapTimes(race.getSeasonEventId(), entry.getDriverId());
            }
        }, new BackgroundLoader.Callback<List<Double>>() {
            public void loaded(List<Double> laps) {
                lapTimes.showLaps(laps, entry);
            }

            public void failed(Exception error) {
                error.printStackTrace();
                lapTimes.showMessage("The lap times could not be loaded.");
            }
        });
    }

    // =====================================================
    // Switching levels
    // =====================================================

    /** Shows a level and sets the matching titles. */
    private void showView(String view) {
        if (RACES.equals(view)) {
            title.setText("Race Results");
            subtitle.setText("Click a race to see its result");
            subtitle.setIcon(null);
        } else if (RESULT.equals(view) && openRace != null) {
            title.setText("R" + openRace.getRoundNumber() + "  " + openRace.getGrandPrixName());
            subtitle.setText(openRace.getCircuitName() + ", " + openRace.getCountry()
                    + "  \u2022  Click a driver to see their lap times");
            subtitle.setIcon(Flags.get(openRace.getCountry(), 14));
        }
        showViewOnly(view);
    }

    /** Shows a level without touching the titles. */
    private void showViewOnly(String view) {
        currentView = view;
        back.setVisible(!RACES.equals(view));
        views.show(viewHolder, view);
    }

    /** Back goes up one level: laps -> result -> races. */
    private void goBack() {
        // Any load still running belongs to the level we are leaving.
        loader.cancel();
        showView(LAPS.equals(currentView) ? RESULT : RACES);
    }

    // =====================================================
    // Header
    // =====================================================

    /** [< Back]  Title / subtitle */
    private JPanel buildHeader() {
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        subtitle.setIconTextGap(8); // space between the circuit's flag and the text

        // GridBagLayout centres the button vertically at its own size,
        // instead of stretching it over the height of both title lines.
        JPanel backHolder = new JPanel(new GridBagLayout());
        backHolder.setOpaque(false);
        backHolder.add(back);

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(backHolder, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);
        return header;
    }

    /** Flat dark button that calls goBack() when clicked. */
    private JButton backButton() {
        JButton button = new JButton("\u2039  Back");
        button.setFont(Theme.font(Font.BOLD, 14));
        button.setForeground(Theme.TEXT);
        button.setBackground(Theme.TILE_BG);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });
        return button;
    }
}
