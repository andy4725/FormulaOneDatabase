package gui.MainMenu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Home screen: hero banner on top, four cards in the middle, footer below.
 */
class HomePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** The navigator (the main window) is handed to the cards, so a click can change screen. */
    HomePanel(Navigator navigator) {
        // BorderLayout: NORTH = top, CENTER = takes the rest of the space, SOUTH = bottom.
        super(new BorderLayout());
        setBackground(Theme.HOME_BG);

        add(new Heropanel(), BorderLayout.NORTH);
        add(buildTiles(navigator), BorderLayout.CENTER);

        // Replace these two texts with real data from SimulationData later.
        add(new FooterBar("SEASON 2026", "Bahrain Grand Prix"),
                BorderLayout.SOUTH);
    }

    /** One row of four cards; clicking a card opens that screen. */
    private JPanel buildTiles(Navigator navigator) {
        // GridLayout(1, 4): one row, four columns of equal width, 16 px apart.
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setBackground(Theme.HOME_BG);
        row.setBorder(BorderFactory.createEmptyBorder(8, 28, 24, 28));

        // Each card: screen to open, title, description and accent colour.
        // "|" in a description marks a line break.
        row.add(new NavTile(navigator, Section.DRIVERS, "DRIVERS",
                "Explore the driver|lineup, view stats and|manage racing talent.",
                new Color(225, 6, 0)));
        row.add(new NavTile(navigator, Section.TEAMS, "TEAMS",
                "Manage team rosters,|car performance and|development.",
                new Color(40, 120, 255)));
        row.add(new NavTile(navigator, Section.RACE, "RACE SIMULATION",
                "Choose a circuit and|set up your car for|the thrill of a race.",
                new Color(255, 110, 40)));
        row.add(new NavTile(navigator, Section.STANDINGS, "STANDINGS",
                "Track the season and|see who is leading|the championship.",
                new Color(155, 90, 255)));
        return row;
    }
}
