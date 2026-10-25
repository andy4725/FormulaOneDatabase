package gui.ResultsMenu;

import java.sql.SQLException;
import java.util.List;

import model.RaceResultEntry;
import model.RaceSummary;

/**
 * Where the Results screen gets its data from.
 *
 * The screen only knows this interface, not the database. The real
 * version is DatabaseResultsData; a test could pass in a version with
 * fixed data instead, without changing the screen.
 */
interface ResultsData {

    /** The newest season of a save slot, or 0 when the slot has none. */
    int findSeasonId(int saveSlot) throws SQLException;

    /** Every race of the season in round order. */
    List<RaceSummary> getRaces(int seasonId) throws SQLException;

    /** The classification of one race. */
    List<RaceResultEntry> getRaceResults(int seasonEventId) throws SQLException;

    /** One driver's lap times in one race; index 0 is lap 1. */
    List<Double> getLapTimes(int seasonEventId, int driverId) throws SQLException;
}
