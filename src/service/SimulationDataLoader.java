package service;

import java.sql.SQLException;

import model.SimulationData;
import repository.CircuitRepository;
import repository.DriverRepository;
import repository.SponsorRepository;
import repository.TeamRepository;

/** Builds the startup data bundle from the database-backed repositories.
 * This keeps individual lookup details out of the application entry point.
 */
public class SimulationDataLoader {
    public SimulationData load() throws SQLException {
        return new SimulationData(
            new DriverRepository().getDrivers(),
            new TeamRepository().getTeams(),
            new CircuitRepository().getCircuits(),
            new SponsorRepository().getSponsor()
        );
    }
}
