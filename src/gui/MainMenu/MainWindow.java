package gui.MainMenu;

import gui.DriversMenu.DriversPanel;
import gui.TeamsMenu.TeamsPanel;
import gui.Start.IntroListener;
import gui.Start.SaveSlots;
import gui.Start.SlotPanel;
import gui.Start.StartPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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

    public MainWindow(SimulationData data) {
        super("F1 2026 Simulator");

        // ----- main menu -----
        content.setBackground(Theme.HOME_BG);
        content.add(new HomePanel(this), Section.HOME.name());
        content.add(new DriversPanel(data), Section.DRIVERS.name());
        content.add(new TeamsPanel(data), Section.TEAMS.name());
        content.add(placeholder(
                "Simulate Race: choose circuit, run, see results"),
                Section.RACE.name());
        content.add(placeholder(
                "Standings: driver and team rankings"),
                Section.STANDINGS.name());

        JPanel app = new JPanel(new BorderLayout());
        app.add(sidebar, BorderLayout.WEST);
        app.add(content, BorderLayout.CENTER);

        // ----- start flow -----
        root.setBackground(Theme.HOME_BG);
        root.add(new StartPanel(this), START);
        root.add(new SlotPanel(this), SLOTS);
        root.add(app, APP);
        add(root);

        showSection(Section.HOME);
        screens.show(root, START);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
    }

    // =====================================================
    // START FLOW (IntroListener)
    // =====================================================

    public void onStart() {
        screens.show(root, SLOTS);
    }

    public void onBack() {
        screens.show(root, START);
    }

    public void onSlotChosen(int slot) {
        currentSlot = slot;
        SaveSlots.markUsed(slot);
        setTitle("F1 2026 Simulator - Slot " + slot);
        showSection(Section.HOME);
        screens.show(root, APP);
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
        cards.show(content, section.name());
        sidebar.setCurrent(section);
    }

    /** Temporary screen for parts that are not built yet. */
    private JComponent placeholder(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(Theme.font(Font.PLAIN, 16));
        label.setForeground(Theme.MUTED);
        label.setOpaque(true);
        label.setBackground(Theme.HOME_BG);
        return label;
    }
}