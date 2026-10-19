package gui.TeamsMenu;

import java.awt.image.BufferedImage;

/**
 * A sponsor's name and logo (the logo is null if none was found).
 */
class SponsorInfo {

    final String name;
    final BufferedImage logo;

    SponsorInfo(String name, BufferedImage logo) {
        this.name = name == null ? "" : name;
        this.logo = logo;
    }
}