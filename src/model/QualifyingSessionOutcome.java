package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QualifyingSessionOutcome {
    private final String session;
    private final List<QualifyingResult> results;
    private final boolean qualifyingComplete;

    public QualifyingSessionOutcome(
        String session,
        List<QualifyingResult> results,
        boolean qualifyingComplete) {
        if (session == null || results == null) {
            throw new IllegalArgumentException("Session and results are required.");
        }
        this.session = session;
        this.results = Collections.unmodifiableList(
                new ArrayList<QualifyingResult>(results));
        this.qualifyingComplete = qualifyingComplete;

    public String getSession() {
        return session;
    }

    public List<QualifyingResult> getResults() {
        return results;
    }

    public boolean isQualifyingComplete() {
        return qualifyingComplete;
    }
}
