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
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Internal error: " + e.toString());
            e.printStackTrace();
        });
    }

}
