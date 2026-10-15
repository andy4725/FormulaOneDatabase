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

public class QualifyingSimulator {
    private static final double TEAM_PACE_WEIGHT = 0.35;
    private static final double DRIVER_PACE_WEIGHT = 0.35;
    private static final double DRIVER_EXPERIENCE_WEIGHT = 0.20;
    private static final double TOTAL_WEIGHT = TEAM_PACE_WEIGHT + DRIVER_PACE_WEIGHT + DRIVER_EXPERIENCE_WEIGHT;

    private static final double BENCHMARK_RATING = 80.0;
    private static final double TIME_FACTOR_PER_RATING_POINT = 0.001;
    private static final double RANDOM_VARIATION = 0.10;

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

    public List<QualifyingResult> simulate(Circuit circuit, List<Driver> drivers, List<Team> teams) {
        if(circuit == null || drivers == null || teams == null) {
            throw new IllegalArgumentException("Circuits, drivers and teams are required.");
        }
        if(circuit.getQualiBenchmarkTime() <= 0) {
            throw new IllegalArgumentException("Circuit quali benchmark time is required.");
        }

        Map<Integer, Team> teamsById = new HashMap<Integer, Team>();
        for(Team team: teams) {
            teamsById.put(team.getTeamId(), team);
        }

        List<QualifyingResult> results = new ArrayList<QualifyingResult>();

        for(Driver driver: drivers) {
            Team team = teamsById.get(driver.getTeamId());
            if(team == null) {
                throw new IllegalArgumentException("No team found for " + driver.getName() + ".");
            }

            double rating = (
                TEAM_PACE_WEIGHT * team.getPace() +
                DRIVER_PACE_WEIGHT * driver.getPace() +
                DRIVER_EXPERIENCE_WEIGHT * driver.getExperience()
            ) / TOTAL_WEIGHT;

            // Apply ±10% randomness to performance rating, not lap time.
            double randomMultiplier = 1.0 + (random.nextDouble() * 2.0 - 1.0) * RANDOM_VARIATION;
            double effectiveRating = rating * randomMultiplier;

            // 80 is the neutral rating: it produces the circuit benchmark.
            double lapTime = circuit.getQualiBenchmarkTime()
                * (1.0 + (BENCHMARK_RATING - effectiveRating)
                * TIME_FACTOR_PER_RATING_POINT);

            results.add(new QualifyingResult(driver, effectiveRating, lapTime));
        }

        Collections.sort(results, new Comparator<QualifyingResult>() {
            @Override
            public int compare(QualifyingResult left, QualifyingResult right) {
                return Double.compare(left.getLapTimeSeconds(), right.getLapTimeSeconds());
            }
        });

        return results;
    }
}
