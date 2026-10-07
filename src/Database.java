import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Provides connections to the project's SQLite database. */
public final class Database {
    private static final String DATABASE_URL = "jdbc:sqlite:F1Database.sqlite3";

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
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }
}
