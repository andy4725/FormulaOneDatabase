package repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.Database;
import model.Team;

import java.util.ArrayList;

public class TeamRepository {
    List<Team> teamList;

    public List<Team> getTeams() throws SQLException {
        teamList = new ArrayList<>();

        String sql = "SELECT teams.team_id, teams.name, teams.logo_path, team_stats.pace, team_stats.reliability, team_stats.sponsor_modifier " + 
                      "FROM teams " +
                      "LEFT JOIN team_stats ON teams.team_id = team_stats.team_id " +
                      "ORDER BY teams.name";

        try(Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()) {

            while(results.next()) {
                int teamId = results.getInt("team_id");
                String name = results.getString("name");
                String logoPath = results.getString("logo_path");
                int pace = results.getInt("pace");
                int reliability = results.getInt("reliability");
                float sponsorModifier = results.getFloat("sponsor_modifier");

                Team team = new Team(teamId, name, logoPath, pace, reliability, sponsorModifier);
                teamList.add(team);
            }
        }

        return teamList;
    }
}