package gui.ResultsMenu;

import gui.Common.Flags;
import gui.Common.ListCard;
import gui.MainMenu.Theme;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import model.RaceSummary;

/** Level 1 of the Results screen: every race of the season. */
class RaceListView extends ListCard {

    private static final long serialVersionUID = 1L;

    /** Told when a finished race is clicked. */
    interface Listener {
        void raceSelected(RaceSummary race);
    }

    private final Listener listener;

    RaceListView(Listener listener) {
        super("Races");
        this.listener = listener;
    }

    /** Fills the list with one row per race. */
    void showRaces(List<RaceSummary> races) {
        List<JPanel> rows = new ArrayList<JPanel>();
        for (RaceSummary race : races) {
            rows.add(raceRow(race));
        }
        showRows(rows, "This season has no races.");
    }

    /** [R5] [Grand Prix / flag circuit, country] ........ [winner  >] */
    private JPanel raceRow(final RaceSummary race) {
        JPanel row = ResultsUi.baseRow();

        row.add(ResultsUi.fixed(Theme.label("R" + race.getRoundNumber(), Font.BOLD, 17,
                Theme.F1_RED), 56, 40), BorderLayout.WEST);
        row.add(ResultsUi.twoLines(race.getGrandPrixName(),
                race.getCircuitName() + ", " + race.getCountry(),
                Flags.get(race.getCountry(), 12), null), BorderLayout.CENTER);

        // Only a finished race can be opened; the others show their status.
        if (race.isComplete()) {
            String winner = race.getWinnerName() == null ? "-" : race.getWinnerName();
            row.add(Theme.label("Winner: " + winner + "   \u203A", Font.BOLD, 15, Theme.TEXT),
                    BorderLayout.EAST);
            ResultsUi.makeClickable(row, new Runnable() {
                public void run() {
                    listener.raceSelected(race);
                }
            });
        } else {
            row.add(Theme.label(ResultsFormat.statusText(race.getStatus()), Font.PLAIN, 14,
                    Theme.MUTED), BorderLayout.EAST);
        }
        return ResultsUi.finish(row);
    }
}
