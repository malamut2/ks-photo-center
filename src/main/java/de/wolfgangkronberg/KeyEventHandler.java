package de.wolfgangkronberg;

import de.wolfgangkronberg.edit.EditAction;
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
        } else if (code == F) {
            ge.getStage().setFullScreen(!ge.getStage().isFullScreen());
        } else if (code == ESCAPE) {
            handleEscape();
        } else if (code == ENTER) {
            handleEnter();
        }
    }

    private void handleEnter() {
        EditAction editAction = ge.getEditActionStack().pollLast();
        if (editAction != null) {
            editAction.confirm();
            return;
        }
        if (!ge.getApplicationLayout().enter()) {
            ge.getCentralPaneMessage().play("No image has been selected for display");
        }
    }

    private void handleEscape() {
        EditAction editAction = ge.getEditActionStack().pollLast();
        if (editAction != null) {
            editAction.cancel();
            return;
        }
        ge.getApplicationLayout().escape();
    }

}
