package de.wolfgangkronberg.kspc;

import de.wolfgangkronberg.kspc.library.Library;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

public class ApplicationLayout {

    private final GlobalElements ge;
    private State state;

    private final Object lock = new Object();
    private Runnable imagePaneSizeListener = null;

    /**
     * The standard application pane, with all the controls on the right and left side
     */
    private final AnchorPane controlPane;

    /**
     * The center area of the control pane. Contains the thumbnailPane, the imagePane, or nothing.
     */
    private final AnchorPane centralPane;

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
        main.layoutBoundsProperty().addListener((observableValue, bounds, t1) -> {
            Runnable r;
            synchronized (lock) {
                r = imagePaneSizeListener;
            }
            if (r != null) {
                r.run();
            }
        });

        controlPane = new AnchorPane();
        centralPane = createCentralPane();
        thumbnailPane = new FlowPane();
        ge.setCentralPaneMessage(new TimedMessage(Pos.CENTER));
        centralPane.getChildren().add(ge.getCentralPaneMessage().getRoot());
        controlPane.getChildren().addAll(createLeftPane(), centralPane, createRightPane());
        setLeftPaneAnchor();
        controlPane.widthProperty().addListener((a, b, c) -> setLeftPaneAnchor());
        setAnchorToChild(controlPane, 1, 0d, ge.getProps().getRightSidePaneWidth(),
                0d, ge.getProps().getLeftSidePaneWidth());
        setRightPaneAnchor();
        controlPane.widthProperty().addListener((a, b, c) -> setRightPaneAnchor());

        switch (initialState) {
            case thumbnail:
                centralPane.getChildren().set(0, thumbnailPane);
                main.getChildren().setAll(controlPane);
                break;
            case imageWithControls:
                centralPane.getChildren().set(0, ge.getImagePane());
                main.getChildren().setAll(controlPane);
                break;
            case imageOnly:
                main.getChildren().setAll(ge.getImagePane());
                break;
        }

        setMaxAnchorToFirstChild(ge.getMainPane());
        setMaxAnchorToFirstChild(centralPane);

        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(centralPane.widthProperty());
        clipRect.heightProperty().bind(centralPane.heightProperty());
        centralPane.setClip(clipRect);

    }

    private void setRightPaneAnchor() {
        double leftDist = Math.max(0d, controlPane.getWidth() - ge.getProps().getRightSidePaneWidth());
        setAnchorToChild(controlPane, 2, 0d, 0d, 0d, leftDist);
    }

    private void setLeftPaneAnchor() {
        double rightDist = Math.max(0d, controlPane.getWidth() - ge.getProps().getLeftSidePaneWidth());
        setAnchorToChild(controlPane, 0, 0d, rightDist, 0d, 0d);
    }

    private void setMaxAnchorToFirstChild(Pane pane) {
        setAnchorToChild(pane, 0, 0d, 0d, 0d, 0d);
    }

    @SuppressWarnings("SameParameterValue")
    private void setAnchorToChild(Pane pane, int child, Double top, Double right, Double bottom, Double left) {
        Node n = pane.getChildren().get(child);
        if (n != null) {
            AnchorPane.setBottomAnchor(n, bottom);
            AnchorPane.setTopAnchor(n, top);
            AnchorPane.setLeftAnchor(n, left);
            AnchorPane.setRightAnchor(n, right);
        }
    }

    public double getImagePaneHeight() {
        return ge.getMainPane().getHeight();
    }

    public double getImagePaneWidth() {
        if (state == State.imageOnly) {
            return ge.getMainPane().getWidth();
        }
        return Math.max(0, ge.getMainPane().getWidth()
                - ge.getProps().getLeftSidePaneWidth()
                - ge.getProps().getRightSidePaneWidth());
    }

    public void setImagePaneSizeListener(Runnable r) {
        synchronized (lock) {
            imagePaneSizeListener = r;
        }
    }

    private AnchorPane createCentralPane() {
        AnchorPane result = new AnchorPane();
        Label contentPlaceholder = new Label("");
        result.getChildren().add(contentPlaceholder);
        return result;
    }

    private Pane createLeftPane() {
        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(50d);
        GridPane result = new GridPane();
        ge.setLibrary(new Library(ge));
        result.add(ge.getLibrary().getPane(), 0, 0);
        result.getRowConstraints().add(rc);
        ge.setFilesystem(new Filesystem(ge));
        result.add(ge.getFilesystem().getPane(), 0, 1);
        result.getRowConstraints().add(rc);
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
                setMaxAnchorToFirstChild(centralPane);
                state = State.imageWithControls;
                ge.getNavigator().init();
                break;
            case imageWithControls:
                centralPane.getChildren().set(0, thumbnailPane);  // remove imagePane from here
                setMaxAnchorToFirstChild(centralPane);
                ge.getMainPane().getChildren().set(0, ge.getImagePane());
                setMaxAnchorToFirstChild(ge.getMainPane());
                state = State.imageOnly;
                ge.getNavigator().init();
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
                setMaxAnchorToFirstChild(ge.getMainPane());
                centralPane.getChildren().set(0, ge.getImagePane());
                setMaxAnchorToFirstChild(centralPane);
                state = State.imageWithControls;
                ge.getNavigator().init();
                break;
            case imageWithControls:
                centralPane.getChildren().set(0, thumbnailPane);
                setMaxAnchorToFirstChild(centralPane);
                state = State.thumbnail;
                ge.getNavigator().init();
                break;
            default:
                break;
        }
    }

}
