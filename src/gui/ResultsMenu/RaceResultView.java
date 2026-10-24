package gui.ResultsMenu;

import gui.Common.ListCard;
import gui.Common.Medals;
import gui.Common.PointsFormat;
import gui.MainMenu.Theme;
import gui.RaceMenu.Avatar;
import gui.RaceMenu.PortraitCache;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Driver;
import model.RaceResultEntry;

/** Level 2 of the Results screen: the result of one race with every driver. */
class RaceResultView extends ListCard {

    private static final long serialVersionUID = 1L;

    /** Told when a driver's row is clicked. */
    interface Listener {
        void driverSelected(RaceResultEntry entry);
    }

    private final Listener listener;

    // Race results only have driver ids; portraits need the Driver object.
    private final Map<Integer, Driver> driversById = new HashMap<Integer, Driver>();
    private final PortraitCache portraits = new PortraitCache();

    RaceResultView(List<Driver> drivers, Listener listener) {
        super("Classification");
        this.listener = listener;
        for (Driver driver : drivers) {
            driversById.put(Integer.valueOf(driver.getDriverId()), driver);
        }
    }

    /** Fills the list with one row per driver. */
    void showResults(List<RaceResultEntry> entries) {
        RaceResultEntry winner = ResultsFormat.winner(entries);
        List<JPanel> rows = new ArrayList<JPanel>();
        for (RaceResultEntry entry : entries) {
            rows.add(resultRow(entry, winner));
        }
        showRows(rows, "No result was saved for this race.");
    }

    /**
     * [pos] [portrait] [name / team] ...... [grid +/-] [time or gap] [points]  >
     */
    private JPanel resultRow(final RaceResultEntry entry, RaceResultEntry winner) {
        JPanel row = ResultsUi.baseRow();
        Color medal = Medals.forPosition(entry.getFinishingPosition());
        Color highlight = medal != null ? medal : Theme.TEXT;

        // ----- left: position and portrait -----
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(ResultsUi.fixed(Theme.label(ResultsFormat.position(entry), Font.BOLD, 18,
                highlight), 44, 40));
        Driver driver = driversById.get(Integer.valueOf(entry.getDriverId()));
        left.add(new Avatar(driver == null ? null : portraits.get(driver),
                entry.getDriverName(), 40));
        row.add(left, BorderLayout.WEST);

        // ----- middle: name, team (and the fastest-lap badge) -----
        row.add(ResultsUi.twoLines(entry.getDriverName(), entry.getTeamName(),
                entry.hasFastestLap() ? "FASTEST LAP" : null), BorderLayout.CENTER);

        // ----- right: grid change, time, points -----
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.add(gridChange(entry));
        right.add(ResultsUi.fixed(ResultsUi.rightAligned(
                ResultsFormat.timeText(entry, winner), Font.PLAIN, 15, Theme.TEXT), 130, 40));
        right.add(ResultsUi.fixed(ResultsUi.rightAligned(
                PointsFormat.format(entry.getPoints()) + " pts", Font.BOLD, 15, highlight),
                80, 40));
        right.add(ResultsUi.fixed(ResultsUi.rightAligned("\u203A", Font.BOLD, 18, Theme.MUTED),
                30, 40));
        row.add(right, BorderLayout.EAST);

        ResultsUi.makeClickable(row, new Runnable() {
            public void run() {
                listener.driverSelected(entry);
            }
        });
        return ResultsUi.finish(row);
    }

    /** "Grid 7" with "\u25B23" (green) or "\u25BC3" (red) under it. */
    private static JComponent gridChange(RaceResultEntry entry) {
        int grid = entry.getGridPosition();
        int gained = ResultsFormat.placesGained(entry);

        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setOpaque(false);
        box.add(ResultsUi.rightAligned(grid > 0 ? "Grid " + grid : "Grid -",
                Font.PLAIN, 12, Theme.MUTED));

        JLabel change;
        if (gained > 0) {
            change = ResultsUi.rightAligned("\u25B2" + gained, Font.BOLD, 13, ResultsUi.GREEN);
        } else if (gained < 0) {
            change = ResultsUi.rightAligned("\u25BC" + (-gained), Font.BOLD, 13, ResultsUi.RED);
        } else {
            change = ResultsUi.rightAligned("\u2013", Font.BOLD, 13, Theme.MUTED);
        }
        box.add(change);
        return ResultsUi.fixed(box, 70, 40);
    }
}
