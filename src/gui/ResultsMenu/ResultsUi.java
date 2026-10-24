package gui.ResultsMenu;

import gui.MainMenu.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Colours and small Swing building blocks shared by the Results screen. */
final class ResultsUi {

    // F1 timing colours: purple = fastest of the race, green = personal best.
    // (The medal colours are shared with Standings, see gui.Common.Medals.)
    static final Color PURPLE = new Color(176, 102, 255);
    static final Color GREEN  = new Color(46, 204, 113);
    static final Color RED    = new Color(235, 87, 87);

    private ResultsUi() {
        // Only static members; no instances needed.
    }

    /** Transparent list row with a thin line on top. */
    static JPanel baseRow() {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBackground(Theme.NAV_HOVER); // only shown while hovered
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        return row;
    }

    /** Stops BoxLayout from making a row taller than it needs to be. */
    static JPanel finish(JPanel row) {
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Bold first line, grey second line, optional purple badge next to it. */
    static JPanel twoLines(String first, String second, String badge) {
        JPanel lines = new JPanel(new GridLayout(2, 1));
        lines.setOpaque(false);
        lines.add(Theme.label(first, Font.BOLD, 15, Theme.TEXT));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(Theme.label(second, Font.PLAIN, 13, Theme.MUTED));
        if (badge != null) {
            bottom.add(Box.createHorizontalStrut(10));
            bottom.add(Theme.label(badge, Font.BOLD, 11, PURPLE));
        }
        lines.add(bottom);
        return lines;
    }

    /** Hand cursor, hover colour and a click action for a whole row. */
    static void makeClickable(final JPanel row, final Runnable action) {
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setOpaque(true);
                row.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setOpaque(false);
                row.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    action.run();
                }
            }
        });
    }

    /** A label whose text sits at the right edge of its space (for number columns). */
    static JLabel rightAligned(String text, int style, int size, Color color) {
        JLabel label = Theme.label(text, style, size, color);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    /** Gives a component a fixed size, so columns line up from row to row. */
    static <T extends JComponent> T fixed(T component, int width, int height) {
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
        return component;
    }
}
