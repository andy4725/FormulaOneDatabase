package gui.ResultsMenu;

import java.sql.SQLException;
import java.util.List;

import model.RaceResultEntry;
import model.RaceSummary;
import repository.ResultsRepository;
import repository.StandingsRepository;

/** ResultsData that reads from the SQLite database through the repositories. */
class DatabaseResultsData implements ResultsData {

    private final StandingsRepository standings = new StandingsRepository();
    private final ResultsRepository results = new ResultsRepository();

    public int findSeasonId(int saveSlot) throws SQLException {
        return standings.findSeasonId(saveSlot);
    }

    public List<RaceSummary> getRaces(int seasonId) throws SQLException {
        return results.getRaces(seasonId);
    }

    public List<RaceResultEntry> getRaceResults(int seasonEventId) throws SQLException {
        return results.getRaceResults(seasonEventId);
    }

    public List<Double> getLapTimes(int seasonEventId, int driverId) throws SQLException {
        return results.getLapTimes(seasonEventId, driverId);
    }
}
