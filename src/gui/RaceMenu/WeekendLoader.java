package gui.RaceMenu;

import java.sql.SQLException;
import java.util.List;

import model.Circuit;
import model.QualifyingResult;
import model.SimulationData;
import model.WeekendEvent;
import repository.QualifyingRepository;
import repository.RaceWeekendRepository;

/**
 * Reads from the database where the current weekend is:
 * which round, which circuit, how many quali sessions are done
 * and the results of Quali 1 (the screen starts showing from there).
 */
class WeekendLoader {

    /** Everything the screen needs to show the weekend. */
    static class LoadedWeekend {
        WeekendEvent event;                  // null: no weekend to play
        Circuit circuit;
        int sessionsDone;                    // 0..3
        List<QualifyingResult> firstResults; // Quali 1, null: no session finished yet
    }

    private final RaceWeekendRepository weekendRepository = new RaceWeekendRepository();
    private final QualifyingRepository qualifyingRepository = new QualifyingRepository();

    LoadedWeekend load(SimulationData data, int slot) throws SQLException {
        LoadedWeekend loaded = new LoadedWeekend();
        loaded.event = weekendRepository.findCurrentEvent(slot);
        if (loaded.event == null) {
            return loaded;
        }

        for (Circuit circuit : data.getCircuits()) {
            if (circuit.getCircuitId() == loaded.event.getCircuitId()) {
                loaded.circuit = circuit;
            }
        }

        int id = loaded.event.getSeasonEventId();
        for (String session : Sessions.CODES) {
            if (qualifyingRepository.sessionHasLaps(id, session)) {
                loaded.sessionsDone++;
            }
        }

        if (loaded.sessionsDone > 0) {
            loaded.firstResults = qualifyingRepository.loadSessionResults(
                    id, Sessions.CODES[0], data.getDrivers());
        }
        return loaded;
    }
}