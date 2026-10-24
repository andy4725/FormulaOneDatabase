package gui.StandingsMenu;

import java.sql.SQLException;
import java.util.List;

import model.DriverStanding;
import model.TeamStanding;

/**
 * Where the Standings screen gets its data from.
 *
 * The screen only knows this interface, not the database. The real
 * version is DatabaseStandingsData; a test could pass in a version with
 * fixed data instead, without changing the screen.
 */
interface StandingsData {

    /** The newest season of a save slot, or 0 when the slot has none. */
    int findSeasonId(int saveSlot) throws SQLException;

    /** Drivers' championship, leader first. */
    List<DriverStanding> getDriverStandings(int seasonId) throws SQLException;

    /** Teams' championship, leader first. */
    List<TeamStanding> getTeamStandings(int seasonId) throws SQLException;
}
