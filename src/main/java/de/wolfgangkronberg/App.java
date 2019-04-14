package de.wolfgangkronberg;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

/**
 * This is the single JavaFX application itself. All initializations and diyplay originates from here.
 */
public class App extends Application {

    private final AppProperties props = new AppProperties();
    private final Navigator navigator = new Navigator();
    private final KeyEventHandler keyEventHandler = new KeyEventHandler(navigator);

    private String currentPictureName = null;

    @Override
    public void start(Stage stage) {
        setCommandlineParams();
        StackPane pane = new StackPane();
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(pane, 640, 480);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.show();
        navigator.init(props, stage, pane, currentPictureName);
    }

    private void setCommandlineParams() {
        Parameters parameters = getParameters();
        props.loadParameters(parameters.getNamed());
        List<String> unnamed = parameters.getUnnamed();
        if (!unnamed.isEmpty()) {
            currentPictureName = unnamed.get(0);
            if (unnamed.size() > 1) {
                System.err.println("Ignoring " + (unnamed.size() - 1) + " superfluous image file parameters");
            }
        }
    }

}
