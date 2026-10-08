package app;

import model.SimulationData;
import service.SimulationDataLoader;

public class App {
    public static void main(String[] args) throws SQLException {
        SimulationData data = new SimulationDataLoader().load();
    }
}