package gui.TeamsMenu;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TeamsPanel extends JPanel {

    public TeamsPanel() {
        buildInterface();
    }

    private void buildInterface() {
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Teams dashboard", SwingConstants.CENTER);
        label.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
    }
}
