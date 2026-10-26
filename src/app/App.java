package app;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

import gui.MainMenu.MainWindow;
import model.SimulationData;
import service.SimulationDataLoader;

/** Starts the application after its shared reference data has loaded.
 * The window is created on Swing's event thread so UI work runs safely.
 */
public class App {
    /** Loads app data, reports an empty team database, or opens the main window. */
    public static void main(String[] args) throws SQLException {
        final SimulationData data = new SimulationDataLoader().load();

        if (data.getTeams().isEmpty()) {
            System.out.println("No teams found in the database.");
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainWindow window = new MainWindow(data);
                window.setVisible(true);
            }
        });
    }

}


