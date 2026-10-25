package gui.StandingsMenu;

import gui.Common.ListCard;
import gui.RaceMenu.Avatar;
import gui.RaceMenu.PortraitCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Driver;
import model.DriverStanding;

/** Left card of the Standings screen: the drivers' championship. */
class DriverStandingsView extends ListCard {

    private static final long serialVersionUID = 1L;

    // DriverStanding only has the driver's id, but the portrait loader
    // needs the Driver object, so we keep the drivers by id.
    private final Map<Integer, Driver> driversById = new HashMap<Integer, Driver>();
    private final PortraitCache portraits = new PortraitCache();

    DriverStandingsView(List<Driver> drivers) {
        super("Drivers");
        for (Driver driver : drivers) {
            driversById.put(Integer.valueOf(driver.getDriverId()), driver);
        }
    }

    /** Fills the card with one row per driver (portrait, name, team, points). */
    void showStandings(List<DriverStanding> standings) {
        List<StandingRow> rows = new ArrayList<StandingRow>();
        for (DriverStanding s : standings) {
            // Portrait of the driver (initials are shown if there is no picture).
            Driver driver = driversById.get(Integer.valueOf(s.getDriverId()));
            Avatar avatar = new Avatar(driver == null ? null : portraits.get(driver),
                    s.getDriverName(), 44);

            rows.add(new StandingRow(s.getPosition(), avatar, s.getDriverName(),
                    s.getTeamName(), s.getPoints()));
        }
        showRows(rows, "No drivers found.");
    }
}
