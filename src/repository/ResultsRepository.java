package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import model.RaceResultEntry;
import model.RaceSummary;

/** Read-only queries that assemble season and race data for the results view.
 * Race lap totals and best laps are calculated from the stored lap records.
 */
public class ResultsRepository {

    /** Every race of the season in round order, with the winner if it was run. */
    public List<RaceSummary> getRaces(int seasonId) throws SQLException {
        validateId(seasonId, "season");

        String sql = "SELECT se.season_event_id, se.round_number, se.status, "
                + "gp.name AS grand_prix_name, c.name AS circuit_name, c.country, "
                + "(SELECT d.name FROM race_results rr "
                + "JOIN drivers d ON d.driver_id = rr.driver_id "
                + "WHERE rr.season_event_id = se.season_event_id "
                + "AND rr.finishing_position = 1) AS winner_name "
                + "FROM season_events se "
                + "JOIN grand_prix gp ON gp.grand_prix_id = se.grand_prix_id "
                + "JOIN circuits c ON c.circuit_id = gp.circuit_id "
                + "WHERE se.season_id = ? "
                + "ORDER BY se.round_number";

        List<RaceSummary> races = new ArrayList<RaceSummary>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    races.add(new RaceSummary(
                            result.getInt("season_event_id"),
                            result.getInt("round_number"),
                            result.getString("grand_prix_name"),
                            result.getString("circuit_name"),
                            result.getString("country"),
                            result.getString("status"),
                            result.getString("winner_name")));
                }
            }
        }

        return races;
    }

    /** The classification of one race: classified drivers first, in finishing order. */
    public List<RaceResultEntry> getRaceResults(int seasonEventId) throws SQLException {
        validateId(seasonEventId, "season event");

        // LEFT JOIN retains a result row even when a driver has no recorded laps.
        String sql = "SELECT rr.driver_id, d.name AS driver_name, t.name AS team_name, "
                + "rr.grid_position, rr.finishing_position, rr.result_status, "
                + "rr.points, rr.fastest_lap, "
                + "COUNT(rl.race_lap_id) AS lap_count, "
                + "COALESCE(SUM(rl.lap_time_seconds), 0) AS total_time, "
                + "COALESCE(MIN(rl.lap_time_seconds), 0) AS best_lap "
                + "FROM race_results rr "
                + "JOIN drivers d ON d.driver_id = rr.driver_id "
                + "JOIN teams t ON t.team_id = d.team_id "
                + "LEFT JOIN race_laps rl "
                + "ON rl.season_event_id = rr.season_event_id "
                + "AND rl.driver_id = rr.driver_id "
                + "WHERE rr.season_event_id = ? "
                + "GROUP BY rr.driver_id "
                + "ORDER BY rr.finishing_position IS NULL, rr.finishing_position, "
                + "d.name COLLATE NOCASE";

        List<RaceResultEntry> entries = new ArrayList<RaceResultEntry>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    // getInt() returns 0 for NULL, which the model treats as "unknown".
                    String status = result.getString("result_status");
                    entries.add(new RaceResultEntry(
                            result.getInt("driver_id"),
                            result.getString("driver_name"),
                            result.getString("team_name"),
                            result.getInt("grid_position"),
                            result.getInt("finishing_position"),
                            status == null ? "FINISHED" : status,
                            result.getDouble("points"),
                            result.getInt("fastest_lap") == 1,
                            result.getInt("lap_count"),
                            result.getDouble("total_time"),
                            result.getDouble("best_lap")));
                }
            }
        }

        return entries;
    }

    /** One driver's lap times in one race; index 0 is lap 1. */
    public List<Double> getLapTimes(int seasonEventId, int driverId) throws SQLException {
        validateId(seasonEventId, "season event");
        validateId(driverId, "driver");

        String sql = "SELECT lap_time_seconds FROM race_laps "
                + "WHERE season_event_id = ? AND driver_id = ? "
                + "ORDER BY lap_number";

        List<Double> lapTimes = new ArrayList<Double>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.setInt(2, driverId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    lapTimes.add(result.getDouble("lap_time_seconds"));
                }
            }
        }

        return lapTimes;
    }

    private void validateId(int id, String what) {
        if (id <= 0) {
            throw new IllegalArgumentException("A valid " + what + " ID is required.");
        }
    }
}
