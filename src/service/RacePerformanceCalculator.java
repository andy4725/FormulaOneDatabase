package service;

import model.Circuit;
import model.Driver;
import model.DriverRacePerformance;
import model.Team;

/** Derives a driver's baseline pace, lap consistency, and passing strength.
 * The race simulator uses these values to generate laps and attempt overtakes.
 */
public class RacePerformanceCalculator {
    private static final double TEAM_PACE_WEIGHT = 0.35;
    private static final double DRIVER_PACE_WEIGHT = 0.30;
    private static final double RACECRAFT_WEIGHT = 0.20;
    private static final double EXPERIENCE_WEIGHT = 0.15;

    private static final double RACECRAFT_OVERTAKING_WEIGHT = 0.80;
    private static final double AWARENESS_OVERTAKING_WEIGHT = 0.20;

    private static final double BENCHMARK_RATING = 80.0;
    private static final double TIME_FACTOR_PER_RATING_POINT = 0.001;
    private static final double BASE_LAP_TIME_VARIATION = 0.003;

    public DriverRacePerformance calculate(
            Circuit circuit,
            Driver driver,
            Team team) {

        if (circuit == null || driver == null || team == null) {
            throw new IllegalArgumentException(
                    "Circuit, driver, and team are required.");
        }
        if (circuit.getRaceBenchmarkTime() <= 0) {
            throw new IllegalArgumentException(
                    "Circuit race benchmark time is required.");
        }

        // Combine team and driver attributes into one rating relative to the benchmark.
        double paceRating =
                TEAM_PACE_WEIGHT * clampRating(team.getPace())
                + DRIVER_PACE_WEIGHT * clampRating(driver.getPace())
                + RACECRAFT_WEIGHT * clampRating(driver.getRacecraft())
                + EXPERIENCE_WEIGHT * clampRating(driver.getExperience());

        double baseLapTime =
                circuit.getRaceBenchmarkTime()
                * (1.0 + (BENCHMARK_RATING - paceRating)
                        * TIME_FACTOR_PER_RATING_POINT);

        // Reliability and awareness reduce the amount of lap-to-lap time variation.
        double consistency =
                (clampRating(team.getReliability())
                + clampRating(driver.getAwareness())) / 2.0;

        double lapTimeVariation =
                BASE_LAP_TIME_VARIATION * (1.5 - consistency / 100.0);

        double overtakingRating =
                RACECRAFT_OVERTAKING_WEIGHT
                        * clampRating(driver.getRacecraft())
                + AWARENESS_OVERTAKING_WEIGHT
                        * clampRating(driver.getAwareness());

        return new DriverRacePerformance(
                baseLapTime,
                lapTimeVariation,
                overtakingRating);
    }

    private int clampRating(int rating) {
        return Math.max(0, Math.min(100, rating));
    }
}
