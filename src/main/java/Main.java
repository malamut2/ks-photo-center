import de.wolfgangkronberg.kspc.App;
import javafx.application.Application;

/**
 * Application main entry point, actually a thin wrapper for de.wolfgangkronberg.App
 */
public class Main {

    public static void main(String[] args) {
        reportExceptions();
        Application.launch(App.class, args);
    }

    private static void reportExceptions() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("Uncaught exception occurred: " + e.toString());
                e.printStackTrace();
            }
        });
    }

}
