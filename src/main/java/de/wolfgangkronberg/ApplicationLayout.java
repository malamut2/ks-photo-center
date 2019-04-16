package de.wolfgangkronberg;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class ApplicationLayout {

    private final GlobalElements ge;
    private State state;

    /**
     * The standard application pane, with all the controls on the right and left side
     */
    private final BorderPane controlPane;

    /**
     * The center area of the control pane. Contains the thumbnailPane, the imagePane, or nothing.
     */
    private final StackPane centralPane;

    /**
     * Displays image thumbnails
     */
    private final FlowPane thumbnailPane;


    public enum State {
        thumbnail, imageWithControls, imageOnly
    }

    public ApplicationLayout(GlobalElements ge, State initialState) {
        this.ge = ge;
        this.state = initialState;
        Pane main = ge.getMainPane();
        centralPane = createCentralPane();
        controlPane = new BorderPane();
        controlPane.setLeft(createLeftPane());
        controlPane.setRight(createRightPane());
        thumbnailPane = new FlowPane();
        ge.setCentralPaneMessage(new TimedMessage(Pos.CENTER));
        centralPane.getChildren().add(ge.getCentralPaneMessage().getRoot());
        controlPane.setCenter(centralPane);

        switch (initialState) {
            case thumbnail:
                centralPane.getChildren().set(0, thumbnailPane);
                main.getChildren().setAll(centralPane);
                break;
            case imageWithControls:
                centralPane.getChildren().set(0, ge.getImagePane());
                main.getChildren().setAll(centralPane);
                break;
            case imageOnly:
                main.getChildren().setAll(ge.getImagePane());
                break;
        }

    }

    private StackPane createCentralPane() {
        StackPane result = new StackPane();
        Label contentPlaceholder = new Label("");
        result.getChildren().add(contentPlaceholder);
        return result;
    }

    private Pane createLeftPane() {
        VBox result = new VBox();
        result.getChildren().addAll(
                new Label("Future Feature: Library"),
                new Label("Future feature: Collections"),
                new Label("Future Feature: Filesystem")
        );
        return result;
    }

    private Pane createRightPane() {
        VBox result = new VBox();
        result.getChildren().addAll(
                new Label("Future Feature: Metadata"),
                new Label("Future feature: Edit Controls"),
                new Label("Future Feature: Search & Print")
        );
        return result;
    }

    /**
     * Performs the action of a press of the enter key. That means:
     * In thumbnail state, imageWithControls state is activated if we have any selected thumbnail.
     * In imageWithControls state, imageOnly state is activated.
     * Otherwise, no action is being taken.
     *
     * @return false if imageWithControlsState could not be activated due to no thumbnail being selected. Returns
     * true in all other cases.
     */
    public boolean enter() {
        switch (state) {
            case thumbnail:
                if (ge.getCurrentImage() == null) {
                    return false;
                }
                centralPane.getChildren().set(0, ge.getImagePane());
                state = State.imageWithControls;
                break;
            case imageWithControls:
                centralPane.getChildren().set(0, thumbnailPane);  // remove imagePane from here
                ge.getMainPane().getChildren().set(0, ge.getImagePane());
                state = State.imageOnly;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Performs the action of a press of the escape key. That means:
     * In imageWithControls state, thumbnail state is activated.
     * In imageOnly state, imageWithControls state is activated.
     * Otherwise, no action is being taken.
     */
    public void escape() {
        switch (state) {
            case imageOnly:
                ge.getMainPane().getChildren().set(0, controlPane);
                centralPane.getChildren().set(0, ge.getImagePane());
                state = State.imageWithControls;
                break;
            case imageWithControls:
                centralPane.getChildren().set(0, thumbnailPane);
                state = State.thumbnail;
                break;
            default:
                break;
        }
    }

}
