package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import database.Database;

public class SeasonService {
    public void startSeason(int slotId) throws SQLException {
        if(slotId < 1 || slotId > 3)
            throw new IllegalArgumentException("Pick a slot between 1 and 3.");

        try(Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                if(!slotExists(connection, slotId))
                    throw new IllegalArgumentException("That save slot does not exist.");

                if(seasonExists)
                    throw new IllegalArgumentException("This slot already has a season");

                List<Integer> calendar = getSeasonCalendar();
                if(calendar.isEmpty())
                    throw new IllegalArgumentException("The Grand Prix calendar is empty.");

                int seasonId = insertSeason(connection, slotId);
                insertSeasonEvents(connection, seasonId, calendar);

                connection.commit();
            } catch(SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private boolean slotExists(Connection connection, int slotId) throws SQLException {
        String sql = "SELECT 1 FROM save_slots WHERE save_slot_id = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, slotId);

            try(ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private boolean seasonExists(Connection connection, int slotId) throws SQLException {
        String sql = "SELECT 1 FROM seasons WHERE save_slot_id = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, slotId);

            try(ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private List<Integer> getSeasonCalendar() throws SQLException {
        List<Integer> grandPrixIDs = new ArrayList<Integer>();

        String sql = 
            "SELECT grand_prix.grand_prix_id " +
            "FROM grand_prix " +
            "JOIN circuits ON grand_prix.circuit_id = circuits.circuit_id " +
            "ORDER BY circuits.circuit.id";

        try(Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()) {
            while(results.next()) {
                grandPrixIDs.add(results.getInt("grand_prix_id"));
            }
        }

        return grandPrixIDs;
    }

    private int insertSeason(Connection connection, int slotId) throws SQLException {
        String sql =
            "INSERT INTO seasons (save_slot_id, status) " +
            "VALUES (?, IN_PROGRESS)";

        try(PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, slotId);
            statement.executeUpdate();

            try(ResultSet keys = statement.getGeneratedKeys()) {
                if(!keys.next()) {
                    throw new SQLException("Creating the season did not return an ID.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertSeasonEvents(Connection connection, int seasonId, List<Integer> grandPrixIds) throws SQLException {
        String sql = "INSERT INTO season_events " +
        "(season_id, round_number, grand_prix_id) " +
        "VALUES (?, ?, ?)";

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            for(int i = 0; i < grandPrixIds.size(); i++) {
                statement.setInt(1, seasonId);
                statement.setInt(2, i + 1);
                statement.setInt(3, grandPrixIds.get(i));
                statement.executeUpdate();
            }
        }
    }
}