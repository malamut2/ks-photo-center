package de.wolfgangkronberg.kspc;

import de.wolfgangkronberg.kspc.files.Navigator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
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
        AppProperties props = ge.getProps();
        ge.setNavigator(new Navigator(ge));
        File current = currentPictureName == null ? null : new File(currentPictureName);
        ge.setCurrentImage(current);
        ge.setImageCache(new GroupedCacheLoader<>((f) -> new ImageWithMetadata(ge, f),
                3, props.getNumCacheShownImages()));
        stage.setTitle("ksPhotoCenter");
        stage.setMinWidth(100 + ge.getProps().getLeftSidePaneWidth() + ge.getProps().getRightSidePaneWidth());
        stage.setMinHeight(100);
        ge.setStage(stage);
        ge.setMainPane(createMainPane(stage));
        ge.setImagePane(createImagePane());
        ge.setImagePaneMessage(new TimedMessage(Pos.BOTTOM_RIGHT));
        ge.getImagePane().getChildren().add(ge.getImagePaneMessage().getRoot());
        ge.setApplicationLayout(createApplicationLayout());
        ge.getNavigator().init();
        stage.show();
        stage.toFront();
        stage.requestFocus();
    }

    private ApplicationLayout createApplicationLayout() {
        ApplicationLayout.State state = ge.getCurrentImage() == null ?
                ApplicationLayout.State.thumbnail : ApplicationLayout.State.imageOnly;
        return new ApplicationLayout(ge, state);
    }

    @Override
    public void stop() {
        ge.getProps().saveSelectedOnExit();
    }

    private Pane createMainPane(Stage stage) {
        AppProperties props = ge.getProps();
        AnchorPane result = new AnchorPane();
        result.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(result, props.getWidth(), props.getHeight());
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        return result;
    }

    private Pane createImagePane() {
        StackPane result = new StackPane();
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

    public GlobalElements getGlobalElements() {
        return ge;
    }
}
