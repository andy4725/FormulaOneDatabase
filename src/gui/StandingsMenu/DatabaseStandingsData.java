package gui.StandingsMenu;

import java.sql.SQLException;
import java.util.List;

import model.DriverStanding;
import model.TeamStanding;
import repository.StandingsRepository;
import service.StandingsService;

/**
 * StandingsData that reads from the SQLite database.
 * The standings logic already exists in the service layer; this class
 * only passes the calls on.
 */
class DatabaseStandingsData implements StandingsData {

    private final StandingsRepository repository = new StandingsRepository();
    private final StandingsService service = new StandingsService(repository);

    public int findSeasonId(int saveSlot) throws SQLException {
        return repository.findSeasonId(saveSlot);
    }

    public List<DriverStanding> getDriverStandings(int seasonId) throws SQLException {
        return service.getDriverStandings(seasonId);
    }

    public List<TeamStanding> getTeamStandings(int seasonId) throws SQLException {
        return service.getTeamStandings(seasonId);
    }
}
