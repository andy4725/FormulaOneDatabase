package service;

import java.sql.SQLException;
import java.util.List;

import model.Circuit;
import model.Driver;
import model.QualifyingResult;
import model.Team;

public class NextQualifyingSessionService {
    private final QualifyingService qualifyingService;

    public NextQualifyingSessionService() {
        this.qualifyingService = new QualifyingService();
    }

    public List<QualifyingResult> simulateNextSession(
            int seasonEventId,
            String session,
            Circuit circuit,
            List<Driver> drivers,
            List<Team> teams) throws SQLException {

        return qualifyingService.simulateAndSave(
            seasonEventId,
            session,
            circuit,
            drivers,
            teams);
    }
}