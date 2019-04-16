package de.wolfgangkronberg;

import de.wolfgangkronberg.filescanner.FileCache;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Keeps track on which picture is actually being displayed
 */
public class Navigator {

    private StackPane pane;
    private Label message;
    private double paneHeight;
    private double paneWidth;
    private int numPrefetchedAroundCurrent;

    private FileSequence files;
    private GroupedCacheLoader<File, ImageWithMetadata> gCache;
    private FileCache<ImageWithMetadata> fCache;

    /**
     * Initialized the Navigator by setting the initial picture which shall be viewed.
     *
     * @param props                  the currently active application properties
     * @param pane                   the pane in which the image shall be displayed
     * @param pictureInitiallyViewed the path/name of the picture to be viewed, or null if just the newest picture
     */
    public void init(AppProperties props, StackPane pane, String pictureInitiallyViewed) {

        numPrefetchedAroundCurrent = props.getNumPrefetchAroundCurrent();
        File current = pictureInitiallyViewed == null ? null : new File(pictureInitiallyViewed);
        files = new FileSequence(props,
                current == null ? props.getDefaultNavStrategy() : props.getOpenFileNavStrategy(), current);
        gCache = new GroupedCacheLoader<>(ImageWithMetadata::new,
                3, props.getNumCacheShownImages());
        fCache = new FileCache<>(files, gCache);

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

        Label imagePlaceholder = new Label("");
        BorderPane bp1 = new BorderPane();
        BorderPane bp2 = new BorderPane();
        message = new Label("");
        message.setFont(Font.font(40));
        message.setStyle("-fx-text-fill: #f0f0f0; -fx-background-radius: 15; -fx-background-color: #30303080; -fx-background-insets: 15 10; -fx-label-padding: 20;");
        bp1.setRight(bp2);
        bp2.setBottom(message);
        pane.getChildren().addAll(imagePlaceholder, bp1);
        if (pictureInitiallyViewed == null) {
            message.setText("Library Mode is not implemented yet.");
        } else {
            displayImage();
        }
    }

    private void displayImage() {
        File current = fCache.prefetch("displayed", numPrefetchedAroundCurrent);
        if (current == null) {
            message.setText("No image to display.");
            return;
        }
        ImageWithMetadata iwm;
        try {
            iwm = gCache.get(current).get();
        } catch (InterruptedException e) {
            message.setText("Interrupted while loading picture: " + current.getAbsolutePath());
            return;
        } catch (ExecutionException e) {
            message.setText("Error loading picture '" + current.getAbsolutePath() + "': " + e.toString());
            return;
        }
        if (!iwm.isValid()) {
            message.setText("Cannot find or display picture: " + current.getAbsolutePath());
            return;
        }
        ImageView iv = iwm.getImageView(paneWidth, paneHeight);
        pane.getChildren().set(0, iv);
    }

    public void switchToNextPicture() {
        if (!files.moveToNext()) {
            message.setText("This is the last image.");
            message.setVisible(true);
            return;
        }
        message.setVisible(false);
        displayImage();
    }

    public void switchToPreviousPicture() {
        if (!files.moveToPrevious()) {
            message.setText("This is the first image.");
            message.setVisible(true);
            return;
        }
        message.setVisible(false);
        displayImage();
    }

    public void reloadImages() {
        files.reload(this::reloadLocal);
    }

    private void reloadLocal() {
        displayImage();
    }
}
