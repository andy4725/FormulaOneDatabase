
package app;

import gui.MainWindow;
import java.sql.SQLException;
import javax.swing.SwingUtilities;
import model.SimulationData;
import service.SimulationDataLoader;

public class App {

    public static void main(String[] args) throws SQLException {

        SimulationData data =
                new SimulationDataLoader().load();

        if (data.getTeams().isEmpty()) {
            System.out.println(
                    "No teams found in the database.");
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                MainWindow window = new MainWindow(data);
                window.setVisible(true);
            }
        });
    }
}