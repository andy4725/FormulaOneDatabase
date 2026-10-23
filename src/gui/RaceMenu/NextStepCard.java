package gui.RaceMenu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Card on the right: what comes next, the main button
 * (Start Quali 1 / Go to next session / Go to race / Start race / Next weekend)
 * and the "Restart qualifying" button.
 */
class NextStepCard extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel title = RaceStyle.label(" ", Font.BOLD, 20, RaceStyle.TEXT);
    private final JLabel hint = RaceStyle.label(" ", Font.PLAIN, 14, RaceStyle.MUTED);
    private final JButton mainButton = new JButton();
    private final JButton restartButton = new JButton("Restart qualifying");

    NextStepCard(ActionListener onMain, ActionListener onRestart) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(RaceStyle.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RaceStyle.LINE),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel heading = RaceStyle.label("NEXT STEP", Font.BOLD, 12, RaceStyle.MUTED);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleButton(mainButton, RaceStyle.RED, Color.WHITE, 17);
        mainButton.addActionListener(onMain);

        styleButton(restartButton, RaceStyle.CARD, RaceStyle.MUTED, 13);
        restartButton.setBorder(BorderFactory.createLineBorder(RaceStyle.LINE));
        restartButton.addActionListener(onRestart);

        add(heading);
        add(Box.createVerticalStrut(10));
        add(title);
        add(Box.createVerticalStrut(8));
        add(hint);
        add(Box.createVerticalStrut(18));
        add(mainButton);
        add(Box.createVerticalStrut(10));
        add(restartButton);
        add(Box.createVerticalGlue());
    }

    /**
     * Texts and buttons for the current point of the weekend.
     *
     * @param hasEvent     there is a weekend to play
     * @param sessionsDone 0..3 finished quali sessions
     * @param raceStage    "Go to race" was already pressed
     * @param raceDone     the race was simulated and saved
     * @param ready        nothing is running, the buttons can be used
     */
    void update(boolean hasEvent, int sessionsDone, boolean raceStage,
            boolean raceDone, boolean ready) {
        mainButton.setEnabled(ready);
        restartButton.setEnabled(ready && sessionsDone > 0);
        restartButton.setVisible(!raceStage);

        if (!hasEvent) {
            show("Nothing to do", " ", "No weekend");
        } else if (sessionsDone == 0) {
            show("Quali 1", "All drivers set a lap. The top 16 go to Quali 2.",
                    "Start Quali 1");
        } else if (sessionsDone < 3) {
            show(Sessions.name(sessionsDone), sessionsDone == 1
                    ? "The top 16 drivers fight for the 10 places in Quali 3."
                    : "The top 10 drivers fight for pole position.",
                    "Go to next session");
        } else if (!raceStage) {
            show("Race", "Qualifying is finished. The grid is set.", "Go to race");
        } else if (!raceDone) {
            // Grid is shown, the race has not been run yet.
            show("Race", "Start the race. The results are saved for Statistics.",
                    "Start race");
        } else {
            // Race is saved: the button takes the player to the next round.
            show("Race finished", "The results and points are saved.",
                    "Next weekend");
        }
    }

    /** Shows a message on the main button while something runs. */
    void showWorking(String message) {
        mainButton.setText(message);
    }

    private void show(String titleText, String hintText, String buttonText) {
        title.setText(titleText);
        hint.setText(" ".equals(hintText) ? hintText : html(hintText));
        mainButton.setText(buttonText);
    }

    private static void styleButton(JButton button, Color background,
            Color foreground, int size) {
        button.setUI(new BasicButtonUI());
        button.setFont(RaceStyle.font(Font.BOLD, size));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        int height = size > 15 ? 52 : 40;
        button.setPreferredSize(new Dimension(258, height));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    /** Lets a long hint wrap inside the card. */
    private static String html(String text) {
        return "<html><div style='width:190px'>" + text + "</div></html>";
    }
}