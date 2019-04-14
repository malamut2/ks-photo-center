package de.wolfgangkronberg;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;

/**
 * Keeps track on which picture is actually being displayed
 */
public class Navigator {

    private StackPane pane;
    private Label message;
    private double paneHeight;
    private double paneWidth;

    private FileSequence files;

    /**
     * Initialized the Navigator by setting the initial picture which shall be viewed.
     * @param props the currently active application properties
     * @param stage a stage which is currently showing full screen
     * @param pane the pane in which the image shall be displayed
     * @param pictureInitiallyViewed the path/name of the picture to be viewed, or null if just the newest picture
     */
    public void init(AppProperties props, Stage stage, StackPane pane, String pictureInitiallyViewed) {

        this.pane = pane;
        paneHeight = pane.getHeight();
        paneWidth = pane.getWidth();
        ChangeListener<Number> paneSizeListener = (observable, oldValue, newValue) -> {
            paneHeight = pane.getHeight();
            paneWidth = pane.getWidth();
            reloadLocal();
        };
        pane.widthProperty().addListener(paneSizeListener);
        pane.heightProperty().addListener(paneSizeListener);

        File current = pictureInitiallyViewed == null ? null : new File(pictureInitiallyViewed);
        files = new FileSequence(props,
                current == null ? props.getDefaultNavStrategy() : props.getOpenFileNavStrategy(), current);

        Label imagePlaceholder = new Label("");
        message = new Label("");
        message.setAlignment(Pos.BOTTOM_CENTER);
        message.setFont(Font.font(40));
        message.setStyle("-fx-text-fill: #f0f0f0; -fx-background-radius: 15; -fx-background-color: #00000080; -fx-background-insets: -5 -10;");
        pane.getChildren().addAll(imagePlaceholder, message);
        if (pictureInitiallyViewed == null) {
            message.setText("Library Mode is not implemented yet.");
        } else {
            displayImage(current);
        }
    }

    private void displayImage(File current) {
        Image image = new Image(current.toURI().toString());
        if (image.getHeight() == 0) {
            message.setText("Cannot find or display picture: " + current.getAbsolutePath());
            return;
        }
        ImageView iv = new ImageView(image);
        double factor = getFullScreenScale(image);
        iv.setScaleX(factor);
        iv.setScaleY(factor);
        pane.getChildren().set(0, iv);
    }

    private double getFullScreenScale(Image image) {
        double scale1 = paneHeight / image.getHeight();
        double scale2 = paneWidth / image.getWidth();
        return Math.min(scale1, scale2);
    }

    public void switchToNextPicture() {
        if (!files.moveToNext()) {
            message.setText("This is the last image.");
            message.setVisible(true);
            return;
        }
        message.setVisible(false);
        displayImage(files.getCurrent());
    }

    public void switchToPreviousPicture() {
        if (!files.moveToPrevious()) {
            message.setText("This is the first image.");
            message.setVisible(true);
            return;
        }
        message.setVisible(false);
        displayImage(files.getCurrent());
    }

    public void reloadImages() {
        files.reload(this::reloadLocal);
    }

    private void reloadLocal() {
        displayImage(files.getCurrent());
    }
}
