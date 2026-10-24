package gui.StandingsMenu;

import gui.Common.BackgroundLoader;
import gui.MainMenu.Theme;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.DriverStanding;
import model.SimulationData;
import model.TeamStanding;

/**
 * Standings screen.
 *
 * Left card:  the drivers' championship (DriverStandingsView).
 * Right card: the teams' championship   (TeamStandingsView).
 *
 * This class only puts the two cards on the screen and asks
 * StandingsData for the lists; how a list looks is up to the views.
 *
 * The points are read from the database (the saved race results of
 * the slot's season), so MainWindow calls showSlot() every time
 * the screen is opened. That way new races always show up.
 */
public class StandingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final StandingsData data;
    private final BackgroundLoader loader = new BackgroundLoader();

    private final DriverStandingsView drivers;
    private final TeamStandingsView teams = new TeamStandingsView();

    /** The screen as used by the app: data comes from the database. */
    public StandingsPanel(SimulationData simulation) {
        this(simulation, new DatabaseStandingsData());
    }

    /** Any StandingsData can be passed in, e.g. fixed test data. */
    StandingsPanel(SimulationData simulation, StandingsData data) {
        this.data = data;
        // The driver card needs the drivers to find their portraits.
        drivers = new DriverStandingsView(simulation.getDrivers());

        // Dark background with some space around the edges.
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.HOME_BG);
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // Title at the top.
        add(Theme.label("Standings", Font.BOLD, 26, Theme.TEXT), BorderLayout.NORTH);

        // The two cards side by side, same width.
        JPanel cards = new JPanel(new GridLayout(1, 2, 18, 0));
        cards.setOpaque(false);
        cards.add(drivers);
        cards.add(teams);
        add(cards, BorderLayout.CENTER);

        showMessage("Choose a save slot first.");
    }

    /** What the background loading returns: both lists at once. */
    private static class Loaded {
        List<DriverStanding> drivers;
        List<TeamStanding> teams;
    }

    /** Reloads the standings of the slot's season. */
    public void showSlot(final int slot) {
        if (slot <= 0) {
            loader.cancel(); // a load for an older slot must not show up now
            showMessage("Choose a save slot first.");
            return;
        }
        showMessage("Loading...");

        loader.load(new Callable<Loaded>() {
            public Loaded call() throws Exception {
                int seasonId = data.findSeasonId(slot);
                if (seasonId <= 0) {
                    return null; // this slot has no season yet
                }
                Loaded loaded = new Loaded();
                loaded.drivers = data.getDriverStandings(seasonId);
                loaded.teams = data.getTeamStandings(seasonId);
                return loaded;
            }
        }, new BackgroundLoader.Callback<Loaded>() {
            public void loaded(Loaded loaded) {
                if (loaded == null) {
                    showMessage("This slot has no season yet.");
                } else {
                    drivers.showStandings(loaded.drivers);
                    teams.showStandings(loaded.teams);
                }
            }

            public void failed(Exception error) {
                error.printStackTrace();
                showMessage("The standings could not be loaded.");
            }
        });
    }

    /** Shows the same message in both cards (e.g. "Loading..."). */
    private void showMessage(String message) {
        drivers.showMessage(message);
        teams.showMessage(message);
    }
}
