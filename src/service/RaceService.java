package service;

import java.sql.SQLException;
import java.util.List;

import model.Circuit;
import model.Driver;
import model.RaceResult;
import model.Team;
import repository.RaceRepository;

public class RaceService {
    private final RaceSimulator simulator;
    private final RaceRepository repository;

    public RaceService() {
        this(new RaceSimulator(), new RaceRepository());
    }

    public RaceService(RaceSimulator simulator, RaceRepository repository) {
        if (simulator == null || repository == null) {
            throw new IllegalArgumentException("A simulator and repository are required.");
        }
        this.simulator = simulator;
        this.repository = repository;
    }

    public List<RaceResult> simulateAndSave(
            int seasonEventId,
            Circuit circuit,
            List<Driver> drivers,
            List<Team> teams) throws SQLException {

        if (seasonEventId <= 0) {
            throw new IllegalArgumentException("A valid season event ID is required.");
        }

        List<RaceResult> results = simulator.simulateRace(seasonEventId, circuit, drivers, teams);

        repository.saveResults(seasonEventId, results);
        return results;
    }
}