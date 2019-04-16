package de.wolfgangkronberg;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Data;

/**
 * Provides central access to all objects which might be of global relevance in the application
 */
@Data
public class GlobalElements {

    /**
     * Application settings from config file and/or command line
     */
    private final AppProperties props = new AppProperties();

    /**
     * The single JavaFX stage of this application
     */
    private Stage stage;

    /**
     * Navigates through our images
     */
    private Navigator navigator = new Navigator(this);

    /**
     * The application pane. Has exactly one child, either imagePane or controlPane
     */
    private Pane mainPane;

    /**
     * The standard application pane, with all the controls on the right and left side
     */
    private Pane controlPane;

    /**
     * The center area of the control pane. Contains the thumbnailPane, the imagePane, or nothing.
     */
    private Pane centralPane;

    /**
     * Displays image thumbnails
     */
    private Pane thumbnailPane;

    /**
     * The pane which display our current image
     */
    private Pane imagePane;

    /**
     * A message which may be displayed as overlay to the displayed image
     */
    private TimedMessage imagePaneMessage;

}
