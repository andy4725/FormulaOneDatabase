package app;

import java.sql.SQLException;

import model.SimulationData;
import service.QualifyingSimulator;
import service.SimulationDataLoader;
import model.Circuit;
import model.QualifyingResult;

import java.util.List;

public class App {
    private static int roundNumber = 0;
    public static void main(String[] args) throws SQLException {
        SimulationData data = new SimulationDataLoader().load();
        Circuit currentCircuit = null;
        List<Circuit> circuits = data.getCircuits();
        
        currentCircuit = circuits.get(roundNumber);

        if(currentCircuit == null) {
            throw new IllegalArgumentException("Circuit couldn't be loaded.");
        }

        String name = currentCircuit.getName();

        List<QualifyingResult> results = 
            new QualifyingSimulator().simulate(currentCircuit, data.getDrivers(), data.getTeams());

        System.out.println(name + " qualifying - benchmark: " + currentCircuit.getQualiBenchmarkTime() + " s");

        for(int i = 0; i < results.size(); i++) {
            QualifyingResult result = results.get(i);
            double time = result.getLapTimeSeconds();
            int minutes = (int) (time / 60);
            double seconds = time - minutes * 60;

            System.out.printf("%2d. %-24s %d:%06.3f%n",
                i + 1,
                result.getDriver().getName(),
                minutes,
                seconds);

            //System.out.println(result.getDriver().getName() + " - 1:" + (result.getLapTimeSeconds() - 60));
        }
    }
}