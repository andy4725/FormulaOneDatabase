package repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import database.Database;
import model.Circuit;

public class CircuitRepository {
    List<Circuit> circuitList;

    public List<Circuit> getCircuits() throws SQLException {
        circuitList = new ArrayList<>();

        String sql = "SELECT circuit_id, name, country, length, race_laps, quali_benchmark_time, race_benchmark_time " +
                     "FROM circuits " +
                     "ORDER BY name";

        try(Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()) {

            while(results.next()) {
                int circuitId = results.getInt("circuit_id");
                String name = results.getString("name");
                String country = results.getString("country");
                float length = results.getFloat("length");
                int raceLaps = results.getInt("race_laps");
                float qualiBenchmarkTime = results.getFloat("quali_benchmark_time");
                float raceBenchmarkTime = results.getFloat("race_benchmark_time");

                Circuit circuit = new Circuit(circuitId, name, country, length, raceLaps, qualiBenchmarkTime, raceBenchmarkTime);
                circuitList.add(circuit);
            }
        }

        return circuitList;
    }
}
