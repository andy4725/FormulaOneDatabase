package gui.Start;

import gui.MainMenu.Theme;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Second screen: "SELECT SLOT" with three save slots and a Back button.
 */
public class SlotPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** listener (the main window) is told when a slot or Back is clicked. */
    public SlotPanel(final IntroListener listener) {
        // GridBagLayout with one child keeps that child centred in the window.
        super(new GridBagLayout());
        setBackground(Theme.HOME_BG);

        // Box with the title and the row of slots.
        RoundedPanel box = new RoundedPanel(new BorderLayout(0, 16),
                Theme.SIDEBAR_BG, Theme.LINE, 16);
        box.setBorder(new EmptyBorder(24, 24, 24, 24));
        box.setPreferredSize(new Dimension(840, 230));
        box.setMinimumSize(new Dimension(640, 230));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = Theme.label("SELECT SLOT", Font.BOLD, 20, Theme.TEXT);

        // One row of equally wide slot cards, 16 px apart. Each card is
        // told whether its slot is used and what status text to show.
        JPanel row = new JPanel(new GridLayout(1, SaveSlots.COUNT, 16, 0));
        row.setOpaque(false);
        for (int slot = 1; slot <= SaveSlots.COUNT; slot++) {
            row.add(new SlotCard(slot, SaveSlots.isUsed(slot),
                    SaveSlots.status(slot), listener));
        }

        box.add(title, BorderLayout.NORTH);
        box.add(row, BorderLayout.CENTER);

        // Back button under the box.
        ActionButton back = new ActionButton("\u2190 Back",
                new Color(40, 44, 54));
        back.setPreferredSize(new Dimension(160, 44));
        back.setMaximumSize(new Dimension(160, 44));
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listener.onBack();
            }
        });

        // The box and the Back button under each other, 20 px apart.
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(box);
        column.add(Box.createVerticalStrut(20));
        column.add(back);

        add(column);
    }
}