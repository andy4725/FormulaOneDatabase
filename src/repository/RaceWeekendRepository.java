package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.Database;
import model.Driver;
import model.QualifyingResult;
import model.WeekendEvent;

/** Reads and writes the state of the current race weekend. */
public class RaceWeekendRepository {

    /**
     * The next weekend to play in a save slot: the first round (of the
     * slot's season, if it is in progress) whose race has no finishing
     * positions yet.
     * Returns null when there is no such weekend.
     */
    public WeekendEvent findCurrentEvent(int saveSlotId) throws SQLException {
        String sql = "SELECT se.season_event_id, se.round_number, gp.name, gp.circuit_id "
                + "FROM season_events se "
                + "JOIN seasons s ON s.season_id = se.season_id "
                + "JOIN grand_prix gp ON gp.grand_prix_id = se.grand_prix_id "
                + "WHERE s.save_slot_id = ? AND s.status = 'IN_PROGRESS' "
                + "AND NOT EXISTS (SELECT 1 FROM race_results rr "
                + "    WHERE rr.season_event_id = se.season_event_id "
                + "    AND rr.finishing_position IS NOT NULL) "
                + "ORDER BY se.round_number LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saveSlotId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new WeekendEvent(
                        result.getInt("season_event_id"),
                        result.getInt("round_number"),
                        result.getString("name"),
                        result.getInt("circuit_id"));
            }
        }
    }

    /** Final qualifying order (the starting grid), best lap included. */
    public List<QualifyingResult> loadStartingGrid(
            int seasonEventId, List<Driver> drivers) throws SQLException {
        Map<Integer, Driver> driversById = new HashMap<Integer, Driver>();
        for (Driver driver : drivers) {
            driversById.put(Integer.valueOf(driver.getDriverId()), driver);
        }

        String sql = "SELECT driver_id, best_lap_seconds FROM qualifying_results "
                + "WHERE season_event_id = ? ORDER BY final_position";
        List<QualifyingResult> grid = new ArrayList<QualifyingResult>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Driver driver = driversById.get(Integer.valueOf(result.getInt("driver_id")));
                    if (driver != null) {
                        grid.add(new QualifyingResult(driver, 0.0,
                                result.getDouble("best_lap_seconds")));
                    }
                }
            }
        }
        return grid;
    }

    /** Sets season_events.status, e.g. QUALIFYING_IN_PROGRESS. */
    public void updateStatus(int seasonEventId, String status) throws SQLException {
        String sql = "UPDATE season_events SET status = ? WHERE season_event_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, seasonEventId);
            statement.executeUpdate();
        }
    }

    /**
     * Deletes every qualifying lap, the final classification and the grid
     * of one weekend, so qualifying can be simulated again from Quali 1.
     */
    public void resetQualifying(int seasonEventId) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                delete(connection, "DELETE FROM qualifying_laps WHERE season_event_id = ?", seasonEventId);
                delete(connection, "DELETE FROM qualifying_results WHERE season_event_id = ?", seasonEventId);
                delete(connection, "DELETE FROM race_results WHERE season_event_id = ? "
                        + "AND finishing_position IS NULL", seasonEventId);
                delete(connection, "UPDATE season_events SET status = 'NOT_STARTED' "
                        + "WHERE season_event_id = ?", seasonEventId);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void delete(Connection connection, String sql, int seasonEventId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.executeUpdate();
        }
    }
}