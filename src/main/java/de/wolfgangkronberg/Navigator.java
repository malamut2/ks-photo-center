package de.wolfgangkronberg;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;

/**
 * Keeps track on which picture is actually being displayed
 */
public class Navigator {

    private StackPane pane;
    private double fullScreenHeight;
    private double fullScreenWidth;

    private FileSequence files;

    /**
     * Initialized the Navigator by setting the initial picture which shall be viewed.
     * @param props the currently active application properties
     * @param fullScreenStage a stage which is currently showning full screen
     * @param pane the pane in which the image shall be displayed
     * @param pictureInitiallyViewed the path/name of the picture to be viewed, or null if just the newest picture
     */
    public void init(AppProperties props, Stage fullScreenStage, StackPane pane, String pictureInitiallyViewed) {

        this.pane = pane;
        fullScreenHeight = fullScreenStage.getHeight();
        fullScreenWidth = fullScreenStage.getWidth();
        File current = pictureInitiallyViewed == null ? null : new File(pictureInitiallyViewed);
        files = new FileSequence(props, props.getDefaultNavStrategy(), current);

        if (pictureInitiallyViewed == null) {
            Label l = new Label("Library Mode is not implemented yet.");
            pane.getChildren().add(l);
        } else {
            displayImage(current);
        }
    }

    private void displayImage(File current) {
        Image image = new Image(current.toURI().toString());
        if (image.getHeight() == 0) {
            Label l = new Label("Cannot find or display picture: " + current.getAbsolutePath());
            pane.getChildren().add(l);
            return;
        }
        ImageView iv = new ImageView(image);
        double factor = getFullScreenScale(image);
        iv.setScaleX(factor);
        iv.setScaleY(factor);
        pane.getChildren().setAll(iv);
    }

    private double getFullScreenScale(Image image) {
        double scale1 = fullScreenHeight / image.getHeight();
        double scale2 = fullScreenWidth / image.getWidth();
        return Math.min(scale1, scale2);
    }

    public void switchToNextPicture() {
        if (!files.moveToNext()) {
            // !kgb display info: no next picture available
            return;
        }
        displayImage(files.getCurrent());
    }

    public void switchToPreviousPicture() {
        if (!files.moveToPrevious()) {
            // !kgb display info: no next picture available
            return;
        }
        displayImage(files.getCurrent());
    }

    public void reloadImages() {
        files.reload();
        displayImage(files.getCurrent());
    }
}
