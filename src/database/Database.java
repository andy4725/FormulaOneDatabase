package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens SQLite connections and applies connection-level settings.
 * The first connection also upgrades the few schema details required by current code.
 */
public final class Database {
    // The SQLite file is resolved from the application's working directory.
    private static final String DATABASE_URL = "jdbc:sqlite:F1Database.sqlite3";
    // Schema setup only needs to run once per process after the first connection opens.
    private static boolean raceGridSchemaChecked;

    private Database() {
    }

    /** Opens a connection and enables SQLite foreign-key enforcement for it. */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicit loading also works with older JDBC drivers used for Java 6.
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver is missing. Add its JAR to the lib folder.", e);
        }

        Connection connection = DriverManager.getConnection(DATABASE_URL);
        try {
            // SQLite enforces foreign keys per connection, so configure each new one.
            Statement statement = connection.createStatement();
            try {
                statement.execute("PRAGMA foreign_keys = ON");
            } finally {
                statement.close();
            }
            ensureRaceGridSchema(connection);
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    private static synchronized void ensureRaceGridSchema(Connection connection)
            throws SQLException {
        if (raceGridSchemaChecked) {
            return;
        }
        // Migrate older databases before adding or using the save-slot metadata.
        if (resultStatusIsRequired(connection)) {
            makeRaceResultStatusNullable(connection);
        }
        ensureSaveSlots(connection);
        raceGridSchemaChecked = true;
    }

    /** Makes sure save_slots has the last_played column and slots 1-3. */
    private static void ensureSaveSlots(Connection connection)
            throws SQLException {
        // Existing databases may predate last_played; add it without replacing user data.
        if (!hasColumn(connection, "save_slots", "last_played")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE save_slots ADD COLUMN last_played TEXT");
            }
        }
        // Seed the standard slots while preserving any names or data already saved.
        try (Statement statement = connection.createStatement()) {
            for (int slot = 1; slot <= 3; slot++) {
                statement.execute("INSERT OR IGNORE INTO save_slots (save_slot_id, name) "
                        + "VALUES (" + slot + ", 'Slot " + slot + "')");
            }
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column)
            throws SQLException {
        // PRAGMA table_info returns one row per column in the requested table.
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) {
                if (column.equals(columns.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean resultStatusIsRequired(Connection connection)
            throws SQLException {
        // SQLite's notnull flag indicates whether legacy schema requires a status value.
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet columns = statement.executeQuery("PRAGMA table_info(race_results)")) {
            while (columns.next()) {
                if ("result_status".equals(columns.getString("name"))) {
                    return columns.getInt("notnull") == 1;
                }
            }
        }
        throw new SQLException("The race_results.result_status column is missing.");
    }

    private static void makeRaceResultStatusNullable(Connection connection)
            throws SQLException {
        // SQLite cannot directly relax a NOT NULL constraint, so rebuild the table.
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            // Preserve the result table's keys, checks, and relationships while allowing
            // result_status to be NULL for results that do not yet have a status.
            statement.execute("CREATE TABLE race_results_new ("
                    + "season_event_id INTEGER NOT NULL, "
                    + "driver_id INTEGER NOT NULL, "
                    + "grid_position INTEGER CHECK (grid_position IS NULL OR grid_position > 0), "
                    + "finishing_position INTEGER CHECK "
                    + "(finishing_position IS NULL OR finishing_position > 0), "
                    + "result_status TEXT CHECK "
                    + "(result_status IS NULL OR result_status IN ('FINISHED', 'DNF', 'DSQ')), "
                    + "points REAL NOT NULL DEFAULT 0 CHECK (points >= 0), "
                    + "fastest_lap INTEGER NOT NULL DEFAULT 0 CHECK (fastest_lap IN (0, 1)), "
                    + "PRIMARY KEY (season_event_id, driver_id), "
                    + "UNIQUE (season_event_id, finishing_position), "
                    + "FOREIGN KEY (season_event_id) REFERENCES season_events(season_event_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE RESTRICT"
                    + ")");
            statement.execute("INSERT INTO race_results_new "
                    + "(season_event_id, driver_id, grid_position, finishing_position, "
                    + "result_status, points, fastest_lap) "
                    + "SELECT season_event_id, driver_id, grid_position, finishing_position, "
                    + "result_status, points, fastest_lap FROM race_results");
            // Replace the old table only after its existing rows have been copied.
            statement.execute("DROP TABLE race_results");
            statement.execute("ALTER TABLE race_results_new RENAME TO race_results");
            connection.commit();
        } catch (SQLException e) {
            // Keep the old table intact if any part of the migration fails.
            connection.rollback();
            throw e;
        } finally {
            // Restore the connection's normal transaction behavior for callers.
            connection.setAutoCommit(true);
        }
    }
}
