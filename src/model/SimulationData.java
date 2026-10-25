package model;

import java.util.List;

/** Groups the reference data loaded for the running application.
 * Services use these shared profiles when simulating a season weekend.
 */
public class SimulationData {
    private final List<Driver> drivers;
    private final List<Team> teams;
    private final List<Sponsor> sponsors;
    private final List<Circuit> circuits;

    public SimulationData(
        List<Driver> drivers,
        List<Team> teams,
        List<Circuit> circuits,
        List<Sponsor> sponsors) {
        
        this.drivers = drivers;
        this.teams = teams;
        this.circuits = circuits;
        this.sponsors = sponsors;
    }

    public List<Circuit> getCircuits() {
        return circuits;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Driver> getDrivers() {
        return drivers;
    }

    public List<Sponsor> getSponsors() {
        return sponsors;
    }
}
