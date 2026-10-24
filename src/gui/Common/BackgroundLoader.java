package gui.Common;

import java.util.concurrent.Callable;

import javax.swing.SwingWorker;

/**
 * Runs database work off the Swing thread and hands the result back on it.
 *
 * Doing database work directly on the Swing thread would freeze the
 * window until it is finished, so it runs in a SwingWorker instead.
 *
 * Only the newest load counts: when a new load starts, or cancel() is
 * called, an older load that finishes later is thrown away. Without that,
 * a slow load could overwrite what the player clicked afterwards.
 */
public final class BackgroundLoader {

    /** What to do with the result; both methods run on the Swing thread. */
    public interface Callback<T> {
        void loaded(T result);

        void failed(Exception error);
    }

    // Number of the newest load; every load and every cancel() raises it.
    private int current = 0;

    /** Runs task in the background, then gives its result to callback. */
    public <T> void load(final Callable<T> task, final Callback<T> callback) {
        final int number = ++current;

        new SwingWorker<T, Void>() {
            // Runs on a background thread: must not touch the window.
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            // Runs on the Swing thread afterwards: may change the window.
            @Override
            protected void done() {
                if (number != current) {
                    return; // an outdated load
                }
                // get() gives back what doInBackground() returned,
                // or throws the error that happened there.
                T result;
                try {
                    result = get();
                } catch (Exception e) {
                    callback.failed(e);
                    return;
                }
                callback.loaded(result);
            }
        }.execute();
    }

    /** Makes every load that is still running outdated. */
    public void cancel() {
        current++;
    }
}
