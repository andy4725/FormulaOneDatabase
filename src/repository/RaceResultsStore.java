package repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import model.RaceResult;

/** Persistence operations required by the race workflow. */
public interface RaceResultsStore {
    Map<Integer, Integer> findGridPositions(int seasonEventId) throws SQLException;

    void saveResults(
            int seasonEventId,
            List<RaceResult> results,
            Map<Integer, Integer> pointsByDriverId) throws SQLException;
}
