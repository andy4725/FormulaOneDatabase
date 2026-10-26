package service;

import java.sql.SQLException;
import java.util.List;

import model.DriverStanding;
import model.TeamStanding;
import repository.StandingsRepository;

/** Exposes the season standings queries to application callers.
 * Ranking and points aggregation are implemented by the repository.
 */
public class StandingsService {
    private final StandingsRepository repository;

    public StandingsService() {
        this(new StandingsRepository());
    }

    public StandingsService(StandingsRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("A standings repository is required.");
        }
        this.repository = repository;
    }

    public List<DriverStanding> getDriverStandings(int seasonId) throws SQLException {
        return repository.getDriverStandings(seasonId);
    }

    public List<TeamStanding> getTeamStandings(int seasonId) throws SQLException {
        return repository.getTeamStandings(seasonId);
    }
}
