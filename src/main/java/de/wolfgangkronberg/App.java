package de.wolfgangkronberg;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

/**
 * This is the single JavaFX application itself. All initializations and display originates from here.
 */
public class App extends Application {

    private final GlobalElements ge = new GlobalElements();
    private final KeyEventHandler keyEventHandler = new KeyEventHandler(ge);

    private String currentPictureName = null;

    @Override
    public void start(Stage stage) {
        setCommandlineParams();
        stage.setTitle("ksPhotoCenter");
        ge.setMainPane(createMainPane(stage));
        ge.setImagePane(createImagePane());
        ge.setImagePaneMessage(new TimedMessage(Pos.BOTTOM_RIGHT));
        ge.getImagePane().getChildren().add(ge.getImagePaneMessage().getRoot());
        ge.getNavigator().init(currentPictureName);
    }

    @Override
    public void stop() {
        ge.getProps().saveSelectedOnExit();
    }

    private Pane createMainPane(Stage stage) {
        AppProperties props = ge.getProps();
        StackPane result = new StackPane();
        result.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(result, props.getWidth(), props.getHeight());
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.show();
        return result;
    }

    private Pane createImagePane() {
        Pane main = ge.getMainPane();
        StackPane result = new StackPane();
        main.getChildren().setAll(result);
        Label imagePlaceholder = new Label("");
        result.getChildren().add(imagePlaceholder);
        return result;
    }

    private void setCommandlineParams() {
        Parameters parameters = getParameters();
        ge.getProps().loadParameters(parameters.getNamed());
        List<String> unnamed = parameters.getUnnamed();
        if (!unnamed.isEmpty()) {
            currentPictureName = unnamed.get(0);
            if (unnamed.size() > 1) {
                System.err.println("Ignoring " + (unnamed.size() - 1) + " superfluous image file parameters");
            }
        }
    }

}
