package service;

import model.Circuit;
import model.Driver;
import model.QualifyingResult;
import model.Team;
import repository.QualifyingRepository;
import repository.QualifyingStore;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Connects qualifying rules, the simulator, and the repository.
 * Each call generates attempts for one session and saves those laps for later phases.
 */
public class QualifyingService {
    private static final Random RANDOM = new Random();
    private final QualifyingSimulator simulator;
    private final QualifyingStore repository;

    public QualifyingService() {
        this(new QualifyingSimulator(), new QualifyingRepository());
    }

    public QualifyingService(QualifyingSimulator simulator) {
        this(simulator, new QualifyingRepository());
    }

    public QualifyingService(
            QualifyingSimulator simulator, QualifyingStore repository) {
        this.simulator = simulator;
        this.repository = repository;
    }

    public List<QualifyingResult> simulateAndSave(
        int seasonEventId,
        String session,
        Circuit circuit,
        List<Driver> drivers,
        List<Team> teams) throws SQLException {

        // Reject invalid session requests before generating or persisting any laps.
        validateRequest(seasonEventId, session);

        // Each entrant receives an independent attempt count for this session.
        Map<Integer, Integer> lapCountsByDriver = new HashMap<Integer, Integer>();

        for (Driver driver : drivers) {
            lapCountsByDriver.put(driver.getDriverId(), chooseLapCount(session));
        }

        List<QualifyingResult> results = simulator.simulateSession(circuit, drivers, teams, lapCountsByDriver);

        repository.saveSessionLaps(seasonEventId, session, results);
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

    public void saveFinalResults(
        int seasonEventId,
        List<QualifyingResult> q1Results,
        List<QualifyingResult> q2Results,
        List<QualifyingResult> q3Results) throws SQLException {

        repository.saveFinalResults(seasonEventId, q1Results, q2Results, q3Results);
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
