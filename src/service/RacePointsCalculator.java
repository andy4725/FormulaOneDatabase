package service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.RaceResult;

/** Converts finishing places into points for each driver.
 * Positions outside the scoring places receive zero points.
 */
public class RacePointsCalculator {
    private static final int[] POINTS_BY_POSITION = {
        25, 18, 15, 12, 10, 8, 6, 4, 2, 1
    };

    public Map<Integer, Integer> calculatePoints(List<RaceResult> results) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("At least one race result is required.");
        }

        Map<Integer, Integer> pointsByDriverId =
                new HashMap<Integer, Integer>();
        Set<Integer> driverIds = new HashSet<Integer>();
        Set<Integer> finishingPositions = new HashSet<Integer>();

        for (RaceResult result : results) {
            if (result == null || result.getDriver() == null) {
                throw new IllegalArgumentException("Each result must include a driver.");
            }

            int driverId = result.getDriver().getDriverId();
            int finishingPosition = result.getFinishingPosition();

            if (!driverIds.add(driverId)) {
                throw new IllegalArgumentException("A driver can only appear once in race results.");
            }
            if (finishingPosition <= 0 || !finishingPositions.add(finishingPosition)) {
                throw new IllegalArgumentException("Finishing positions must be positive and unique.");
            }

            int points = finishingPosition <= POINTS_BY_POSITION.length
                    ? POINTS_BY_POSITION[finishingPosition - 1]
                    : 0;

            pointsByDriverId.put(driverId, points);
        }

        return pointsByDriverId;
    }
}
