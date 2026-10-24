package model;

/** One driver's saved result in one race (a row of race_results plus lap totals). */
public final class RaceResultEntry {
    private final int driverId;
    private final String driverName;
    private final String teamName;
    private final int gridPosition;
    private final int finishingPosition;
    private final String status;
    private final double points;
    private final boolean fastestLap;
    private final int lapCount;
    private final double totalTimeSeconds;
    private final double bestLapSeconds;

    public RaceResultEntry(
            int driverId,
            String driverName,
            String teamName,
            int gridPosition,
            int finishingPosition,
            String status,
            double points,
            boolean fastestLap,
            int lapCount,
            double totalTimeSeconds,
            double bestLapSeconds) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.teamName = teamName;
        this.gridPosition = gridPosition;
        this.finishingPosition = finishingPosition;
        this.status = status;
        this.points = points;
        this.fastestLap = fastestLap;
        this.lapCount = lapCount;
        this.totalTimeSeconds = totalTimeSeconds;
        this.bestLapSeconds = bestLapSeconds;
    }

    public int getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getTeamName() {
        return teamName;
    }

    /** 0 when unknown. */
    public int getGridPosition() {
        return gridPosition;
    }

    /** 0 when the driver has no classified position. */
    public int getFinishingPosition() {
        return finishingPosition;
    }

    /** FINISHED, DNF or DSQ. */
    public String getStatus() {
        return status;
    }

    public double getPoints() {
        return points;
    }

    public boolean hasFastestLap() {
        return fastestLap;
    }

    public int getLapCount() {
        return lapCount;
    }

    /** Sum of all lap times, 0 when there are no laps. */
    public double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    /** 0 when there are no laps. */
    public double getBestLapSeconds() {
        return bestLapSeconds;
    }
}
