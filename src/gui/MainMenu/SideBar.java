package gui.MainMenu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Left navigation bar. The button of the current screen is red.
 */
class SideBar extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Navigator navigator;
    private final JButton[] buttons = new JButton[Section.values().length];
    private Section current = Section.HOME;

    SideBar(Navigator navigator) {
        this.navigator = navigator;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.SIDEBAR_BG);
        setPreferredSize(new Dimension(210, 100));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.LINE));

        add(Box.createVerticalStrut(16));
        for (Section section : Section.values()) {
            JButton button = createButton(section);
            buttons[section.ordinal()] = button;
            add(button);
            add(Box.createVerticalStrut(4));
        }
        add(Box.createVerticalGlue());

        JLabel slogan = Theme.label(
                "<html>FASTER.<br>SMARTER.<br>TOGETHER.</html>",
                Font.BOLD, 12, Theme.MUTED);
        slogan.setAlignmentX(Component.LEFT_ALIGNMENT);
        slogan.setBorder(BorderFactory.createEmptyBorder(0, 22, 24, 0));
        add(slogan);

        refresh();
    }

    /** Called by the window when the screen changes. */
    void setCurrent(Section section) {
        current = section;
        refresh();
    }

    private JButton createButton(final Section section) {
        final JButton button = new JButton(section.label);
        button.setUI(new BasicButtonUI());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(16);
        button.setFont(Theme.font(Font.PLAIN, 15));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 10));
        button.setPreferredSize(new Dimension(210, 52));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (section != current) {
                    button.setBackground(Theme.NAV_HOVER);
                }
            }

            public void mouseExited(MouseEvent e) {
                refresh();
            }
        });
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                navigator.showSection(section);
            }
        });
        return button;
    }

    /** Active button: red with a white icon. Others: plain. */
    private void refresh() {
        for (Section section : Section.values()) {
            JButton button = buttons[section.ordinal()];
            boolean selected = (section == current);
            button.setBackground(selected ? Theme.F1_RED : Theme.SIDEBAR_BG);
            button.setForeground(selected ? Color.WHITE : Theme.TEXT);
            button.setIcon(Icons.makeIcon(section.icon,
                    selected ? Color.WHITE : Theme.MUTED));
        }
    }
}