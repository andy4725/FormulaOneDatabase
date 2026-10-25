package repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import database.Database;
import model.Driver;

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

    // Updates the row in driver_stats, or creates it if the driver has none yet.
    public void updateRatings(int driverId, int experience, int racecraft, int pace, int awareness) throws SQLException {
        String sql = "INSERT INTO driver_stats (driver_id, experience, racecraft, pace, awareness) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT(driver_id) DO UPDATE SET " +
                     "experience = excluded.experience, racecraft = excluded.racecraft, " +
                     "pace = excluded.pace, awareness = excluded.awareness";

        try(Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, driverId);
            statement.setInt(2, experience);
            statement.setInt(3, racecraft);
            statement.setInt(4, pace);
            statement.setInt(5, awareness);
            statement.executeUpdate();
        }
    }
}