package de.wolfgangkronberg;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import static javafx.scene.input.KeyCode.*;

/**
 * Handles all global key events.
 */
public class KeyEventHandler implements EventHandler<KeyEvent> {

    private final GlobalElements ge;

    public KeyEventHandler(GlobalElements ge) {
        this.ge = ge;
    }

    public void handle(KeyEvent key) {
        ge.getImagePaneMessage().stop();
        KeyCode code = key.getCode();
        if (code == RIGHT) {
            ge.getNavigator().switchToNextPicture();
        } else if (code == LEFT) {
            ge.getNavigator().switchToPreviousPicture();
        } else if (code == F5) {
            ge.getNavigator().reloadImages();
        }
    }

}
