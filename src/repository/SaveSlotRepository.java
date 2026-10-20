package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the save slots and the results saved in them, in the SQLite
 * database. Two tables are created automatically when needed:
 *
 *   SaveSlot    (slot, last_played)                      - slots in use
 *   SlotResult  (id, slot, title, details, saved_at)     - results per slot
 */
public class SaveSlotRepository {

    // >>> Change this if your database file is somewhere else. <<<
    // If your Database class already has a method that returns a
    // Connection, return that from connect() instead.
    private static final String URL = "jdbc:sqlite:F1Database.sqlite3";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            // The driver also registers itself in newer versions.
        }
    }

    // =====================================================
    // READ
    // =====================================================

    /** When the slot was last played ("yyyy-MM-dd HH:mm:ss"), or null if empty. */
    public String lastPlayed(int slot) throws SQLException {
        Connection connection = connect();
        PreparedStatement statement = null;
        ResultSet rows = null;
        try {
            statement = connection.prepareStatement(
                    "SELECT last_played FROM SaveSlot WHERE slot = ?");
            statement.setInt(1, slot);
            rows = statement.executeQuery();
            return rows.next() ? rows.getString(1) : null;
        } finally {
            close(rows);
            close(statement);
            close(connection);
        }
    }

    /**
     * Results saved in a slot, newest first.
     * Every entry is {title, details, savedAt}.
     */
    public List<String[]> listResults(int slot) throws SQLException {
        Connection connection = connect();
        PreparedStatement statement = null;
        ResultSet rows = null;
        try {
            statement = connection.prepareStatement(
                    "SELECT title, details, saved_at FROM SlotResult "
                    + "WHERE slot = ? ORDER BY id DESC");
            statement.setInt(1, slot);
            rows = statement.executeQuery();

            List<String[]> results = new ArrayList<String[]>();
            while (rows.next()) {
                results.add(new String[] {
                    rows.getString(1), rows.getString(2), rows.getString(3)
                });
            }
            return results;
        } finally {
            close(rows);
            close(statement);
            close(connection);
        }
    }

    // =====================================================
    // WRITE
    // =====================================================

    /** Marks the slot as used and sets "last played" to now. */
    public void touch(int slot) throws SQLException {
        Connection connection = connect();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(TOUCH_SQL);
            statement.setInt(1, slot);
            statement.executeUpdate();
        } finally {
            close(statement);
            close(connection);
        }
    }

    /** Saves one result (for example a race) in the slot. */
    public void saveResult(int slot, String title, String details)
            throws SQLException {
        Connection connection = connect();
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(TOUCH_SQL);
            statement.setInt(1, slot);
            statement.executeUpdate();
            close(statement);

            statement = connection.prepareStatement(
                    "INSERT INTO SlotResult (slot, title, details, saved_at) "
                    + "VALUES (?, ?, ?, datetime('now','localtime'))");
            statement.setInt(1, slot);
            statement.setString(2, title);
            statement.setString(3, details);
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            rollback(connection);
            throw exception;
        } finally {
            close(statement);
            close(connection);
        }
    }

    /** Deletes the slot and every result saved in it. */
    public void delete(int slot) throws SQLException {
        Connection connection = connect();
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(
                    "DELETE FROM SlotResult WHERE slot = ?");
            statement.setInt(1, slot);
            statement.executeUpdate();
            close(statement);

            statement = connection.prepareStatement(
                    "DELETE FROM SaveSlot WHERE slot = ?");
            statement.setInt(1, slot);
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            rollback(connection);
            throw exception;
        } finally {
            close(statement);
            close(connection);
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private static final String TOUCH_SQL =
            "INSERT OR REPLACE INTO SaveSlot (slot, last_played) "
            + "VALUES (?, datetime('now','localtime'))";

    /** Opens a connection and makes sure the two tables exist. */
    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS SaveSlot ("
                    + "slot INTEGER PRIMARY KEY, "
                    + "last_played TEXT NOT NULL)");
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS SlotResult ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "slot INTEGER NOT NULL, "
                    + "title TEXT NOT NULL, "
                    + "details TEXT, "
                    + "saved_at TEXT NOT NULL)");
        } catch (SQLException exception) {
            close(connection);
            throw exception;
        } finally {
            close(statement);
        }
        return connection;
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // nothing more to do
        }
    }

    private static void close(ResultSet rows) {
        if (rows != null) {
            try {
                rows.close();
            } catch (SQLException ignored) {
                // nothing more to do
            }
        }
    }

    private static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
                // nothing more to do
            }
        }
    }

    private static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nothing more to do
            }
        }
    }
}