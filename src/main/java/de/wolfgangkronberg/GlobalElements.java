package de.wolfgangkronberg;

import de.wolfgangkronberg.edit.EditAction;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Data;

import java.io.File;
import java.util.LinkedList;

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
     * The instance keeping track of current base application layout state, and which handles layout state changes
     */
    private ApplicationLayout applicationLayout;

    /**
     * Navigates through our images
     */
    private Navigator navigator = new Navigator(this);

    /**
     * The application pane. Has exactly one child, either imagePane or controlPane
     */
    private Pane mainPane;

    /**
     * The pane which display our current image
     */
    private Pane imagePane;

    /**
     * A message which may be displayed as overlay to the displayed image
     */
    private TimedMessage imagePaneMessage;

    /**
     * A message which may be displayed as overlay to the central pane
     */
    private TimedMessage centralPaneMessage;

    /**
     * Contains open edit operations which build upon each other (e.g. Paint -> Insert Text Field)
     */
    private LinkedList<EditAction> editActionStack = new LinkedList<>();

    /**
     * The currently selected/displayed image. May be null if no image is selected.
     */
    private File currentImage;

}
