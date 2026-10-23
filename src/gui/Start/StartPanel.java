package gui.Start;

import gui.MainMenu.Theme;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * First screen: picture, "F1 2026", tagline and a red START button.
 */
public class StartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public StartPanel(final IntroListener listener) {
        super(new GridBagLayout());
        setBackground(Theme.HOME_BG);

        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 18),
                Theme.SIDEBAR_BG, Theme.LINE, 16);
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setPreferredSize(new Dimension(780, 600));
        card.setMinimumSize(new Dimension(560, 440));

        JLabel title = Theme.label("F1 2026", Font.BOLD, 32, Theme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = Theme.label("YOUR RACE. YOUR CAREER.",
                Font.PLAIN, 15, Theme.MUTED);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        ActionButton start = new ActionButton("START", Theme.F1_RED);
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listener.onStart();
            }
        });

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(title);
        info.add(Box.createVerticalStrut(6));
        info.add(tagline);
        info.add(Box.createVerticalStrut(18));
        info.add(start);

        card.add(new ArtPanel(), BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);
        add(card);
    }
}