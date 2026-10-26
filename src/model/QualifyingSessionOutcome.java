package model;

import java.util.List;

/** Bundles the session name, its results, and the weekend's qualifying state.
 * The completion flag becomes true only after Q3 has produced the final grid.
 */
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
