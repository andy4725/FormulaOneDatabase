import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {
    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;
        ResultSet results = null;

        try {
            connection = Database.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery("SELECT name FROM teams ORDER BY name");

            System.out.println("Connected to the F1 database.");
            System.out.println("Teams:");
            while (results.next()) {
                System.out.println("- " + results.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Could not connect to or read the F1 database.");
            System.err.println(e.getMessage());
        } finally {
            closeQuietly(results);
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    private static void closeQuietly(ResultSet results) {
        if (results != null) {
            try {
                results.close();
            } catch (SQLException ignored) {
                // Preserve the original connection or query error.
            }
        }
    }

    private static void closeQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
                // Preserve the original connection or query error.
            }
        }
    }

    private static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Preserve the original connection or query error.
            }
        }
    }
}
