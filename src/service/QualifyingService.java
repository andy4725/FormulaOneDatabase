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

        List<QualifyingResult> results = simulator.simulateSession(circuit, drivers, teams, lapCountsByDriver);

        saveSessionLaps(seasonEventId, session, results);
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

    private void saveSessionLaps(
        int seasonEventId,
        String session,
        List<QualifyingResult> results) throws SQLException {

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

    public void saveFinalResults(
        int seasonEventId,
        List<QualifyingResult> q1Results,
        List<QualifyingResult> q2Results,
        List<QualifyingResult> q3Results) throws SQLException {

        if (q1Results == null || q2Results == null || q3Results == null
                || q2Results.size() > q1Results.size()
                || q3Results.size() > q2Results.size()) {
            throw new IllegalArgumentException("Qualifying results are incomplete.");
        }

        String sql =
            "INSERT INTO qualifying_results " +
            "(season_event_id, driver_id, final_position, eliminated_in_session, best_lap_seconds) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                deleteEventResults(connection, seasonEventId);

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int position = 1;

                    // Q3 participants: positions 1–10, with no elimination session.
                    for (QualifyingResult result : q3Results) {
                        addFinalResult(statement, seasonEventId, position++, result, null);
                    }

                    // Drivers eliminated in Q2: next positions, ordered by their Q2 times.
                    for (int i = q3Results.size(); i < q2Results.size(); i++) {
                        addFinalResult(
                            statement, seasonEventId, position++, q2Results.get(i), "Q2");
                    }

                    // Drivers eliminated in Q1: remaining positions, ordered by their Q1 times.
                    for (int i = q2Results.size(); i < q1Results.size(); i++) {
                        addFinalResult(
                            statement, seasonEventId, position++, q1Results.get(i), "Q1");
                    }

                    statement.executeBatch();
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
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
            statement.setNull(4, java.sql.Types.VARCHAR);
        } else {
            statement.setString(4, eliminatedInSession);
        }

        statement.setDouble(5, roundToMilliseconds(result.getLapTimeSeconds()));
        statement.addBatch();
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

    private double roundToMilliseconds(double seconds) {
        return Math.round(seconds * 1000.0) / 1000.0;
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
                    statement.setDouble(5, roundToMilliseconds(result.getLapTimes().get(lap)));
                    statement.addBatch();
                }
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
