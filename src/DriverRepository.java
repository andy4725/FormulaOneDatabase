import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class DriverRepository {
    List<Driver> driverList;

    public List<Driver> getDrivers() throws SQLException {
        driverList = new ArrayList<>();

        String sql = "SELECT drivers.driver_id, drivers.team_id, drivers.name, drivers.car_number, drivers.portrait_path, driver_stats.experience, driver_stats.racecraft, driver_stats.pace, driver_stats.awareness " +
                     "FROM drivers " +
                     "LEFT JOIN driver_stats ON drivers.driver_id = driver_stats.driver_id " +
                     "ORDER BY drivers.name";

        try(Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()) {

            while(results.next()) {
                int driverId = results.getInt("driver_id");
                int teamId = results.getInt("team_id");
                String name = results.getString("name");
                int carNumber = results.getInt("car_number");
                String portraitPath = results.getString("portrait_path");
                int experience = results.getInt("experience");
                int racecraft = results.getInt("racecraft");
                int pace = results.getInt("pace");
                int awareness = results.getInt("awareness");

                Driver driver = new Driver(driverId, teamId, name, carNumber, portraitPath, experience, racecraft, pace, awareness);
                driverList.add(driver);
            }
        }

        return driverList;
    }
}