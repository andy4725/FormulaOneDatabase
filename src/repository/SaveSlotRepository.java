package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

/**
 * Reads and writes the save slots in the save_slots table.
 *
 *   save_slots (save_slot_id, name, last_played)
 *
 * A slot holds a game when it has a row in seasons. Deleting a slot
 * deletes its season; the season's rounds and results go with it
 * (ON DELETE CASCADE).
 */
public class SaveSlotRepository {

    // =====================================================
    // READ
    // =====================================================

    /** True if the slot has a season. */
    public boolean hasSeason(int slot) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM seasons WHERE save_slot_id = ?")) {
            statement.setInt(1, slot);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    /**
     * When the slot was last played ("yyyy-MM-dd HH:mm:ss"), or null if
     * it has no season. Falls back to when the season was created.
     */
    public String lastPlayed(int slot) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COALESCE(ss.last_played, s.created_at) "
                     + "FROM save_slots ss "
                     + "JOIN seasons s ON s.save_slot_id = ss.save_slot_id "
                     + "WHERE ss.save_slot_id = ?")) {
            statement.setInt(1, slot);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getString(1) : null;
            }
        }
    }

    // =====================================================
    // WRITE
    // =====================================================

    /** Sets "last played" of the slot to now. */
    public void touch(int slot) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE save_slots "
                     + "SET last_played = datetime('now','localtime') "
                     + "WHERE save_slot_id = ?")) {
            statement.setInt(1, slot);
            statement.executeUpdate();
        }
    }

    /** Deletes the slot's season (and everything saved in it). */
    public void delete(int slot) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM seasons WHERE save_slot_id = ?")) {
                    statement.setInt(1, slot);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE save_slots SET last_played = NULL "
                        + "WHERE save_slot_id = ?")) {
                    statement.setInt(1, slot);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
