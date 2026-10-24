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
 *
 * There is one button for every entry of the Section enum, so adding
 * a new Section automatically adds a new button here.
 */
class SideBar extends JPanel {

    private static final long serialVersionUID = 1L;

    // Who to tell when a button is clicked (the main window).
    private final Navigator navigator;
    // One button per Section; buttons[section.ordinal()] is the button of that section.
    private final JButton[] buttons = new JButton[Section.values().length];
    // The screen that is shown at the moment (its button is red).
    private Section current = Section.HOME;

    SideBar(Navigator navigator) {
        this.navigator = navigator;

        // BoxLayout Y_AXIS: everything is placed under each other.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.SIDEBAR_BG);
        // 210 px wide; the height (100) does not matter, the window stretches it.
        setPreferredSize(new Dimension(210, 100));
        // Thin line on the right edge, between the sidebar and the screen.
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.LINE));

        // A strut is fixed empty space; here 16 px above the first button
        // and 4 px between buttons.
        add(Box.createVerticalStrut(16));
        for (Section section : Section.values()) {
            JButton button = createButton(section);
            buttons[section.ordinal()] = button;
            add(button);
            add(Box.createVerticalStrut(4));
        }
        // Glue is empty space that grows, so the slogan is pushed to the bottom.
        add(Box.createVerticalGlue());

        // The slogan at the bottom; <br> in HTML makes the line breaks.
        JLabel slogan = Theme.label(
                "<html>FASTER.<br>SMARTER.<br>TOGETHER.</html>",
                Font.BOLD, 12, Theme.MUTED);
        slogan.setAlignmentX(Component.LEFT_ALIGNMENT);
        slogan.setBorder(BorderFactory.createEmptyBorder(0, 22, 24, 0));
        add(slogan);

        // Give every button its starting colours and icon.
        refresh();
    }

    /** Called by the window when the screen changes. */
    void setCurrent(Section section) {
        current = section;
        refresh();
    }

    /** One flat, full-width button with an icon and the section's name. */
    private JButton createButton(final Section section) {
        final JButton button = new JButton(section.label);
        // BasicButtonUI removes the look of the operating system,
        // so the button is a plain flat rectangle we can colour ourselves.
        button.setUI(new BasicButtonUI());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(16); // space between icon and text
        button.setFont(Theme.font(Font.PLAIN, 15));
        button.setOpaque(true);
        button.setFocusPainted(false); // no dotted box around the text after a click
        button.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 10));
        // 52 px high; the maximum width lets BoxLayout stretch it over the whole sidebar.
        button.setPreferredSize(new Dimension(210, 52));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect: a lighter background while the mouse is over the button
        // (not for the current screen, that one stays red).
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
        // Click: ask the window to show this screen.
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
            // The icon is drawn again in the right colour each time.
            button.setIcon(Icons.makeIcon(section.icon,
                    selected ? Color.WHITE : Theme.MUTED));
        }
    }
}
