package gui.Common;

import gui.DriversMenu.DarkScrollBarUI;
import gui.MainMenu.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

/**
 * A card with a heading on top and a scrolling list below.
 * Used by the Standings and the Results screens.
 *
 * It only knows how to show rows or a message; what is in the rows is
 * decided by the classes that use it (usually by extending it).
 */
public class ListCard extends JPanel {

    private static final long serialVersionUID = 1L;

    // Rows are placed under each other in here.
    private final JPanel rows = new JPanel();
    private final JScrollPane scroll;

    public ListCard(String title) {
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        // The rows go into a holder at the top (NORTH), so a short list
        // stays at the top instead of being stretched over the whole card.
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(rows, BorderLayout.NORTH);

        // Scrolls up and down only, with the dark scroll bar of the Drivers screen.
        scroll = new JScrollPane(holder,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new DarkScrollBarUI());
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(16); // scroll speed of the mouse wheel

        JLabel heading = Theme.label(title, Font.BOLD, 18, Theme.TEXT);
        heading.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // The card itself: heading on top (NORTH), list in the rest (CENTER).
        setLayout(new BorderLayout());
        setBackground(Theme.TILE_BG);
        setBorder(BorderFactory.createLineBorder(Theme.LINE));
        add(heading, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /** Replaces the list with one grey message (e.g. "Loading..."). */
    public void showMessage(String message) {
        JLabel label = Theme.label(message, Font.PLAIN, 15, Theme.MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(40, 18, 0, 0));

        rows.removeAll();
        rows.add(label);
        refresh();
    }

    /** Replaces the list with these rows, or with emptyMessage if there are none. */
    public void showRows(List<? extends JComponent> newRows, String emptyMessage) {
        if (newRows.isEmpty()) {
            showMessage(emptyMessage);
            return;
        }
        rows.removeAll();
        for (JComponent row : newRows) {
            rows.add(row);
        }
        refresh();
    }

    /** Swing has to be told to lay out and repaint; then scroll back to the top. */
    private void refresh() {
        rows.revalidate();
        rows.repaint();
        // invokeLater: only after the new layout is done is the new
        // scroll range known, so the scroll waits until then.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scroll.getVerticalScrollBar().setValue(0);
            }
        });
    }
}
