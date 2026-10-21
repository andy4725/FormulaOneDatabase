package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.Database;
import model.Driver;
import model.QualifyingResult;

/** Reads and writes qualifying session data. */
public class QualifyingRepository {

    public void saveSessionLaps(
            int seasonEventId, String session, List<QualifyingResult> results)
            throws SQLException {
        validateEventId(seasonEventId);
        validateSession(session);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteSessionLaps(connection, seasonEventId, session);
                insertSessionLaps(connection, seasonEventId, session, results);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public boolean sessionHasLaps(int seasonEventId, String session)
            throws SQLException {
        validateEventId(seasonEventId);
        validateSession(session);

        String sql = "SELECT 1 FROM qualifying_laps "
                + "WHERE season_event_id = ? AND session = ? LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.setString(2, session);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public int finalResultsCount(int seasonEventId) throws SQLException {
        validateEventId(seasonEventId);
        String sql = "SELECT COUNT(*) FROM qualifying_results WHERE season_event_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    public List<Driver> findAdvancingDrivers(
            int seasonEventId, String session, int limit, List<Driver> allDrivers)
            throws SQLException {
        Map<Integer, Driver> driversById = indexDriversById(allDrivers);
        List<Integer> driverIds = findAdvancingDriverIds(seasonEventId, session, limit);
        List<Driver> advancingDrivers = new ArrayList<Driver>();

        for (Integer driverId : driverIds) {
            Driver driver = driversById.get(driverId);
            if (driver == null) {
                throw new SQLException("No driver found for ID " + driverId + ".");
            }
            advancingDrivers.add(driver);
        }
        return advancingDrivers;
    }

    /** Returns driver IDs in session order, fastest first. */
    private List<Integer> findAdvancingDriverIds(
            int seasonEventId, String session, int limit) throws SQLException {
        validateEventId(seasonEventId);
        validateSession(session);
        if (limit <= 0) {
            throw new IllegalArgumentException("The advancing driver count must be positive.");
        }

        String sql = "SELECT driver_id FROM qualifying_laps "
                + "WHERE season_event_id = ? AND session = ? "
                + "GROUP BY driver_id "
                + "ORDER BY MIN(lap_time_seconds), driver_id LIMIT ?";
        List<Integer> driverIds = new ArrayList<Integer>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.setString(2, session);
            statement.setInt(3, limit);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    driverIds.add(Integer.valueOf(result.getInt("driver_id")));
                }
            }
        }
        return driverIds;
    }

    public void saveFinalResults(
            int seasonEventId,
            List<QualifyingResult> q1Results,
            List<QualifyingResult> q2Results,
            List<QualifyingResult> q3Results) throws SQLException {
        validateEventId(seasonEventId);
        validateFinalResults(q1Results, q2Results, q3Results);

        String sql = "INSERT INTO qualifying_results "
                + "(season_event_id, driver_id, final_position, eliminated_in_session, best_lap_seconds) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteEventResults(connection, seasonEventId);
                insertFinalResults(connection, sql, seasonEventId, q1Results, q2Results, q3Results);
                insertGridPositions(connection, seasonEventId, q1Results, q2Results, q3Results);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void insertGridPositions(
            Connection connection,
            int seasonEventId,
            List<QualifyingResult> q1Results,
            List<QualifyingResult> q2Results,
            List<QualifyingResult> q3Results) throws SQLException {

        String sql = "INSERT INTO race_results "
                + "(season_event_id, driver_id, grid_position) VALUES (?, ?, ?) "
                + "ON CONFLICT(season_event_id, driver_id) "
                + "DO UPDATE SET grid_position = excluded.grid_position";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int position = 1;
            for (QualifyingResult result : q3Results) {
                addGridPosition(statement, seasonEventId, position++, result);
            }
            for (int i = q3Results.size(); i < q2Results.size(); i++) {
                addGridPosition(statement, seasonEventId, position++, q2Results.get(i));
            }
            for (int i = q2Results.size(); i < q1Results.size(); i++) {
                addGridPosition(statement, seasonEventId, position++, q1Results.get(i));
            }
            statement.executeBatch();
        }
    }

    private void addGridPosition(
            PreparedStatement statement,
            int seasonEventId,
            int position,
            QualifyingResult result) throws SQLException {
        statement.setInt(1, seasonEventId);
        statement.setInt(2, result.getDriver().getDriverId());
        statement.setInt(3, position);
        statement.addBatch();
    }

    private void insertFinalResults(
            Connection connection,
            String sql,
            int seasonEventId,
            List<QualifyingResult> q1Results,
            List<QualifyingResult> q2Results,
            List<QualifyingResult> q3Results) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int position = 1;
            for (QualifyingResult result : q3Results) {
                addFinalResult(statement, seasonEventId, position++, result, null);
            }
            for (int i = q3Results.size(); i < q2Results.size(); i++) {
                addFinalResult(statement, seasonEventId, position++, q2Results.get(i), "Q2");
            }
            for (int i = q2Results.size(); i < q1Results.size(); i++) {
                addFinalResult(statement, seasonEventId, position++, q1Results.get(i), "Q1");
            }
            statement.executeBatch();
        }
    }

    private void insertSessionLaps(
            Connection connection,
            int seasonEventId,
            String session,
            List<QualifyingResult> results) throws SQLException {
        String sql = "INSERT INTO qualifying_laps "
                + "(season_event_id, driver_id, session, attempt_number, lap_time_seconds) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (QualifyingResult result : results) {
                for (int lap = 0; lap < result.getLapTimes().size(); lap++) {
                    statement.setInt(1, seasonEventId);
                    statement.setInt(2, result.getDriver().getDriverId());
                    statement.setString(3, session);
                    statement.setInt(4, lap + 1);
                    statement.setDouble(5, roundToMilliseconds(result.getLapTimes().get(lap)));
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private void addFinalResult(
            PreparedStatement statement,
            int seasonEventId,
            int position,
            QualifyingResult result,
            String eliminatedInSession) throws SQLException {
        statement.setInt(1, seasonEventId);
        statement.setInt(2, result.getDriver().getDriverId());
        statement.setInt(3, position);
        if (eliminatedInSession == null) {
            statement.setNull(4, Types.VARCHAR);
        } else {
            statement.setString(4, eliminatedInSession);
        }
        statement.setDouble(5, roundToMilliseconds(result.getLapTimeSeconds()));
        statement.addBatch();
    }

    private void deleteSessionLaps(
            Connection connection, int seasonEventId, String session)
            throws SQLException {
        String sql = "DELETE FROM qualifying_laps WHERE season_event_id = ? AND session = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.setString(2, session);
            statement.executeUpdate();
        }
    }

    private void deleteEventResults(Connection connection, int seasonEventId)
            throws SQLException {
        String sql = "DELETE FROM qualifying_results WHERE season_event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonEventId);
            statement.executeUpdate();
        }
    }

    private void validateFinalResults(
            List<QualifyingResult> q1Results,
            List<QualifyingResult> q2Results,
            List<QualifyingResult> q3Results) {
        if (q1Results == null || q2Results == null || q3Results == null
                || q1Results.isEmpty()
                || q2Results.size() > q1Results.size()
                || q3Results.size() > q2Results.size()) {
            throw new IllegalArgumentException("Qualifying results are incomplete.");
        }
    }

    private void validateEventId(int seasonEventId) {
        if (seasonEventId <= 0) {
            throw new IllegalArgumentException("A valid season event ID is required.");
        }
    }

    private void validateSession(String session) {
        if (!"Q1".equals(session) && !"Q2".equals(session) && !"Q3".equals(session)) {
            throw new IllegalArgumentException("Session must be Q1, Q2, or Q3.");
        }
    }

    private double roundToMilliseconds(double seconds) {
        return Math.round(seconds * 1000.0) / 1000.0;
    }

    public List<QualifyingResult> loadSessionResults(int eventId, String session, List<Driver> drivers) throws SQLException {
        validateEventId(eventId);
        validateSession(session);

        Map<Integer, List<Double>> lapTimes = loadLapTimesByDriver(eventId, session);
        Map<Integer, Driver> driversById = indexDriversById(drivers);
        List<QualifyingResult> results = buildResults(lapTimes, driversById);
        sortByBestLap(results);
        return results;
    }

    private Map<Integer, List<Double>> loadLapTimesByDriver(int eventId, String session) throws SQLException {
        Map<Integer, List<Double>> lapTimes = new HashMap<Integer, List<Double>>();
        String sql = "SELECT driver_id, lap_time_seconds FROM qualifying_laps "
                + "WHERE season_event_id = ? AND session = ? "
                + "ORDER BY driver_id, attempt_number";

        try (Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            statement.setString(2, session);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    addLapTime(lapTimes, result.getInt("driver_id"),
                            result.getDouble("lap_time_seconds"));
                }
            }
        }
        return lapTimes;
    }

    private void addLapTime(Map<Integer, List<Double>> lapTimes, int driverId, double time) {
        Integer id = Integer.valueOf(driverId);
        List<Double> times = lapTimes.get(id);
        if (times == null) {
            times = new ArrayList<Double>();
            lapTimes.put(id, times);
        }
        times.add(Double.valueOf(time));
    }

    private Map<Integer, Driver> indexDriversById(List<Driver> drivers) {
        Map<Integer, Driver> driversById = new HashMap<Integer, Driver>();
        for (Driver driver : drivers) {
            driversById.put(Integer.valueOf(driver.getDriverId()), driver);
        }
        return driversById;
    }

    private List<QualifyingResult> buildResults(
            Map<Integer, List<Double>> lapTimes,
            Map<Integer, Driver> driversById) throws SQLException {

        List<QualifyingResult> results = new ArrayList<QualifyingResult>();

        for (Map.Entry<Integer, List<Double>> entry : lapTimes.entrySet()) {
            Driver driver = driversById.get(entry.getKey());
            if (driver == null) {
                throw new SQLException("No driver found for ID " + entry.getKey() + ".");
            }
            results.add(new QualifyingResult(driver, 0.0, entry.getValue()));
        }
        return results;
    }

    private void sortByBestLap(List<QualifyingResult> results) {
        Collections.sort(results, new Comparator<QualifyingResult>() {
            @Override
            public int compare(QualifyingResult left, QualifyingResult right) {
                return Double.compare(left.getLapTimeSeconds(), right.getLapTimeSeconds());
            }
        });
    }
}
