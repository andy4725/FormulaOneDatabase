package service;

import java.sql.SQLException;
import java.util.List;

import model.Circuit;
import model.Driver;
import model.QualifyingResult;
import model.QualifyingSessionOutcome;
import model.Team;
import repository.QualifyingRepository;

public class NextQualifyingSessionService {
    private final QualifyingRepository repository;
    private final QualifyingService qualifyingService;

    public NextQualifyingSessionService() {
        this(new QualifyingRepository(), new QualifyingSimulator());
    }

    public NextQualifyingSessionService(
            QualifyingRepository repository, QualifyingSimulator simulator) {
        this.repository = repository;
        this.qualifyingService = new QualifyingService(simulator, repository);
    }

    public QualifyingSessionOutcome simulateNextSession(
            int eventId, Circuit circuit, List<Driver> allDrivers, List<Team> teams)
            throws SQLException {
        String session = determineNextQualifyingSession(eventId);
        List<Driver> drivers = selectDriversForSession(session, eventId, allDrivers);

        List<QualifyingResult> results = qualifyingService.simulateAndSave(
                eventId, session, circuit, drivers, teams);

        boolean complete = "Q3".equals(session);
        if (complete) {
            saveFinalClassification(eventId, allDrivers, results);
        }

        return new QualifyingSessionOutcome(session, results, complete);
    }

    public boolean isQualifyingComplete(int eventId, int expectedDriverCount)
            throws SQLException {
        return repository.finalResultsCount(eventId) == expectedDriverCount;
    }

    private String determineNextQualifyingSession(int eventId) throws SQLException {
        boolean q1Complete = repository.sessionHasLaps(eventId, "Q1");
        boolean q2Complete = repository.sessionHasLaps(eventId, "Q2");
        boolean q3Complete = repository.sessionHasLaps(eventId, "Q3");

        if ((q2Complete && !q1Complete) || (q3Complete && !q2Complete)) {
            throw new IllegalStateException("Qualifying session data is out of order.");
        }
        if (!q1Complete) return "Q1";
        if (!q2Complete) return "Q2";
        if (!q3Complete) return "Q3";

        throw new IllegalStateException("Qualifying is already complete.");
    }

    private List<Driver> selectDriversForSession(
            String session, int eventId, List<Driver> allDrivers) throws SQLException {
        if ("Q1".equals(session)) return allDrivers;

        String previousSession = "Q2".equals(session) ? "Q1" : "Q2";
        int count = "Q2".equals(session) ? 16 : 10;
        return advancingDrivers(eventId, previousSession, count, allDrivers);
    }

    private List<Driver> advancingDrivers(
            int eventId, String previousSession, int count, List<Driver> allDrivers)
            throws SQLException {
        return repository.findAdvancingDrivers(
                eventId, previousSession, count, allDrivers);
    }

    private void saveFinalClassification(
            int eventId, List<Driver> allDrivers, List<QualifyingResult> q3Results)
            throws SQLException {
        List<QualifyingResult> q1Results =
                repository.loadSessionResults(eventId, "Q1", allDrivers);
        List<QualifyingResult> q2Results =
                repository.loadSessionResults(eventId, "Q2", allDrivers);

        repository.saveFinalResults(eventId, q1Results, q2Results, q3Results);
    }
}
