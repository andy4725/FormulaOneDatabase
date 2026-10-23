package model;

public final class DriverRacePerformance {
    private final double baseLapTimeSeconds;
    private final double lapTimeVariation;
    private final double overtakingRating;

    public DriverRacePerformance(
            double baseLapTimeSeconds,
            double lapTimeVariation,
            double overtakingRating) {
        if (baseLapTimeSeconds <= 0 || lapTimeVariation < 0) {
            throw new IllegalArgumentException(
                    "Base lap time must be positive and variation cannot be negative.");
        }
        if (overtakingRating < 0 || overtakingRating > 100) {
            throw new IllegalArgumentException(
                    "Overtaking rating must be between 0 and 100.");
        }

        this.baseLapTimeSeconds = baseLapTimeSeconds;
        this.lapTimeVariation = lapTimeVariation;
        this.overtakingRating = overtakingRating;
    }

    public double getBaseLapTimeSeconds() {
        return baseLapTimeSeconds;
    }

    public double getLapTimeVariation() {
        return lapTimeVariation;
    }

    public double getOvertakingRating() {
        return overtakingRating;
    }
}