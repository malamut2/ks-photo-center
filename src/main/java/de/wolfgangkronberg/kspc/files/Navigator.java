package de.wolfgangkronberg.kspc.files;

import de.wolfgangkronberg.kspc.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    private NavigationStrategy libraryStrategy;
    private NavigationStrategy filesystemStrategy;
    private FileSequence files;
    private FileCache<ImageWithMetadata> fCache;

    public Navigator(GlobalElements ge) {
        this.ge = ge;
        AppProperties props = ge.getProps();
        numPrefetchedAroundCurrent = props.getNumPrefetchAroundCurrent();
        libraryStrategy = props.getLibraryNavStrategy();
        filesystemStrategy = props.getFilesystemNavStrategy();
    }

    /**
     * Initialized the Navigator by setting the initial picture which shall be viewed.
     */
    public void init() {

        AppProperties props = ge.getProps();
        File current = ge.getCurrentImage();
        pane = ge.getImagePane();
        message = ge.getImagePaneMessage();
        NavigationStrategy currentStrategy = current == null && !ge.getLibrary().isEmpty() ?
                libraryStrategy : filesystemStrategy;
        files = new FileSequence(props, currentStrategy, current);
        fCache = new FileCache<>(files, ge.getImageCache());

        ApplicationLayout appLayout = ge.getApplicationLayout();
        appLayout.setImagePaneSizeListener(() -> {
            paneHeight = appLayout.getImagePaneHeight();
            paneWidth = appLayout.getImagePaneWidth();
            displayImage();
        });

        if (current != null) {
            displayImage();
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
        final Future<ImageWithMetadata> future = ge.getImageCache().get(current);
        ImageWithMetadata iwm;
        try {
            iwm = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            message.play("Interrupted while loading picture: " + current.getAbsolutePath());
            return;
        } catch (ExecutionException e) {
            message.play("Error loading picture '" + current.getAbsolutePath() + "': " + e.toString());
            return;
        } catch (TimeoutException e) {
            message.play("Timeout while loading picture: " + current.getAbsolutePath());
            ge.getImageCache().inspectFuture(future);
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

    public NavigationStrategy getCurrentFilesystemStrategy() {
        return filesystemStrategy;
    }

}
