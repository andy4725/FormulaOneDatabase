package gui.StandingsMenu;

import gui.Common.Medals;
import gui.Common.PointsFormat;
import gui.MainMenu.Theme;
import gui.RaceMenu.Avatar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * One line of a championship list:
 *   [position] [portrait] [name / team underneath] ......... [points]
 *
 * Used for drivers and for teams; the team list simply has no portrait
 * and no second line. P1, P2 and P3 get a tinted background and a
 * coloured stripe on the left.
 */
class StandingRow extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * @param avatar may be null (team rows have no portrait)
     * @param team   second line under the name; null for no second line
     */
    StandingRow(int position, Avatar avatar, String name, String team, double points) {
        // BorderLayout: left part (WEST), name in the middle (CENTER), points (EAST).
        super(new BorderLayout(14, 0));
        Color medal = Medals.forPosition(position); // null below P3
        boolean podium = medal != null;

        if (podium) {
            // Medal colour mixed into the card colour (12%) as background,
            // plus a 4 px stripe on the left in the full medal colour.
            setOpaque(true);
            setBackground(Theme.blend(Theme.TILE_BG, medal, 0.12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 4, 0, 0, medal),
                    BorderFactory.createEmptyBorder(12, 14, 12, 18)));
        } else {
            // Normal row: transparent, thin line on top to separate rows.
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        }

        // Text colour: medal colour on the podium, white for the others.
        Color highlight = podium ? medal : Theme.TEXT;

        add(left(position, avatar, podium, highlight), BorderLayout.WEST);
        add(middle(name, team, podium), BorderLayout.CENTER);
        add(Theme.label(PointsFormat.format(points) + " pts", Font.BOLD,
                podium ? 17 : 15, highlight), BorderLayout.EAST);

        // Stop BoxLayout from making rows taller than they need to be.
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }

    /** Position number and (optional) portrait, next to each other. */
    private static JPanel left(int position, Avatar avatar, boolean podium, Color highlight) {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JLabel pos = Theme.label(String.valueOf(position), Font.BOLD,
                podium ? 22 : 18, highlight);
        // Fixed width so all names start at the same place. Rows with a
        // portrait are as tall as the portrait (44 px), team rows are lower.
        pos.setPreferredSize(new Dimension(32, avatar != null ? 44 : 26));
        pos.setMinimumSize(pos.getPreferredSize());
        pos.setMaximumSize(pos.getPreferredSize());
        left.add(pos);

        if (avatar != null) {
            left.add(avatar);
        }
        return left;
    }

    /** The name, with the team in grey underneath when there is one. */
    private static JPanel middle(String name, String team, boolean podium) {
        JPanel middle = new JPanel(new GridLayout(team == null ? 1 : 2, 1));
        middle.setOpaque(false);
        middle.add(Theme.label(name, Font.BOLD, podium ? 17 : 15, Theme.TEXT));
        if (team != null) {
            middle.add(Theme.label(team, Font.PLAIN, 13, Theme.MUTED));
        }
        return middle;
    }
}
