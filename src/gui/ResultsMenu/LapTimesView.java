package gui.ResultsMenu;

import gui.Common.ListCard;
import gui.MainMenu.Theme;
import gui.RaceMenu.TimeFormat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.RaceResultEntry;

/**
 * Level 3 of the Results screen: one driver's laps in one race.
 * A row of totals on top, the list of laps below.
 */
class LapTimesView extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JPanel summary = new JPanel(new GridLayout(1, 5, 12, 0));
    private final ListCard laps = new ListCard("Lap times");

    LapTimesView() {
        super(new BorderLayout(0, 18));
        setOpaque(false);
        summary.setOpaque(false);
        add(summary, BorderLayout.NORTH);
        add(laps, BorderLayout.CENTER);
    }

    /**
     * Shows the totals of the driver straight away (they are already
     * known from the result row) and "Loading..." until showLaps().
     */
    void showDriver(RaceResultEntry entry) {
        summary.removeAll();
        int lapCount = entry.getLapCount();
        int position = entry.getFinishingPosition();
        boolean hasLaps = lapCount > 0;

        summary.add(statTile("Position",
                position > 0 ? "P" + position : entry.getStatus(), Theme.TEXT));
        summary.add(statTile("Race time",
                hasLaps ? TimeFormat.total(entry.getTotalTimeSeconds()) : "-", Theme.TEXT));
        summary.add(statTile("Best lap",
                hasLaps ? TimeFormat.lap(entry.getBestLapSeconds()) : "-", bestColor(entry)));
        summary.add(statTile("Average lap",
                hasLaps ? TimeFormat.lap(entry.getTotalTimeSeconds() / lapCount) : "-",
                Theme.TEXT));
        summary.add(statTile("Laps", String.valueOf(lapCount), Theme.TEXT));
        summary.revalidate();
        summary.repaint();

        laps.showMessage("Loading...");
    }

    /** Fills the list with one row per lap; the best lap is highlighted. */
    void showLaps(List<Double> lapTimes, RaceResultEntry entry) {
        double best = Double.MAX_VALUE;
        for (Double lap : lapTimes) {
            best = Math.min(best, lap.doubleValue());
        }

        List<JPanel> rows = new ArrayList<JPanel>();
        double elapsed = 0.0; // race time after each lap
        for (int i = 0; i < lapTimes.size(); i++) {
            double lap = lapTimes.get(i).doubleValue();
            elapsed += lap;
            rows.add(lapRow(i + 1, lap, lap - best, elapsed,
                    lap == best ? bestColor(entry) : null));
        }
        laps.showRows(rows, "No lap times were saved for this driver.");
    }

    void showMessage(String message) {
        laps.showMessage(message);
    }

    /** Purple if this driver set the fastest lap of the race, green otherwise. */
    private static Color bestColor(RaceResultEntry entry) {
        return entry.hasFastestLap() ? ResultsUi.PURPLE : ResultsUi.GREEN;
    }

    /** [Lap 12] ...... [1:32.481] [+0.532] [0:18:21.004] */
    private static JPanel lapRow(int lapNumber, double seconds, double toBest,
            double elapsed, Color highlight) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        if (highlight != null) {
            row.setOpaque(true);
            row.setBackground(Theme.blend(Theme.TILE_BG, highlight, 0.14));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 4, 0, 0, highlight),
                    BorderFactory.createEmptyBorder(8, 14, 8, 18)));
        } else {
            row.setOpaque(false);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        }

        row.add(Theme.label("Lap " + lapNumber, Font.BOLD, 15, Theme.TEXT), BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.add(ResultsUi.fixed(ResultsUi.rightAligned(TimeFormat.lap(seconds), Font.BOLD, 15,
                highlight != null ? highlight : Theme.TEXT), 110, 22));
        right.add(ResultsUi.fixed(ResultsUi.rightAligned(
                highlight != null ? "BEST" : TimeFormat.gap(toBest), Font.PLAIN, 14,
                highlight != null ? highlight : Theme.MUTED), 90, 22));
        right.add(ResultsUi.fixed(ResultsUi.rightAligned(TimeFormat.total(elapsed), Font.PLAIN,
                14, Theme.MUTED), 130, 22));
        row.add(right, BorderLayout.EAST);

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Small card: grey name on top, big value below. */
    private static JPanel statTile(String name, String value, Color valueColor) {
        JPanel tile = new JPanel(new GridLayout(2, 1, 0, 4));
        tile.setBackground(Theme.TILE_BG);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        tile.add(Theme.label(name, Font.PLAIN, 13, Theme.MUTED));
        tile.add(Theme.label(value, Font.BOLD, 20, valueColor));
        return tile;
    }
}
