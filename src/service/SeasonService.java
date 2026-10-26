package service;

import java.sql.SQLException;

import repository.SeasonRepository;

/** Coordinates season lifecycle rules for save slots. */
public class SeasonService {
    private final SeasonRepository repository;

    public SeasonService() {
        this(new SeasonRepository());
    }

    public SeasonService(SeasonRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("A season repository is required.");
        }
        this.repository = repository;
    }

    public void startSeason(int slotId) throws SQLException {
        validateSlotId(slotId);
        repository.createSeasonForSlot(slotId);
    }

    public void eraseSeason(int slotId) throws SQLException {
        validateSlotId(slotId);
        repository.deleteSeasonForSlot(slotId);
    }

    private void validateSlotId(int slotId) {
        if (slotId < 1 || slotId > 3) {
            throw new IllegalArgumentException("Pick a slot between 1 and 3.");
        }
    }
}
