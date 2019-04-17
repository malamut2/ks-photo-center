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

    private class KeyHandleSequence {

        private final KeyEvent key;
        private final KeyCode code;

        private KeyHandleSequence(KeyEvent key) {
            this.key = key;
            code = key.getCode();
        }

        private void handle() {
            @SuppressWarnings("unused") boolean ignored =
                    cursor() && fullscreen() && function() && enterEscape() && zoom();
        }

        private boolean zoom() {
            if (!key.isControlDown()) {
                return true;
            }
            switch (code) {
                case DIGIT0:
                case NUMPAD0:
                    setZoom(-1);
                    return false;
                case DIGIT1:
                case NUMPAD1:
                    setZoom(1);
                    return false;
                case PLUS:
                    setZoom(nextZoomLevel());
                    return false;
                case MINUS:
                    setZoom(previousZoomLevel());
                    return false;
                default:
                    return true;
            }
        }

        private boolean enterEscape() {
            switch (code) {
                case ESCAPE:
                    handleEscape();
                    return false;
                case ENTER:
                    handleEnter();
                    return false;
                default:
                    return true;
            }
        }

        private boolean function() {
            if (code == F5) {
                ge.getNavigator().reloadImages();
                return false;
            }
            return true;
        }

        private boolean fullscreen() {
            if (code == F) {
                ge.getStage().setFullScreen(!ge.getStage().isFullScreen());
                return false;
            }
            return true;
        }

        private boolean cursor() {
            switch (code) {
                case RIGHT:
                    ge.getNavigator().switchToNextPicture();
                    return false;
                case LEFT:
                    ge.getNavigator().switchToPreviousPicture();
                    return false;
                default:
                    return true;
            }
        }

    }

    private double previousZoomLevel() {
        ZoomLevelSteps steps = new ZoomLevelSteps(ge);
        return steps.stepify(false);
    }

    private double nextZoomLevel() {
        ZoomLevelSteps steps = new ZoomLevelSteps(ge);
        return steps.stepify(true);
    }

    private void setZoom(double newZoom) {
        ge.setImageZoom(newZoom);
        ge.getNavigator().displayImage();
    }

    public void handle(KeyEvent key) {
        ge.getImagePaneMessage().stop();
        KeyHandleSequence sequemce = new KeyHandleSequence(key);
        sequemce.handle();
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
