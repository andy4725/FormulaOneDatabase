package gui.StandingsMenu;

import gui.Common.ListCard;
import java.util.ArrayList;
import java.util.List;

import model.TeamStanding;

/** Right card of the Standings screen: the teams' championship. */
class TeamStandingsView extends ListCard {

    private static final long serialVersionUID = 1L;

    TeamStandingsView() {
        super("Teams");
    }

    /** Fills the card with one row per team (name and points). */
    void showStandings(List<TeamStanding> standings) {
        List<StandingRow> rows = new ArrayList<StandingRow>();
        for (TeamStanding s : standings) {
            // Teams have no portrait and no second line.
            rows.add(new StandingRow(s.getPosition(), null, s.getTeamName(), null,
                    s.getPoints()));
        }
        showRows(rows, "No teams found.");
    }
}
