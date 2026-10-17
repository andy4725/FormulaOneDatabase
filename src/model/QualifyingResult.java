package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QualifyingResult {
    private final Driver driver;
    private final double performanceRating;
    private final double lapTimeSeconds;
    private final List<Double> lapTimes;

    public QualifyingResult(Driver driver, double performanceRating, double lapTimeSeconds) {
        this(driver, performanceRating, Collections.singletonList(lapTimeSeconds));
    }

    public QualifyingResult(Driver driver, double performanceRating, List<Double> lapTimes) {
        if(driver == null || lapTimes == null || lapTimes.isEmpty()) {
            throw new IllegalArgumentException("A driver and at least one lap time are required");
        }

        this.driver = driver;
        this.performanceRating = performanceRating;
        this.lapTimes = Collections.unmodifiableList(new ArrayList<Double>(lapTimes));

        double bestTime = Double.MAX_VALUE;
        for(Double lapTime: lapTimes) {
            if(lapTime == null || lapTime <= 0) {
                throw new IllegalArgumentException("Lap times must be positive.");
            }
            bestTime = Math.min(bestTime, lapTime);
        }
        this.lapTimeSeconds = bestTime;
    }

    public Driver getDriver() {
        return driver;
    }

    public double getPerformanceRating() {
        return performanceRating;
    }

    public double getLapTimeSeconds() {
        return lapTimeSeconds;
    }

    public List<Double> getLapTimes() {
        return lapTimes;
        }
}
