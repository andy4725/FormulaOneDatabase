package service;

import database.Database;
import model.Circuit;
import model.Driver;
import model.QualifyingResult;
import model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QualifyingService {
    private static final Random RANDOM = new Random();
    private final QualifyingSimulator simulator;

    public QualifyingService() {
        this(new QualifyingSimulator());
    }

    public QualifyingService(QualifyingSimulator simulator) {
        this.simulator = simulator;
    }

    public List<QualifyingResult> simulateAndSave(
        int seasonEventId,
        String session,
        Circuit circuit,
        List<Driver> drivers,
        List<Team> teams) throws SQLException {

        validateRequest(seasonEventId, session);

        Map<Integer, Integer> lapCountsByDriver = new HashMap<Integer, Integer>();

        for (Driver driver : drivers) {
            lapCountsByDriver.put(driver.getDriverId(), chooseLapCount(session));
        }

        List<QualifyingResult> results = simulator.simulateWithLapCounts(circuit, drivers, teams, lapCountsByDriver);

        saveSession(seasonEventId, session, results);
        return results;
    }

    private void validateRequest(int seasonEventId, String session) {
        if (seasonEventId <= 0) {
            throw new IllegalArgumentException("A valid season event ID is required.");
        }
        if (!"Q1".equals(session) && !"Q2".equals(session) && !"Q3".equals(session)) {
            throw new IllegalArgumentException("Session must be Q1, Q2, or Q3.");
        }
    }

    private void saveSession(
            int seasonEventId,
            String session,
            List<QualifyingResult> results) throws SQLException {

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                deleteSessionLaps(connection, seasonEventId, session);
                deleteEventResults(connection, seasonEventId);
                insertSessionLaps(connection, seasonEventId, session, results);
                insertResults(connection, seasonEventId, results);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void deleteSessionLaps(
            Connection connection, int seasonEventId, String session) throws SQLException {

        String sql =
            "DELETE FROM qualifying_laps WHERE season_event_id = ? AND session = ?";

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

    private void insertSessionLaps(
            Connection connection,
            int seasonEventId,
            String session,
            List<QualifyingResult> results) throws SQLException {

        String sql =
            "INSERT INTO qualifying_laps " +
            "(season_event_id, driver_id, session, attempt_number, lap_time_seconds) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (QualifyingResult result : results) {
                for (int lap = 0; lap < result.getLapTimes().size(); lap++) {
                    statement.setInt(1, seasonEventId);
                    statement.setInt(2, result.getDriver().getDriverId());
                    statement.setString(3, session);
                    statement.setInt(4, lap + 1);
                    statement.setDouble(5, result.getLapTimes().get(lap));
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private void insertResults(
            Connection connection,
            int seasonEventId,
            List<QualifyingResult> results) throws SQLException {

        String sql =
            "INSERT INTO qualifying_results " +
            "(season_event_id, driver_id, final_position, best_lap_seconds) " +
            "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int position = 0; position < results.size(); position++) {
                QualifyingResult result = results.get(position);
                statement.setInt(1, seasonEventId);
                statement.setInt(2, result.getDriver().getDriverId());
                statement.setInt(3, position + 1);
                statement.setDouble(4, result.getLapTimeSeconds());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private int chooseLapCount(String session) {
        double roll = RANDOM.nextDouble();

        if ("Q3".equals(session)) {
            return roll < 0.30 ? 1 : 2;
        }

        // Q1/Q2: 25% do 2 laps, 50% do 3, and 25% do 4.
        if (roll < 0.25) {
            return 2;
        }
        if (roll < 0.75) {
            return 3;
        }
        return 4;
    }
}
