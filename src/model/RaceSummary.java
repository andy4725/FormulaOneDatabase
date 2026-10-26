package model;

/** Compact information about one scheduled or completed season race.
 * The winner is absent until the race has a classified first-place result.
 */
public final class RaceSummary {
    private final int seasonEventId;
    private final int roundNumber;
    private final String grandPrixName;
    private final String circuitName;
    private final String country;
    private final String status;
    private final String winnerName;

    public RaceSummary(
            int seasonEventId,
            int roundNumber,
            String grandPrixName,
            String circuitName,
            String country,
            String status,
            String winnerName) {
        this.seasonEventId = seasonEventId;
        this.roundNumber = roundNumber;
        this.grandPrixName = grandPrixName;
        this.circuitName = circuitName;
        this.country = country;
        this.status = status;
        this.winnerName = winnerName;
    }

    public int getSeasonEventId() {
        return seasonEventId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getGrandPrixName() {
        return grandPrixName;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public String getCountry() {
        return country;
    }

    public String getStatus() {
        return status;
    }

    /** Null while the race has not been run yet. */
    public String getWinnerName() {
        return winnerName;
    }

    public boolean isComplete() {
        return "RACE_COMPLETE".equals(status);
    }
}
