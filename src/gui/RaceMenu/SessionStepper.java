package gui.RaceMenu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * The four sessions of a weekend in a row:
 * Quali 1, Quali 2, Quali 3 and Race.
 * Finished sessions get a red top bar, the next one a red outline,
 * the others are shown as locked.
 */
class SessionStepper extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String[] NAMES = { "Quali 1", "Quali 2", "Quali 3", "Race" };

    private final StepBox[] boxes = new StepBox[NAMES.length];

    SessionStepper() {
        setOpaque(false);
        setLayout(new GridLayout(1, NAMES.length, 12, 0));
        for (int i = 0; i < NAMES.length; i++) {
            boxes[i] = new StepBox(NAMES[i]);
            add(boxes[i]);
        }
        setProgress(0, false);
    }

    /** sessionsDone: 0 to 3 finished qualifying sessions. */
    void setProgress(int sessionsDone, boolean raceDone) {
        for (int i = 0; i < 3; i++) {
            if (i < sessionsDone) {
                boxes[i].setState(StepBox.FINISHED);
            } else if (i == sessionsDone) {
                boxes[i].setState(StepBox.NEXT);
            } else {
                boxes[i].setState(StepBox.LOCKED);
            }
        }
        boxes[3].setState(raceDone ? StepBox.FINISHED
                : sessionsDone >= 3 ? StepBox.NEXT : StepBox.LOCKED);
        repaint();
    }

    /** One painted box of the stepper. */
    private static class StepBox extends JComponent {

        private static final long serialVersionUID = 1L;

        static final int FINISHED = 0;
        static final int NEXT = 1;
        static final int LOCKED = 2;

        private final String name;
        private int state = LOCKED;

        StepBox(String name) {
            this.name = name;
            setPreferredSize(new Dimension(150, 64));
        }

        void setState(int state) {
            this.state = state;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            // Work on a copy of the Graphics (the stroke changes below must
            // not affect other components) and always release it in finally.
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                RaceStyle.enableQuality(g);
                int w = getWidth();
                int h = getHeight();

                Color fill = state == LOCKED ? RaceStyle.CARD_DARK : RaceStyle.CARD;
                g.setColor(fill);
                g.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

                if (state == FINISHED) {
                    // Red bar along the top: rounded top corners, square bottom.
                    g.setColor(RaceStyle.RED);
                    g.fillRoundRect(0, 0, w - 1, 6, 6, 6);
                    g.fillRect(0, 3, w - 1, 3);
                } else if (state == NEXT) {
                    // Thick red outline around the whole box.
                    g.setColor(RaceStyle.RED);
                    g.setStroke(new BasicStroke(2f));
                    g.drawRoundRect(1, 1, w - 3, h - 3, 12, 12);
                } else {
                    // Dashed grey outline: 5px line, 4px gap.
                    g.setColor(RaceStyle.LINE);
                    g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND, 10f, new float[] { 5f, 4f }, 0f));
                    g.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                }

                String tag = state == FINISHED ? "FINISHED" : state == NEXT ? "UP NEXT" : "LOCKED";
                g.setFont(RaceStyle.font(Font.BOLD, 11));
                g.setColor(RaceStyle.MUTED);
                g.drawString(tag, 16, 26);

                g.setFont(RaceStyle.font(Font.BOLD, 18));
                g.setColor(state == LOCKED ? RaceStyle.MUTED : RaceStyle.TEXT);
                g.drawString(name, 16, 48);
            } finally {
                g.dispose();
            }
        }
    }
}