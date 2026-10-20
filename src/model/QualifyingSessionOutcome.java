package model;

import java.util.List;

public final class QualifyingSessionOutcome {
    private final String session;
    private final List<QualifyingResult> results;
    private final boolean qualifyingComplete;

    public QualifyingSessionOutcome(
        String session,
        List<QualifyingResult> results,
        boolean qualifyingComplete) {

            this.session = session;
            this.results = results;
            this.qualifyingComplete = qualifyingComplete;
        }

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
