package gui.RaceMenu;

import gui.DriversMenu.DarkScrollBarUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import model.Driver;
import model.QualifyingResult;
import model.RaceResult;

/**
 * Card that shows every driver of a session, the starting grid
 * or the race result. The list scrolls when it doesn't fit.
 */
class ResultsBoard extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel title = RaceStyle.label("", Font.BOLD, 18, RaceStyle.TEXT);
    private final JLabel subtitle = RaceStyle.label("", Font.PLAIN, 13, RaceStyle.MUTED);
    private final JPanel rows = new JPanel();

    private final PortraitCache portraits = new PortraitCache();
    private final Map<Integer, String> teamNames;

    ResultsBoard(Map<Integer, String> teamNames) {
        this.teamNames = teamNames;

        setLayout(new BorderLayout());
        setBackground(RaceStyle.CARD);
        setBorder(BorderFactory.createLineBorder(RaceStyle.LINE));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        // The rows go into a scroll pane: 22 drivers don't fit on the
        // screen, so the list scrolls up and down (never sideways).
        JScrollPane scroll = new JScrollPane(rows,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // Transparent, so the card colour shows through; no extra border.
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Same thin dark scroll bar as the Drivers and Standings screens.
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new DarkScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0)); // 10 px wide
        bar.setUnitIncrement(16); // mouse wheel speed
        add(scroll, BorderLayout.CENTER);
    }

    /** Shows a message instead of a ranking. */
    void showMessage(String heading, String message) {
        title.setText(heading);
        subtitle.setText("");
        rows.removeAll();
        JLabel text = RaceStyle.label(message, Font.PLAIN, 15, RaceStyle.MUTED);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        text.setAlignmentX(CENTER_ALIGNMENT);
        rows.add(Box.createVerticalStrut(60));
        rows.add(text);
        refresh();
    }

    /** Shows all results; the leader gets a lap time, the others a gap. */
    void showResults(String heading, String note, List<QualifyingResult> results) {
        title.setText(heading);
        subtitle.setText(note);
        rows.removeAll();

        if (results.isEmpty()) {
            showMessage(heading, "No results yet.");
            return;
        }

        double best = results.get(0).getLapTimeSeconds();
        // The list is sorted fastest first, so row i is position i + 1.
        for (int i = 0; i < results.size(); i++) {
            QualifyingResult result = results.get(i);
            double lap = result.getLapTimeSeconds();
            String time = i == 0 ? TimeFormat.lap(lap) : TimeFormat.gap(lap - best);
            addRow(i + 1, result.getDriver(), time);
        }
        rows.add(Box.createVerticalGlue());
        refresh();
    }

    /** Shows the race classification with the points each driver scored. */
    void showRaceResults(String heading, String note, List<RaceResult> results,
            Map<Integer, Integer> pointsByDriverId) {
        title.setText(heading);
        subtitle.setText(note);
        rows.removeAll();

        // Instead of a lap time, the right column shows the points scored
        // (only the top 10 score, everybody else gets 0).
        for (RaceResult result : results) {
            Integer points = pointsByDriverId.get(
                    Integer.valueOf(result.getDriver().getDriverId()));
            addRow(result.getFinishingPosition(), result.getDriver(),
                    (points == null ? 0 : points.intValue()) + " pts");
        }
        rows.add(Box.createVerticalGlue());
        refresh();
    }

    /** Adds one row: position, portrait, driver, team and the text on the right. */
    private void addRow(int position, Driver driver, String time) {
        String team = teamNames.get(Integer.valueOf(driver.getTeamId()));
        ResultRow row = new ResultRow(position, driver.getName(),
                team == null ? "" : team, portraits.get(driver), time);
        // Keep rows at their normal height when there are only a few.
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        rows.add(row);
    }

    private void refresh() {
        rows.revalidate();
        rows.repaint();
    }
}