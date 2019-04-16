package de.wolfgangkronberg;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Shows a message which fades out after a given time
 */
public class TimedMessage {

    private static final String style =
            "-fx-text-fill: #f0f0f0; " +
                    "-fx-background-radius: 15; " +
                    "-fx-background-color: #30303080; " +
                    "-fx-background-insets: 15 10; " +
                    "-fx-label-padding: 20; ";

    private final Pane root;
    private final Label message;
    private final Transition transition;

    public TimedMessage(Pos pos) {
        message = new Label("");
        message.setFont(Font.font(40));
        message.setStyle(style);
        root = createRoot(pos, message);
        transition = createTransition(message);
    }

    private static Transition createTransition(Label message) {
        SequentialTransition result = new SequentialTransition();
        PauseTransition pause = new PauseTransition(Duration.millis(1000));
        FadeTransition fader = new FadeTransition(Duration.millis(500), message);
        fader.setFromValue(1.0);
        fader.setToValue(0.0);
        result.getChildren().addAll(pause, fader);
        return result;
    }

    private static Pane createRoot(Pos pos, Label message) {
        BorderPane bp1 = new BorderPane();
        BorderPane bp2 = new BorderPane();
        switch (pos.getHpos()) {
            case LEFT:
                bp1.setLeft(bp2);
                break;
            case CENTER:
                bp1.setCenter(bp2);
                break;
            default:
                bp1.setRight(bp2);
                break;
        }
        switch (pos.getVpos()) {
            case TOP:
                bp2.setTop(message);
                break;
            case CENTER:
                bp2.setCenter(message);
                break;
            default:
                bp2.setBottom(message);
                break;
        }
        return bp1;
    }

    public Pane getRoot() {
        return root;
    }

    public void play(String text) {
        message.setText(text);
        transition.play();
    }

    /**
     * Hides the message immediately
     */
    public void stop() {
        message.setText("");
        transition.stop();
        transition.jumpTo(Duration.ZERO);
    }
}
