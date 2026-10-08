package service;

import java.sql.SQLException;

import model.SimulationData;
import repository.CircuitRepository;
import repository.DriverRepository;
import repository.SponsorRepository;
import repository.TeamRepository;

public class SimulationDataLoader {
    public SimulationData load() throws SQLException {
        return new SimulationData(
            new DriverRepository().getDrivers(),
            new TeamRepository().getTeams(),
            new SponsorRepository().getSponsor(),
            new CircuitRepository().getCircuits()
        );
    }
}
