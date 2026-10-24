package gui.MainMenu;

import gui.DriversMenu.DriversPanel;
import gui.RaceMenu.RaceWeekendPanel;
import gui.ResultsMenu.ResultsPanel;
import gui.Start.IntroListener;
import gui.Start.SaveSlots;
import gui.Start.SlotPanel;
import gui.Start.StartPanel;
import gui.StandingsMenu.StandingsPanel;
import gui.TeamsMenu.TeamsPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import model.SimulationData;

/**
 * Main window of the Formula One 2026 simulator.
 *
 * Flow: start screen -> slot selection -> main menu.
 *
 * The outer CardLayout (root) switches between those three. The main
 * menu is a Sidebar on the left and an inner CardLayout (content) that
 * shows one screen at a time.
 */
public class MainWindow extends JFrame implements Navigator, IntroListener {

    private static final long serialVersionUID = 1L;

    private static final String START = "start";
    private static final String SLOTS = "slots";
    private static final String APP   = "app";

    // Outer screens: start, slots, main menu.
    private final CardLayout screens = new CardLayout();
    private final JPanel root = new JPanel(screens);

    // Main menu screens.
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final SideBar sidebar = new SideBar(this);

    // Slot chosen by the player (0 = none yet).
    private int currentSlot = 0;

    // True while a chosen slot is being prepared in the background.
    private boolean openingSlot = false;

    private final RaceWeekendPanel racePanel;
    private final StandingsPanel standingsPanel;
    private final ResultsPanel resultsPanel;

    /** Builds every screen once; data holds the drivers, teams etc. read at start-up. */
    public MainWindow(SimulationData data) {
        super("F1 2026 Simulator"); // the window title

        // ----- main menu -----
        // Every screen is added to the CardLayout with its Section name as key;
        // showSection() later shows one of them by that key.
        content.setBackground(Theme.HOME_BG);
        content.add(new HomePanel(this), Section.HOME.name());
        content.add(new DriversPanel(data), Section.DRIVERS.name());
        content.add(new TeamsPanel(data), Section.TEAMS.name());
        // Its "View race results" button opens that race on the Results screen.
        racePanel = new RaceWeekendPanel(data, new RaceWeekendPanel.Listener() {
            public void showRaceResult(int seasonEventId) {
                resultsPanel.showRace(currentSlot, seasonEventId);
                cards.show(content, Section.RESULTS.name());
                sidebar.setCurrent(Section.RESULTS);
            }
        });
        content.add(racePanel, Section.RACE.name());
        // Standings screen; it needs the drivers to find their portraits.
        standingsPanel = new StandingsPanel(data);
        content.add(standingsPanel, Section.STANDINGS.name());
        // Results screen: races -> one race's result -> one driver's laps.
        resultsPanel = new ResultsPanel(data);
        content.add(resultsPanel, Section.RESULTS.name());

        // The main menu: sidebar on the left, the current screen in the rest.
        JPanel app = new JPanel(new BorderLayout());
        app.add(sidebar, BorderLayout.WEST);
        app.add(content, BorderLayout.CENTER);

        // ----- start flow -----
        root.setBackground(Theme.HOME_BG);
        root.add(new StartPanel(this), START);
        root.add(new SlotPanel(this), SLOTS);
        root.add(app, APP);
        add(root);

        // The program starts on the start screen, with Home ready behind it.
        showSection(Section.HOME);
        screens.show(root, START);

        // Closing the window ends the program. Start at 1200x760, never
        // smaller than 1000x640, in the middle of the screen (null).
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
    }

    // =====================================================
    // START FLOW (IntroListener)
    // =====================================================

    /** "Start" was clicked on the start screen: go to the slot selection. */
    public void onStart() {
        screens.show(root, SLOTS);
    }

    /** "Back" was clicked on the slot screen: return to the start screen. */
    public void onBack() {
        screens.show(root, START);
    }

    /** A save slot was clicked: prepare its season, then open the main menu. */
    public void onSlotChosen(final int slot) {
        // Ignore extra clicks while a slot is already being opened.
        if (openingSlot) {
            return;
        }
        openingSlot = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Starting a new season writes to the database and can take a
        // moment, so it runs in the background. Doing it directly here
        // (on the Swing thread) would freeze the window until it finished.
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return Boolean.valueOf(SaveSlots.markUsed(slot));
            }

            // Back on the Swing thread: now it is safe to change screens.
            @Override
            protected void done() {
                openingSlot = false;
                setCursor(Cursor.getDefaultCursor());

                boolean ok;
                try {
                    ok = get().booleanValue();
                } catch (Exception e) {
                    ok = false;
                }
                if (!ok) {
                    // Stay on the slot screen instead of opening an empty game.
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Slot " + slot + " could not be opened. Check the database.",
                            "Open slot", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                currentSlot = slot;
                racePanel.showSlot(slot);
                setTitle("F1 2026 Simulator - Slot " + slot);
                showSection(Section.HOME);
                screens.show(root, APP);
            }
        }.execute();
    }

    /** The slot the player chose (1, 2 or 3); results are saved in it. */
    public int getCurrentSlot() {
        return currentSlot;
    }

    // =====================================================
    // MAIN MENU (Navigator)
    // =====================================================

    /** Shows one screen and highlights its sidebar button. */
    public void showSection(Section section) {
        if (section == Section.STANDINGS) {
            // Reload every time, so races finished since the last visit show up.
            standingsPanel.showSlot(currentSlot);
        } else if (section == Section.RESULTS) {
            // Same for the results: start again at the list of races.
            resultsPanel.showSlot(currentSlot);
        }
        cards.show(content, section.name());
        sidebar.setCurrent(section);
    }
}