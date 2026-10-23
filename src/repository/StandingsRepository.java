package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import model.DriverStanding;
import model.TeamStanding;

public class StandingsRepository {

    public List<DriverStanding> getDriverStandings(int seasonId) throws SQLException {
        validateSeasonId(seasonId);

        String sql = "SELECT d.driver_id, d.name AS driver_name, "
                + "t.name AS team_name, COALESCE(SUM(rr.points), 0) AS points "
                + "FROM drivers d "
                + "JOIN teams t ON t.team_id = d.team_id "
                + "LEFT JOIN race_results rr "
                + "ON rr.driver_id = d.driver_id "
                + "AND rr.result_status = 'FINISHED' "
                + "AND rr.season_event_id IN ("
                + "SELECT season_event_id FROM season_events "
                + "WHERE season_id = ?) "
                + "GROUP BY d.driver_id, d.name, t.name "
                + "ORDER BY points DESC, d.name COLLATE NOCASE ASC";

        List<DriverStanding> standings = new ArrayList<DriverStanding>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                int rowNumber = 0;
                int position = 0;
                double previousPoints = 0.0;

                while (result.next()) {
                    double points = result.getDouble("points");

                    if (rowNumber == 0
                            || Double.compare(points, previousPoints) != 0) {
                        position = rowNumber + 1;
                    }

                    standings.add(new DriverStanding(
                            position,
                            result.getInt("driver_id"),
                            result.getString("driver_name"),
                            result.getString("team_name"),
                            points));

                    previousPoints = points;
                    rowNumber++;
                }
            }
        }

        return standings;
    }

    public List<TeamStanding> getTeamStandings(int seasonId)
            throws SQLException {
        validateSeasonId(seasonId);

        String sql = "SELECT t.team_id, t.name AS team_name, t.logo_path, "
                + "COALESCE(SUM(rr.points), 0) AS points "
                + "FROM teams t "
                + "LEFT JOIN drivers d ON d.team_id = t.team_id "
                + "LEFT JOIN race_results rr "
                + "ON rr.driver_id = d.driver_id "
                + "AND rr.result_status = 'FINISHED' "
                + "AND rr.season_event_id IN ("
                + "SELECT season_event_id FROM season_events "
                + "WHERE season_id = ?) "
                + "GROUP BY t.team_id, t.name, t.logo_path "
                + "ORDER BY points DESC, t.name COLLATE NOCASE ASC";

        List<TeamStanding> standings =
                new ArrayList<TeamStanding>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                int rowNumber = 0;
                int position = 0;
                double previousPoints = 0.0;

                while (result.next()) {
                    double points = result.getDouble("points");

                    if (rowNumber == 0
                            || Double.compare(points, previousPoints) != 0) {
                        position = rowNumber + 1;
                    }

                    standings.add(new TeamStanding(
                            position,
                            result.getInt("team_id"),
                            result.getString("team_name"),
                            result.getString("logo_path"),
                            points));

                    previousPoints = points;
                    rowNumber++;
                }
            }
        }

        return standings;
    }

    private void validateSeasonId(int seasonId) {
        if (seasonId <= 0) {
            throw new IllegalArgumentException("A valid season ID is required.");
        }
    }
}