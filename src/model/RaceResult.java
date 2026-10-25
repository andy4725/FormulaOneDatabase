package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One driver's race outcome, including starting/finishing places and lap history.
 * Lap times are copied and exposed read-only so the saved result stays consistent.
 */
public class RaceResult {
    private final Driver driver;
    private final int gridPosition;
    private final int finishingPosition;
    private final List<Double> lapTimes;

    public RaceResult(
            Driver driver,
            int gridPosition,
            int finishingPosition,
            List<Double> lapTimes) {

        if (driver == null) {
            throw new IllegalArgumentException("A driver is required.");
        }
        if (gridPosition <= 0 || finishingPosition <= 0) {
            throw new IllegalArgumentException("Grid and finishing positions must be positive.");
        }
        if (lapTimes == null || lapTimes.isEmpty()) {
            throw new IllegalArgumentException("At least one lap time is required.");
        }

        for (Double lapTime : lapTimes) {
            if (lapTime == null || lapTime <= 0) {
                throw new IllegalArgumentException("Lap times must be positive.");
            }
        }

        this.driver = driver;
        this.gridPosition = gridPosition;
        this.finishingPosition = finishingPosition;
        // Keep a defensive copy because these laps are used for totals and fastest lap.
        this.lapTimes = Collections.unmodifiableList(new ArrayList<Double>(lapTimes));
    }

    public Driver getDriver() {
        return driver;
    }

    public int getGridPosition() {
        return gridPosition;
    }

    public int getFinishingPosition() {
        return finishingPosition;
    }

    public List<Double> getLapTimes() {
        return lapTimes;
    }
}
