package model;

public class QualifyingResult {
    private final Driver driver;
    private final double performanceRating;
    private final double lapTimeSeconds;

    public QualifyingResult(Driver driver, double performanceRating, double lapTimeSeconds) {
        this.driver = driver;
        this.performanceRating = performanceRating;
        this.lapTimeSeconds = lapTimeSeconds;
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
}
