package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import model.Circuit;
import model.Driver;
import model.RaceResult;
import model.Team;
import repository.RaceRepository;
import repository.RaceResultsStore;

/** Orchestrates the race workflow from qualifying grid through saved results.
 * Simulation, points calculation, and persistence remain separate collaborators.
 */
public class RaceService {
    private final RaceSimulator simulator;
    private final RaceResultsStore repository;
    private final RacePointsCalculator pointsCalculator;

    public RaceService() {
        this(new RaceSimulator(), new RaceRepository(),
                new RacePointsCalculator());
    }

    public RaceService(
            RaceSimulator simulator,
            RaceResultsStore repository) {
        this(simulator, repository, new RacePointsCalculator());
    }

    public RaceService(
            RaceSimulator simulator,
            RaceResultsStore repository,
            RacePointsCalculator pointsCalculator) {
        if (simulator == null || repository == null
                || pointsCalculator == null) {
            throw new IllegalArgumentException(
                    "A simulator, repository, and points calculator are required.");
        }

        this.simulator = simulator;
        this.repository = repository;
        this.pointsCalculator = pointsCalculator;
    }

    public List<RaceResult> simulateAndSave(
            int seasonEventId,
            Circuit circuit,
            List<Driver> drivers,
            List<Team> teams) throws SQLException {

        // Validate before querying the grid so invalid event IDs fail at the boundary.
        if (seasonEventId <= 0) {
            throw new IllegalArgumentException(
                    "A valid season event ID is required.");
        }

        // The race cannot start until qualifying has supplied a starting grid.
        Map<Integer, Integer> gridPositions =
                repository.findGridPositions(seasonEventId);

        if (gridPositions.isEmpty()) {
            throw new IllegalStateException(
                    "A qualifying grid is required before the race can be simulated.");
        }

        List<RaceResult> results = simulator.simulateRace(
                circuit, drivers, teams, gridPositions);

        Map<Integer, Integer> pointsByDriverId =
                pointsCalculator.calculatePoints(results);

        repository.saveResults(
                seasonEventId, results, pointsByDriverId);

        return results;
    }
}
