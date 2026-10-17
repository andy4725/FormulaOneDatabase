package gui.MainMenu;

import java.util.Locale;

/**
 * The screens of the application. Each one has a sidebar label and icon.
 */
enum Section {

    HOME("Home", Icons.HOME),
    DRIVERS("Drivers", Icons.DRIVER),
    TEAMS("Teams", Icons.TEAMS),
    RACE("Race Simulation", Icons.RACE),
    STANDINGS("Standings", Icons.STANDINGS);

    final String label;
    final int icon;

    Section(String label, int icon) {
        this.label = label;
        this.icon = icon;
    }

    /** Name of the optional card picture, e.g. "drivers" for assets/drivers.jpg. */
    String imageName() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}