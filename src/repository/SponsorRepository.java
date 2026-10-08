package repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.Database;
import model.Sponsor;

import java.util.ArrayList;

public class SponsorRepository {
    List<Sponsor> sponsorList;

    public List<Sponsor> getSponsor() throws SQLException {
        sponsorList = new ArrayList<>();

        String sql = "SELECT sponsors.sponsor_id, sponsors.team_id, sponsors.name, sponsors.logo_path, sponsors.funding_amount " +
                     "FROM sponsors " +
                     "LEFT JOIN teams on sponsors.team_id = teams.team_id " +
                     "ORDER BY sponsors.name";

        try(Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()) {

            while(results.next()) {
                int sponsorId = results.getInt("sponsor_id");
                int teamId = results.getInt("team_id");
                String name = results.getString("name");
                String logoPath = results.getString("logo_path");
                float fundingAmount = results.getFloat("funding_amount");

                Sponsor sponsor = new Sponsor(sponsorId, teamId, name, logoPath, fundingAmount);
                sponsorList.add(sponsor);
            }
        }

        return sponsorList;
    }
}
