package de.wolfgangkronberg;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import static javafx.scene.input.KeyCode.*;

/**
 * Handles all global key events.
 */
public class KeyEventHandler implements EventHandler<KeyEvent> {

    private final Navigator navigator;

    public KeyEventHandler(Navigator navigator) {
        this.navigator = navigator;
    }

    public void handle(KeyEvent key) {
        KeyCode code = key.getCode();
        if (code == RIGHT) {
            navigator.switchToNextPicture();
        } else if (code == LEFT) {
            navigator.switchToPreviousPicture();
        } else if (code == F5) {
            navigator.reloadImages();
        }
    }

}
