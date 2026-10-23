package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import database.Database;
import model.RaceResult;

public class RaceRepository {
    public Map<Integer, Integer> findGridPositions(int seasonEventId) throws SQLException {
        validateEventId(seasonEventId);

        String sql = "SELECT driver_id, grid_position FROM race_results "
                + "WHERE season_event_id = ? AND grid_position IS NOT NULL "
                + "ORDER BY grid_position";

        Map<Integer, Integer> gridPositions = new HashMap<Integer, Integer>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    gridPositions.put(
                            result.getInt("driver_id"),
                            result.getInt("grid_position"));
                }
            }
        }

        if (gridPositions.isEmpty()) {
            gridPositions = findQualifyingPositions(seasonEventId);
        }

        return gridPositions;
    }

    /**
     * Fallback when the grid was never copied to race_results (e.g. older
     * saves): the final qualifying classification is the starting grid.
     */
    private Map<Integer, Integer> findQualifyingPositions(int seasonEventId) throws SQLException {
        String sql = "SELECT driver_id, final_position FROM qualifying_results "
                + "WHERE season_event_id = ? ORDER BY final_position";

        Map<Integer, Integer> gridPositions = new HashMap<Integer, Integer>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    gridPositions.put(
                            result.getInt("driver_id"),
                            result.getInt("final_position"));
                }
            }
        }

        return gridPositions;
    }

    public void saveResults(
            int seasonEventId,
            List<RaceResult> results,
            Map<Integer, Integer> pointsByDriverId) throws SQLException {
        validateEventId(seasonEventId);
        validateResults(results);
        validatePoints(results, pointsByDriverId);

        double fastestLap = findFastestLap(results);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                deleteLapTimes(connection, seasonEventId);
                deleteRaceResults(connection, seasonEventId);
                insertRaceResults(
                        connection,
                        seasonEventId,
                        results,
                        pointsByDriverId,
                        fastestLap);
                insertLapTimes(connection, seasonEventId, results);
                markRaceComplete(connection, seasonEventId);
                markSeasonCompleteIfAllRacesFinished(connection, seasonEventId);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void markRaceComplete(
            Connection connection,
            int seasonEventId) throws SQLException {
        String sql = "UPDATE season_events SET status = 'RACE_COMPLETE' "
                + "WHERE season_event_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "The season event does not exist: " + seasonEventId);
            }
        }
    }

    private void markSeasonCompleteIfAllRacesFinished(
            Connection connection,
            int seasonEventId) throws SQLException {
        String sql = "UPDATE seasons "
                + "SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP "
                + "WHERE season_id = ("
                + "SELECT season_id FROM season_events WHERE season_event_id = ?"
                + ") AND status <> 'COMPLETED' AND NOT EXISTS ("
                + "SELECT 1 FROM season_events "
                + "WHERE season_id = seasons.season_id "
                + "AND status <> 'RACE_COMPLETE'"
                + ")";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.executeUpdate();
        }
    }

    private void insertRaceResults(
            Connection connection,
            int seasonEventId,
            List<RaceResult> results,
            Map<Integer, Integer> pointsByDriverId,
            double fastestLap) throws SQLException {

        String sql = "INSERT INTO race_results "
                + "(season_event_id, driver_id, grid_position, "
                + "finishing_position, result_status, points, fastest_lap) "
                + "VALUES (?, ?, ?, ?, 'FINISHED', ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (RaceResult result : results) {
                int driverId = result.getDriver().getDriverId();

                statement.setInt(1, seasonEventId);
                statement.setInt(2, driverId);
                statement.setInt(3, result.getGridPosition());
                statement.setInt(4, result.getFinishingPosition());
                statement.setInt(5, pointsByDriverId.get(driverId));
                statement.setInt(6, hasFastestLap(result, fastestLap) ? 1 : 0);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void insertLapTimes(
            Connection connection,
            int seasonEventId,
            List<RaceResult> results) throws SQLException {

        String sql = "INSERT INTO race_laps "
                + "(season_event_id, driver_id, lap_number, lap_time_seconds) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (RaceResult result : results) {
                for (int lap = 0; lap < result.getLapTimes().size(); lap++) {
                    statement.setInt(1, seasonEventId);
                    statement.setInt(2, result.getDriver().getDriverId());
                    statement.setInt(3, lap + 1);
                    statement.setDouble(
                            4,
                            roundToMilliseconds(
                                    result.getLapTimes().get(lap)));
                    statement.addBatch();
                }
            }

            statement.executeBatch();
        }
    }

    private void deleteLapTimes(
            Connection connection,
            int seasonEventId) throws SQLException {
        String sql = "DELETE FROM race_laps WHERE season_event_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.executeUpdate();
        }
    }

    private void deleteRaceResults(
            Connection connection,
            int seasonEventId) throws SQLException {
        String sql = "DELETE FROM race_results WHERE season_event_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.executeUpdate();
        }
    }

    private double findFastestLap(List<RaceResult> results) {
        double fastestLap = Double.MAX_VALUE;

        for (RaceResult result : results) {
            for (Double lapTime : result.getLapTimes()) {
                fastestLap = Math.min(
                        fastestLap,
                        roundToMilliseconds(lapTime));
            }
        }

        return fastestLap;
    }

    private boolean hasFastestLap(RaceResult result, double fastestLap) {
        for (Double lapTime : result.getLapTimes()) {
            if (Double.compare(
                    roundToMilliseconds(lapTime),
                    fastestLap) == 0) {
                return true;
            }
        }

        return false;
    }

    private void validateResults(List<RaceResult> results) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one race result is required.");
        }

        Set<Integer> driverIds = new HashSet<Integer>();
        Set<Integer> gridPositions = new HashSet<Integer>();
        Set<Integer> finishingPositions = new HashSet<Integer>();

        for (RaceResult result : results) {
            if (result == null || result.getDriver() == null
                    || result.getLapTimes() == null
                    || result.getLapTimes().isEmpty()) {
                throw new IllegalArgumentException(
                        "Each result needs a driver and lap times.");
            }

            if (!driverIds.add(result.getDriver().getDriverId())) {
                throw new IllegalArgumentException(
                        "A driver can only appear once in race results.");
            }
            if (!gridPositions.add(result.getGridPosition())
                    || !finishingPositions.add(
                            result.getFinishingPosition())) {
                throw new IllegalArgumentException(
                        "Grid and finishing positions must be unique.");
            }

            for (Double lapTime : result.getLapTimes()) {
                if (lapTime == null || lapTime <= 0) {
                    throw new IllegalArgumentException(
                            "Lap times must be positive.");
                }
            }
        }
    }

    private void validatePoints(
            List<RaceResult> results,
            Map<Integer, Integer> pointsByDriverId) {
        if (pointsByDriverId == null
                || pointsByDriverId.size() != results.size()) {
            throw new IllegalArgumentException(
                    "Points are required for every race result.");
        }

        for (RaceResult result : results) {
            Integer points =
                    pointsByDriverId.get(result.getDriver().getDriverId());

            if (points == null || points < 0) {
                throw new IllegalArgumentException(
                        "Each driver must have a nonnegative points value.");
            }
        }
    }

    private void validateEventId(int seasonEventId) {
        if (seasonEventId <= 0) {
            throw new IllegalArgumentException(
                    "A valid season event ID is required.");
        }
    }

    private double roundToMilliseconds(double seconds) {
        return Math.round(seconds * 1000.0) / 1000.0;
    }
}
