package gui.MainMenu;

import gui.DriversMenu.DriversPanel;
import gui.TeamsMenu.TeamsPanel;
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
 * It only puts the pieces together: a Sidebar on the left and a
 * CardLayout on the right that shows one screen at a time.
 */
public class MainWindow extends JFrame implements Navigator {

    private static final long serialVersionUID = 1L;

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final SideBar sidebar = new SideBar(this);

    public MainWindow(SimulationData data) {
        super("F1 2026 Simulator");

        content.setBackground(Theme.HOME_BG);
        content.add(new HomePanel(this), Section.HOME.name());
        content.add(new DriversPanel(data), Section.DRIVERS.name());
        content.add(new TeamsPanel(), Section.TEAMS.name());
        content.add(placeholder(
                "Simulate Race: choose circuit, run, see results"),
                Section.RACE.name());
        content.add(placeholder(
                "Standings: driver and team rankings"),
                Section.STANDINGS.name());

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        showSection(Section.HOME);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
    }

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