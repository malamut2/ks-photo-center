package de.wolfgangkronberg;

import de.wolfgangkronberg.filescanner.FileCache;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Keeps track on which picture is actually being displayed
 */
public class Navigator {

    private final GlobalElements ge;
    private Pane pane;
    private double paneHeight;
    private double paneWidth;
    private int numPrefetchedAroundCurrent;
    private TimedMessage message;

    private FileSequence files;
    private GroupedCacheLoader<File, ImageWithMetadata> gCache;
    private FileCache<ImageWithMetadata> fCache;

    public Navigator(GlobalElements ge) {
        this.ge = ge;
    }

    /**
     * Initialized the Navigator by setting the initial picture which shall be viewed.
     */
    public void init() {
        AppProperties props = ge.getProps();
        File current = ge.getCurrentImage();
        pane = ge.getImagePane();
        message = ge.getImagePaneMessage();
        numPrefetchedAroundCurrent = props.getNumPrefetchAroundCurrent();
        files = new FileSequence(props,
                current == null ? props.getDefaultNavStrategy() : props.getOpenFileNavStrategy(), current);
        gCache = new GroupedCacheLoader<>((f) -> new ImageWithMetadata(ge, f),
                3, props.getNumCacheShownImages());
        fCache = new FileCache<>(files, gCache);

        Pane parent = ((Pane)pane.getParent());  // !kgb don't use parent. Use separate logic instead. Also, introduce panning of zoomed images.
        if (parent != null) {
            paneHeight = parent.getHeight();
            paneWidth = parent.getWidth();
            ChangeListener<Number> paneSizeListener = (observable, oldValue, newValue) -> {
                paneHeight = parent.getHeight();
                paneWidth = parent.getWidth();
                displayImage();
            };
            parent.widthProperty().addListener(paneSizeListener);
            parent.heightProperty().addListener(paneSizeListener);

            if (current != null) {
                displayImage();
            }
        }
    }

    public void switchToPicture(File newPicture) {
        fCache.setCurrent(newPicture);
        displayImage();
    }

    public void switchToNextPicture() {
        if (!files.moveToNext()) {
            message.play("This is the last image.");
            return;
        }
        displayImage();
    }

    public void switchToPreviousPicture() {
        if (!files.moveToPrevious()) {
            message.play("This is the first image.");
            return;
        }
        displayImage();
    }

    public void reloadImages() {
        files.reload(this::displayImage);
    }

    public void displayImage() {
        File current = fCache.prefetch("displayed", numPrefetchedAroundCurrent);
        if (current == null) {
            message.play("No image to display.");
            return;
        }
        ImageWithMetadata iwm;
        try {
            iwm = gCache.get(current).get();
        } catch (InterruptedException e) {
            message.play("Interrupted while loading picture: " + current.getAbsolutePath());
            return;
        } catch (ExecutionException e) {
            message.play("Error loading picture '" + current.getAbsolutePath() + "': " + e.toString());
            return;
        }
        if (!iwm.isValid()) {
            message.play("Cannot find or display picture: " + current.getAbsolutePath());
            return;
        }
        ge.setCurrentImage(current);
        ImageView iv = iwm.getImageView(paneWidth, paneHeight);
        pane.getChildren().set(0, iv);
    }

}
