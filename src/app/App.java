package app;

import java.sql.SQLException;

import model.SimulationData;
import model.Team;
import service.SimulationDataLoader;

public class App {
    public static void main(String[] args) throws SQLException {
        SimulationData data = new SimulationDataLoader().load();

        if (data.getTeams().isEmpty()) {
            System.out.println("No teams found in the database.");
            return;
        }

        for (Team team : data.getTeams()) {
            System.out.println(team.getTeamName());
        }
    }
}