package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import model.Circuit;
import model.Driver;
import model.DriverRacePerformance;
import model.RaceResult;
import model.Team;

public class RaceSimulator {
    private final Random random;
    private final RacePerformanceCalculator performanceCalculator;

    public RaceSimulator() {
        this(new Random(), new RacePerformanceCalculator());
    }

    public RaceSimulator(long seed) {
        this(new Random(seed), new RacePerformanceCalculator());
    }

    public RaceSimulator(RacePerformanceCalculator performanceCalculator, long seed) {
        this(new Random(seed), performanceCalculator);
    }

    private RaceSimulator(Random random, RacePerformanceCalculator performanceCalculator) {
        if (random == null || performanceCalculator == null) {
            throw new IllegalArgumentException("Random generator and performance calculator are required.");
        }

        this.random = random;
        this.performanceCalculator = performanceCalculator;
    }

    public List<RaceResult> simulateRace(
            Circuit circuit,
            List<Driver> drivers,
            List<Team> teams,
            Map<Integer, Integer> gridPositionsByDriver) {

        validateInputs(circuit, drivers, teams, gridPositionsByDriver);

        Map<Integer, Team> teamsById = indexTeamsById(teams);
        List<Racer> runningOrder = new ArrayList<Racer>();
        Set<Integer> usedGridPositions = new HashSet<Integer>();

        for (Driver driver : drivers) {
            if (driver == null) {
                throw new IllegalArgumentException("The driver list cannot contain null values.");
            }

            Integer gridPosition = gridPositionsByDriver.get(driver.getDriverId());

            if (gridPosition == null || gridPosition <= 0) {
                throw new IllegalArgumentException("A positive grid position is required for " + driver.getName() + ".");
            }
            if (!usedGridPositions.add(gridPosition)) {
                throw new IllegalArgumentException("Grid positions must be unique.");
            }

            Team team = teamsById.get(driver.getTeamId());
            if (team == null) {
                throw new IllegalArgumentException("No team found for " + driver.getName() + ".");
            }

            DriverRacePerformance performance = performanceCalculator.calculate(circuit, driver, team);

            runningOrder.add(new Racer(driver, gridPosition, performance));
        }

        sortByGridPosition(runningOrder);

        for (int lap = 0; lap < circuit.getRaceLaps(); lap++) {
            simulateLap(runningOrder);
        }

        List<RaceResult> results = new ArrayList<RaceResult>();
        for (int i = 0; i < runningOrder.size(); i++) {
            Racer racer = runningOrder.get(i);
            results.add(new RaceResult(
                    racer.driver,
                    racer.gridPosition,
                    i + 1,
                    racer.lapTimes));
        }

        return results;
    }

    private void validateInputs(
            Circuit circuit,
            List<Driver> drivers,
            List<Team> teams,
            Map<Integer, Integer> gridPositionsByDriver) {

        if (circuit == null || drivers == null || teams == null
                || gridPositionsByDriver == null) {
            throw new IllegalArgumentException("Circuit, drivers, teams, and grid positions are required.");
        }
        if (drivers.isEmpty()) {
            throw new IllegalArgumentException("At least one driver is required.");
        }
        if (circuit.getRaceLaps() <= 0
                || circuit.getRaceBenchmarkTime() <= 0) {
            throw new IllegalArgumentException("Circuit race laps and benchmark time must be positive.");
        }
    }

    private Map<Integer, Team> indexTeamsById(List<Team> teams) {
        Map<Integer, Team> teamsById = new HashMap<Integer, Team>();

        for (Team team : teams) {
            if (team == null) {
                throw new IllegalArgumentException("The team list cannot contain null values.");
            }
            teamsById.put(team.getTeamId(), team);
        }

        return teamsById;
    }

    private void sortByGridPosition(List<Racer> runningOrder) {
        Collections.sort(runningOrder, new Comparator<Racer>() {
            @Override
            public int compare(Racer left, Racer right) {
                return Integer.compare(left.gridPosition, right.gridPosition);
            }
        });
    }

    private void simulateLap(List<Racer> runningOrder) {
        Map<Racer, Double> lapTimes = new HashMap<Racer, Double>();

        for (Racer racer : runningOrder) {
            double lapTime = simulateLapTime(racer.performance);
            racer.lapTimes.add(lapTime);
            lapTimes.put(racer, lapTime);
        }

        // Check from the front toward the back. A driver can pass the car
        // directly ahead if they are faster on this lap and succeed.
        for (int i = 1; i < runningOrder.size(); i++) {
            Racer behind = runningOrder.get(i);
            Racer ahead = runningOrder.get(i - 1);

            double behindLapTime = lapTimes.get(behind);
            double aheadLapTime = lapTimes.get(ahead);

            if (behindLapTime < aheadLapTime
                    && canOvertake(behind.performance, aheadLapTime - behindLapTime)) {
                runningOrder.set(i - 1, behind);
                runningOrder.set(i, ahead);
            }
        }
    }

    private double simulateLapTime(DriverRacePerformance performance) {
        double variation = performance.getLapTimeVariation();
        double multiplier = 1.0 + (random.nextDouble() * 2.0 - 1.0) * variation;

        return performance.getBaseLapTimeSeconds() * multiplier;
    }

    private boolean canOvertake(
            DriverRacePerformance behindPerformance,
            double lapTimeAdvantage) {

        double probability =
                0.20
                + behindPerformance.getOvertakingRating() * 0.004
                + Math.min(0.20, lapTimeAdvantage * 0.15);

        probability = Math.min(0.90, probability);
        return random.nextDouble() < probability;
    }

    private static class Racer {
        private final Driver driver;
        private final int gridPosition;
        private final DriverRacePerformance performance;
        private final List<Double> lapTimes = new ArrayList<Double>();

        private Racer(
                Driver driver,
                int gridPosition,
                DriverRacePerformance performance) {
            this.driver = driver;
            this.gridPosition = gridPosition;
            this.performance = performance;
        }
    }
}