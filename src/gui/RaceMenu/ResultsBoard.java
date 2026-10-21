package gui.RaceMenu;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.QualifyingResult;

/**
 * Card that shows the top 5 of a session (or the starting grid).
 */
class ResultsBoard extends JPanel {

    private static final long serialVersionUID = 1L;
    static final int SHOWN = 5;

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
        add(rows, BorderLayout.CENTER);
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

    /** Shows the first 5 results; the leader gets a lap time, the others a gap. */
    void showResults(String heading, String note, List<QualifyingResult> results) {
        title.setText(heading);
        subtitle.setText(note);
        rows.removeAll();

        if (results.isEmpty()) {
            showMessage(heading, "No results yet.");
            return;
        }

        double best = results.get(0).getLapTimeSeconds();
        int count = Math.min(SHOWN, results.size());
        for (int i = 0; i < count; i++) {
            QualifyingResult result = results.get(i);
            double lap = result.getLapTimeSeconds();
            String time = i == 0 ? TimeFormat.lap(lap) : TimeFormat.gap(lap - best);

            String team = teamNames.get(Integer.valueOf(result.getDriver().getTeamId()));
            rows.add(new ResultRow(i + 1, result.getDriver().getName(),
                    team == null ? "" : team,
                    portraits.get(result.getDriver()), time));
        }
        rows.add(Box.createVerticalGlue());
        refresh();
    }

    private void refresh() {
        rows.revalidate();
        rows.repaint();
    }
}