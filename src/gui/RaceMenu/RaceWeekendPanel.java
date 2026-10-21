package gui.RaceMenu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import model.Circuit;
import model.QualifyingResult;
import model.QualifyingSessionOutcome;
import model.SimulationData;
import model.Team;
import model.WeekendEvent;
import repository.QualifyingRepository;
import repository.RaceWeekendRepository;
import service.NextQualifyingSessionService;

/**
 * Race weekend screen.
 *
 * One button walks through the weekend in a fixed order:
 * Quali 1 -> Quali 2 -> Quali 3 -> Race. Sessions can't be skipped.
 * After every quali session the top 5 is shown. The simulation and the
 * saving to the database are done by NextQualifyingSessionService;
 * this screen only starts it and shows what came back.
 *
 * The pieces are in this package:
 * WeekendHeader (top), SessionStepper (the 4 steps), ResultsBoard (top 5),
 * NextStepCard (buttons), WeekendLoader (reads the current weekend).
 */
public class RaceWeekendPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final SimulationData data;
    private final NextQualifyingSessionService qualifying = new NextQualifyingSessionService();
    private final RaceWeekendRepository weekendRepository = new RaceWeekendRepository();
    private final QualifyingRepository qualifyingRepository = new QualifyingRepository();
    private final WeekendLoader loader = new WeekendLoader();

    private final WeekendHeader header = new WeekendHeader();
    private final SessionStepper stepper = new SessionStepper();
    private final ResultsBoard board;
    private final NextStepCard nextStep;

    private int slot;             // save slot chosen by the player, 0 = none yet
    private WeekendEvent event;
    private Circuit circuit;
    private int sessionsDone;     // 0..3 quali sessions shown to the player
    private int sessionsSaved;    // 0..3 quali sessions already in the database
    private boolean raceStage;    // true after "Go to race"
    private boolean busy;         // something is running in the background

    public RaceWeekendPanel(SimulationData data) {
        this.data = data;
        board = new ResultsBoard(teamNames(data));
        nextStep = new NextStepCard(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        onMainButton();
                    }
                },
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        onRestart();
                    }
                });

        setLayout(new BorderLayout(0, 18));
        setBackground(RaceStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(stepper, BorderLayout.CENTER);

        JPanel side = new JPanel(new BorderLayout());
        side.setOpaque(false);
        side.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        side.setPreferredSize(new Dimension(300, 100));
        side.add(nextStep, BorderLayout.NORTH);

        add(top, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);
        add(side, BorderLayout.EAST);

        applyLoadedWeekend(new WeekendLoader.LoadedWeekend());
        setBusy(false, null);
    }

    /** Called when the player picks a save slot: shows that slot's weekend. */
    public void showSlot(int slot) {
        this.slot = slot;
        loadWeekend();
    }

    private static Map<Integer, String> teamNames(SimulationData data) {
        Map<Integer, String> names = new HashMap<Integer, String>();
        for (Team team : data.getTeams()) {
            names.put(Integer.valueOf(team.getTeamId()), team.getTeamName());
        }
        return names;
    }

    // =====================================================
    // Button actions
    // =====================================================

    private void onMainButton() {
        if (busy || event == null) {
            return;
        }
        if (sessionsDone < sessionsSaved) {
            showSavedSession();
        } else if (sessionsDone < 3) {
            simulateNextSession();
        } else if (!raceStage) {
            goToRace();
        } else {
            startRace();
        }
    }

    /** Runs the next quali session in the background, then shows its top 5. */
    private void simulateNextSession() {
        setBusy(true, "Simulating " + Sessions.name(sessionsDone) + "...");

        new SwingWorker<QualifyingSessionOutcome, Void>() {
            @Override
            protected QualifyingSessionOutcome doInBackground() throws Exception {
                QualifyingSessionOutcome outcome = qualifying.simulateNextSession(
                        event.getSeasonEventId(), circuit,
                        data.getDrivers(), data.getTeams());
                weekendRepository.updateStatus(event.getSeasonEventId(),
                        outcome.isQualifyingComplete()
                                ? "QUALIFYING_COMPLETE" : "QUALIFYING_IN_PROGRESS");
                return outcome;
            }

            @Override
            protected void done() {
                try {
                    QualifyingSessionOutcome outcome = get();
                    sessionsDone = Sessions.indexOf(outcome.getSession()) + 1;
                    sessionsSaved = sessionsDone;
                    showSessionResults(outcome.getResults());
                } catch (Exception e) {
                    showError("The session could not be simulated.", e);
                }
                setBusy(false, null);
            }
        }.execute();
    }

    /** The next session is already in the database: load and show its top 5. */
    private void showSavedSession() {
        final String session = Sessions.CODES[sessionsDone];
        setBusy(true, "Loading " + Sessions.name(sessionsDone) + "...");

        new SwingWorker<List<QualifyingResult>, Void>() {
            @Override
            protected List<QualifyingResult> doInBackground() throws Exception {
                return qualifyingRepository.loadSessionResults(
                        event.getSeasonEventId(), session, data.getDrivers());
            }

            @Override
            protected void done() {
                try {
                    List<QualifyingResult> results = get();
                    sessionsDone++;
                    showSessionResults(results);
                } catch (Exception e) {
                    showError("The session results could not be loaded.", e);
                }
                setBusy(false, null);
            }
        }.execute();
    }

    /** Qualifying is over: switch to the race and show the starting grid. */
    private void goToRace() {
        setBusy(true, "Loading the starting grid...");

        new SwingWorker<List<QualifyingResult>, Void>() {
            @Override
            protected List<QualifyingResult> doInBackground() throws Exception {
                return weekendRepository.loadStartingGrid(
                        event.getSeasonEventId(), data.getDrivers());
            }

            @Override
            protected void done() {
                try {
                    raceStage = true;
                    board.showResults("Starting grid", "Top " + ResultsBoard.SHOWN
                            + " from qualifying", get());
                } catch (Exception e) {
                    showError("The starting grid could not be loaded.", e);
                }
                setBusy(false, null);
            }
        }.execute();
    }

    private void startRace() {
        // TODO: call the race simulation service here when it exists,
        // e.g. raceService.simulateAndSave(event.getSeasonEventId(), ...),
        // then show its result with board.showResults(...).
        JOptionPane.showMessageDialog(this,
                "The race simulation is not connected yet.",
                "Start race", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Deletes this weekend's quali data so it can be simulated again. */
    private void onRestart() {
        if (busy || event == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete all qualifying results of this weekend and start again from Quali 1?",
                "Restart qualifying", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true, "Restarting qualifying...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                weekendRepository.resetQualifying(event.getSeasonEventId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    showError("Qualifying could not be restarted.", e);
                }
                busy = false;
                loadWeekend();
            }
        }.execute();
    }

    // =====================================================
    // Loading the current weekend
    // =====================================================

    private void loadWeekend() {
        setBusy(true, "Loading...");

        new SwingWorker<WeekendLoader.LoadedWeekend, Void>() {
            @Override
            protected WeekendLoader.LoadedWeekend doInBackground() throws Exception {
                return loader.load(data, slot);
            }

            @Override
            protected void done() {
                WeekendLoader.LoadedWeekend loaded = new WeekendLoader.LoadedWeekend();
                try {
                    loaded = get();
                } catch (Exception e) {
                    showError("The race weekend could not be loaded.", e);
                }
                applyLoadedWeekend(loaded);
                setBusy(false, null);
            }
        }.execute();
    }

    private void applyLoadedWeekend(WeekendLoader.LoadedWeekend loaded) {
        event = loaded.event;
        circuit = loaded.circuit;
        sessionsSaved = loaded.sessionsDone;
        sessionsDone = Math.min(1, sessionsSaved); // start from Quali 1
        raceStage = false;

        if (event == null) {
            header.showNoWeekend();
            board.showMessage("Race weekend", "There is no weekend in progress.");
            return;
        }

        header.show(event, circuit);
        if (loaded.firstResults == null) {
            board.showMessage("Qualifying", "Press \"Start Quali 1\" to begin the weekend.");
        } else {
            showSessionResults(loaded.firstResults);
        }
    }

    // =====================================================
    // Small helpers
    // =====================================================

    private void showSessionResults(List<QualifyingResult> results) {
        board.showResults(Sessions.name(sessionsDone - 1) + " results",
                "Top " + ResultsBoard.SHOWN, results);
    }

    private void setBusy(boolean isBusy, String message) {
        busy = isBusy;
        stepper.setProgress(sessionsDone);
        boolean ready = event != null && circuit != null && !busy;
        nextStep.update(event != null, sessionsDone, raceStage, ready);
        if (isBusy && message != null) {
            nextStep.showWorking(message);
        }
    }

    private void showError(String message, Exception e) {
        Throwable cause = e instanceof ExecutionException && e.getCause() != null
                ? e.getCause() : e;
        cause.printStackTrace();
        JOptionPane.showMessageDialog(this, message + "\n" + cause.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}