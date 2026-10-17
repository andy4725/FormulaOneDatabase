package gui.MainMenu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Bar at the bottom of the home screen: season, next race and a quote.
 */
class FooterBar extends JPanel {

    private static final long serialVersionUID = 1L;

    FooterBar(String season, String nextRace) {
        super(new BorderLayout());
        setBackground(Theme.SIDEBAR_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(14, 28, 14, 28)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(Theme.label(season, Font.BOLD, 14, Theme.TEXT));
        left.add(Theme.label("|", Font.PLAIN, 20, Theme.LINE));
        left.add(Theme.label("Next Race:", Font.PLAIN, 13, Theme.MUTED));
        left.add(Theme.label(nextRace, Font.BOLD, 13, Theme.TEXT));

        JLabel quote = Theme.label(
                "<html><div align='right'><i>\u201CThe difference between a good driver"
                + "<br>and a great one is a few tenths.\u201D</i></div></html>",
                Font.PLAIN, 12, Theme.MUTED);

        add(left, BorderLayout.WEST);
        add(quote, BorderLayout.EAST);
    }
}