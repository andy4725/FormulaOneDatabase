package model;

/**
 * One Grand Prix weekend of the current season (a row of season_events).
 * The event ID links session and race data; the round and circuit identify what is next.
 */
public final class WeekendEvent {
    private final int seasonEventId;
    private final int roundNumber;
    private final String grandPrixName;
    private final int circuitId;

    public WeekendEvent(int seasonEventId, int roundNumber,
            String grandPrixName, int circuitId) {
        this.seasonEventId = seasonEventId;
        this.roundNumber = roundNumber;
        this.grandPrixName = grandPrixName;
        this.circuitId = circuitId;
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

    public int getCircuitId() {
        return circuitId;
    }
}
