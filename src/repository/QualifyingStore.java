package repository;

import java.sql.SQLException;
import java.util.List;

import model.Driver;
import model.QualifyingResult;

/** Persistence operations required by qualifying workflow services. */
public interface QualifyingStore {
    void saveSessionLaps(
            int seasonEventId, String session, List<QualifyingResult> results)
            throws SQLException;

    boolean sessionHasLaps(int seasonEventId, String session) throws SQLException;

    int finalResultsCount(int seasonEventId) throws SQLException;

    List<Driver> findAdvancingDrivers(
            int seasonEventId, String session, int limit, List<Driver> allDrivers)
            throws SQLException;

    void saveFinalResults(
            int seasonEventId,
            List<QualifyingResult> q1Results,
            List<QualifyingResult> q2Results,
            List<QualifyingResult> q3Results) throws SQLException;

    List<QualifyingResult> loadSessionResults(
            int eventId, String session, List<Driver> drivers) throws SQLException;
}
