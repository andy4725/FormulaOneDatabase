package gui.TeamsMenu;

import java.awt.image.BufferedImage;

/**
 * A sponsor's name and logo (the logo is null if none was found).
 *
 * Only a small data holder for the Teams screen: the logo is loaded
 * once in TeamsPanel and kept here, so it is not read from disk again
 * every time a team is selected.
 */
class SponsorInfo {

    final String name;
    final BufferedImage logo;

    /** A null name is stored as an empty text. */
    SponsorInfo(String name, BufferedImage logo) {
        this.name = name == null ? "" : name;
        this.logo = logo;
    }
}