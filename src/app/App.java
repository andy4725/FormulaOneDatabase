package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.Database;
import model.Circuit;
import model.QualifyingResult;
import model.SimulationData;
import service.QualifyingService;
import service.SimulationDataLoader;

public class App {
    private static final int SAVE_SLOT_ID = 1;
    private static final int ROUND_NUMBER = 1; // Season rounds are 1-based.

    public static void main(String[] args) throws SQLException {
        SimulationData data = new SimulationDataLoader().load();
        SeasonEvent event = findSeasonEvent(SAVE_SLOT_ID, ROUND_NUMBER);
        Circuit circuit = findCircuit(data.getCircuits(), event.circuitId);

        if (circuit == null) {
            throw new IllegalStateException(
                "No circuit found for season round " + ROUND_NUMBER + ".");
        }

        QualifyingService qualifyingService = new QualifyingService();
        String[] sessions = {"Q1", "Q2", "Q3"};

        for (String session : sessions) {
            List<QualifyingResult> results =
                qualifyingService.simulateAndSave(
                    event.seasonEventId,
                    session,
                    circuit,
                    data.getDrivers(),
                    data.getTeams());

            System.out.println(
                circuit.getName() + " " + session +
                " qualifying - benchmark: " +
                circuit.getQualiBenchmarkTime() + " s");

            printResults(results);
            System.out.println();
        }
    }

    private static void printResults(List<QualifyingResult> results) {
        for (int i = 0; i < results.size(); i++) {
            QualifyingResult result = results.get(i);
            double time = result.getLapTimeSeconds();
            int minutes = (int) (time / 60);
            double seconds = time - minutes * 60;

            System.out.printf("%2d. %-24s %d:%06.3f%n",
                i + 1,
                result.getDriver().getName(),
                minutes,
                seconds);
        }
    }

    private static SeasonEvent findSeasonEvent(int slotId, int roundNumber)
            throws SQLException {

        String sql =
            "SELECT se.season_event_id, c.circuit_id " +
            "FROM seasons s " +
            "JOIN season_events se ON se.season_id = s.season_id " +
            "JOIN grand_prix gp ON gp.grand_prix_id = se.grand_prix_id " +
            "JOIN circuits c ON c.circuit_id = gp.circuit_id " +
            "WHERE s.save_slot_id = ? " +
            "AND s.status = 'IN_PROGRESS' " +
            "AND se.round_number = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, slotId);
            statement.setInt(2, roundNumber);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "No active season event found for slot " + slotId +
                        ", round " + roundNumber +
                        ". Start a season in that slot first.");
                }

                return new SeasonEvent(
                    result.getInt("season_event_id"),
                    result.getInt("circuit_id"));
            }
        }
    }

    private static Circuit findCircuit(List<Circuit> circuits, int circuitId) {
        for (Circuit circuit : circuits) {
            if (circuit.getCircuitId() == circuitId) {
                return circuit;
            }
        }
        return null;
    }

    private static final class SeasonEvent {
        private final int seasonEventId;
        private final int circuitId;

        private SeasonEvent(int seasonEventId, int circuitId) {
            this.seasonEventId = seasonEventId;
            this.circuitId = circuitId;
        }
    }
}