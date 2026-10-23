package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Provides connections to the project's SQLite database. */
public final class Database {
    private static final String DATABASE_URL = "jdbc:sqlite:F1Database.sqlite3";
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
        if (resultStatusIsRequired(connection)) {
            makeRaceResultStatusNullable(connection);
        }
        ensureSaveSlots(connection);
        raceGridSchemaChecked = true;
    }

    /** Makes sure save_slots has the last_played column and slots 1-3. */
    private static void ensureSaveSlots(Connection connection)
            throws SQLException {
        if (!hasColumn(connection, "save_slots", "last_played")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE save_slots ADD COLUMN last_played TEXT");
            }
        }
        try (Statement statement = connection.createStatement()) {
            for (int slot = 1; slot <= 3; slot++) {
                statement.execute("INSERT OR IGNORE INTO save_slots (save_slot_id, name) "
                        + "VALUES (" + slot + ", 'Slot " + slot + "')");
            }
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column)
            throws SQLException {
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
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
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
            statement.execute("DROP TABLE race_results");
            statement.execute("ALTER TABLE race_results_new RENAME TO race_results");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
