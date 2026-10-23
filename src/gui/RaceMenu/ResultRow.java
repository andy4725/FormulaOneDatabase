package gui.RaceMenu;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * One line of the ranking: position, round portrait, driver, team, time.
 */
class ResultRow extends JPanel {

    private static final long serialVersionUID = 1L;

    ResultRow(int position, String driverName, String teamName,
            BufferedImage portrait, String time) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RaceStyle.LINE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 14);

        JLabel pos = RaceStyle.label(String.valueOf(position), Font.BOLD, 22,
                position == 1 ? RaceStyle.RED : RaceStyle.TEXT);
        pos.setPreferredSize(new Dimension(36, 30));
        c.gridx = 0;
        add(pos, c);

        c.gridx = 1;
        add(new Avatar(portrait, driverName, 44), c);

        // Same tiny preferred width for name and team, so both columns get
        // an equal share of the space and line up from row to row.
        c.gridx = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel name = RaceStyle.label(driverName, Font.BOLD, 17, RaceStyle.TEXT);
        name.setPreferredSize(new Dimension(10, 30));
        add(name, c);

        c.gridx = 3;
        JLabel team = RaceStyle.label(teamName, Font.PLAIN, 15, RaceStyle.MUTED);
        team.setPreferredSize(new Dimension(10, 30));
        add(team, c);

        c.gridx = 4;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 0, 0, 0);
        JLabel timeLabel = RaceStyle.label(time, Font.BOLD, 17, RaceStyle.TEXT);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setPreferredSize(new Dimension(110, 30));
        add(timeLabel, c);
    }
}
