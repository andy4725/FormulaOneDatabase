package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.Circuit;
import model.Driver;
import model.Team;
import model.QualifyingResult;

/** Calculates each driver's qualifying attempts and orders the field by best lap.
 * Ratings shift the circuit baseline, with small random differences between attempts.
 */
public class QualifyingSimulator {
    private static final double TEAM_PACE_WEIGHT = 0.35;
    private static final double DRIVER_PACE_WEIGHT = 0.35;
    private static final double DRIVER_EXPERIENCE_WEIGHT = 0.20;
    private static final double TOTAL_WEIGHT = TEAM_PACE_WEIGHT + DRIVER_PACE_WEIGHT + DRIVER_EXPERIENCE_WEIGHT;

    private static final double BENCHMARK_RATING = 80.0;
    private static final double TIME_FACTOR_PER_RATING_POINT = 0.001;
    private static final double RANDOM_VARIATION = 0.10;

    private static final double LAP_VARIATION = 0.002;

    private final Random random;

    public QualifyingSimulator() {
        this(new Random());
    }

    public QualifyingSimulator(long seed) {
        this(new Random(seed));
    }

    private QualifyingSimulator(Random random) {
        this.random = random;
    }

    public List<QualifyingResult> simulateSession(
            Circuit circuit,
            List<Driver> drivers,
            List<Team> teams,
            Map<Integer, Integer> lapCountsByDriver) {

        if (lapCountsByDriver == null) {
            throw new IllegalArgumentException("Lap counts are required.");
        }

        validateInputs(circuit, drivers, teams);
        // Resolve each driver's team once so per-driver simulation uses a direct lookup.
        Map<Integer, Team> teamsById = indexTeamsById(teams);
        List<QualifyingResult> results = new ArrayList<QualifyingResult>();

        for (Driver driver : drivers) {
            Integer lapCount = lapCountsByDriver.get(driver.getDriverId());

            if (lapCount == null || lapCount <= 0) {
                throw new IllegalArgumentException(
                    "A positive lap count is required for " + driver.getName() + ".");
            }

            results.add(simulateDriver(circuit, driver, teamsById, lapCount));
        }

        sortByFastestLap(results);
        return results;
    }

    private void validateInputs(Circuit circuit, List<Driver> drivers, List<Team> teams) {
        if(circuit == null || drivers == null || teams == null) {
            throw new IllegalArgumentException("Circuits, drivers and teams are required.");
        }
        if(circuit.getQualiBenchmarkTime() <= 0) {
            throw new IllegalArgumentException("Circuit quali benchmark time is required.");
        }
    }

    private Map<Integer, Team> indexTeamsById(List<Team> teams) {
        Map<Integer, Team> teamsById = new HashMap<Integer, Team>();
        for(Team team: teams) {
            teamsById.put(team.getTeamId(), team);
        }

        return teamsById;
    }

    private QualifyingResult simulateDriver(Circuit circuit, Driver driver, Map<Integer, Team> teamsById, int lapCount) {
        Team team = teamsById.get(driver.getTeamId());
        if (team == null) {
            throw new IllegalArgumentException("No team found for " + driver.getName() + ".");
        }

        double performanceRating = calculatePerformanceRating(driver, team);
        List<Double> lapTimes = simulateLapTimes(circuit, performanceRating, lapCount);

        return new QualifyingResult(driver, performanceRating, lapTimes);
    }

    private double calculatePerformanceRating(Driver driver, Team team) {
        // Normalize the weighted inputs so the rating stays on the same scale as attributes.
        double rating = (
                TEAM_PACE_WEIGHT * team.getPace() +
                DRIVER_PACE_WEIGHT * driver.getPace() +
                DRIVER_EXPERIENCE_WEIGHT * driver.getExperience()
        ) / TOTAL_WEIGHT;

        // Apply ±10% randomness to performance rating, not lap time.
        double randomMultiplier = 1.0 + (random.nextDouble() * 2.0 - 1.0) * RANDOM_VARIATION;
        double effectiveRating = rating * randomMultiplier;

        return effectiveRating;
    }

    private List<Double> simulateLapTimes(Circuit circuit, double performanceRating, int lapCount) {

        List<Double> lapTimes = new ArrayList<Double>();
        for (int lap = 0; lap < lapCount; lap++) {
            lapTimes.add(calculateLapTime(circuit, performanceRating));
        }
        return lapTimes;
    }

    private double calculateLapTime(Circuit circuit, double performanceRating) {
        double baseLapTime = circuit.getQualiBenchmarkTime()
            * (1.0 + (BENCHMARK_RATING - performanceRating)
            * TIME_FACTOR_PER_RATING_POINT);
        
        // Apply a small independent variation to each lap.
        double lapMultiplier = 1.0 + (random.nextDouble() * 2.0 - 1.0) * LAP_VARIATION;
        double finalLapTime = baseLapTime * lapMultiplier;

        return finalLapTime;
    }

    private void sortByFastestLap(List<QualifyingResult> results) {
        Collections.sort(results, new Comparator<QualifyingResult>() {
            @Override
            public int compare(QualifyingResult left, QualifyingResult right) {
                return Double.compare(left.getLapTimeSeconds(), right.getLapTimeSeconds());
            }
        });
    }
}
