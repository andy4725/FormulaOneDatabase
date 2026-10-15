package app;

import java.sql.SQLException;

import model.SimulationData;
import service.QualifyingSimulator;
import service.SimulationDataLoader;
import model.Team;
import model.Circuit;
import model.QualifyingResult;

import java.util.List;

public class App {
    public static void main(String[] args) throws SQLException {
        SimulationData data = new SimulationDataLoader().load();
        Circuit monaco = null;

        for(Circuit circuit : data.getCircuits()) {
            if("Monaco".equals(circuit.getCountry())) {
                monaco = circuit;
                break;
            }
        }

        if(monaco == null) {
            throw new IllegalArgumentException("Monaco wasn't found.");
        }

        List<QualifyingResult> results = 
            new QualifyingSimulator().simulate(monaco, data.getDrivers(), data.getTeams());

        System.out.println("Monaco qualifying - benchmark: " + monaco.getQualiBenchmarkTime() + " s");

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