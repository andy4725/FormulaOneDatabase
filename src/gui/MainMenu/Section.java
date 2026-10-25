package gui.MainMenu;

import java.util.Locale;

/**
 * The screens of the application. Each one has a sidebar label and icon.
 *
 * The sidebar makes one button per entry, in this order. The name()
 * of an entry (e.g. "STANDINGS") is also the key of its screen in the
 * CardLayout of MainWindow.
 */
enum Section {

    HOME("Home", Icons.HOME),
    DRIVERS("Drivers", Icons.DRIVER),
    TEAMS("Teams", Icons.TEAMS),
    RACE("Race Simulation", Icons.RACE),
    STANDINGS("Standings", Icons.STANDINGS),
    RESULTS("Race Results", Icons.RESULTS);

    final String label; // text on the sidebar button
    final int icon;     // one of the Icons constants

    Section(String label, int icon) {
        this.label = label;
        this.icon = icon;
    }

    /** Name of the optional card picture, e.g. "drivers" for assets/drivers.jpg. */
    String imageName() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}