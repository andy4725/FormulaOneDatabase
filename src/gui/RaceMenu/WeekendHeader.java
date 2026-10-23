package gui.RaceMenu;

import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Circuit;
import model.WeekendEvent;

/** Top of the screen: round number, Grand Prix name, circuit and country. */
class WeekendHeader extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel roundLabel = RaceStyle.label(" ", Font.BOLD, 13, RaceStyle.MUTED);
    private final JLabel eventLabel = RaceStyle.label("Loading...", Font.BOLD, 26, RaceStyle.TEXT);
    private final JLabel circuitLabel = RaceStyle.label(" ", Font.PLAIN, 14, RaceStyle.MUTED);

    WeekendHeader() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(roundLabel);
        add(Box.createVerticalStrut(2));
        add(eventLabel);
        add(Box.createVerticalStrut(2));
        add(circuitLabel);
    }

    void show(WeekendEvent event, Circuit circuit) {
        roundLabel.setText("ROUND " + event.getRoundNumber());
        eventLabel.setText(event.getGrandPrixName());
        circuitLabel.setText(circuit == null ? " "
                : circuit.getName() + "  ·  " + circuit.getCountry());
    }

    void showNoWeekend() {
        roundLabel.setText(" ");
        eventLabel.setText("No race weekend to play");
        circuitLabel.setText("Start a season first, or every race is already finished.");
    }
}