import de.wolfgangkronberg.kspc.App;
import javafx.application.Application;

/**
 * Application main entry point, actually a thin wrapper for de.wolfgangkronberg.App
 */
public class Main {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("Uncaught exception occurred: " + e.toString());
                e.printStackTrace();
            }
        });
        Application.launch(App.class, args);
    }

}
