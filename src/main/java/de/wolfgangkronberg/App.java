package de.wolfgangkronberg;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

public class App extends Application {

    private String currentPictureName = null;

    @Override
    public void start(Stage stage) {
        setCommandlineParams(new ArrayDeque<>(getParameters().getRaw()));
        StackPane pane = new StackPane();
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(pane, 640, 480);
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.show();
        if (currentPictureName == null) {
            Label l = new Label("ksPhotoCenter is ready.");
            pane.getChildren().add(l);
        } else {
            Image image;
            File f = new File(currentPictureName);
            image = new Image(f.toURI().toString());
            if (image.getHeight() == 0) {
                Label l = new Label("Cannot find or display picture: '" + currentPictureName + "'");
                pane.getChildren().add(l);
                return;
            }
            ImageView iv = new ImageView(image);
            double factor = getScale(image, stage);
            iv.setScaleX(factor);
            iv.setScaleY(factor);
            pane.getChildren().add(iv);
        }
    }

    private double getScale(Image image, Stage stage) {
        double scale1 = stage.getHeight() / image.getHeight();
        double scale2 = stage.getWidth() / image.getWidth();
        return Math.min(scale1, scale2);
    }

    private void setCommandlineParams(Deque<String> args) {
        while (!args.isEmpty()) {
            String arg = args.pollFirst();
            if (arg.startsWith("-")) {
                // !kgb do some action on the option, possibly polling another string
            } else {
                setViewPicture(arg);
                if (!args.isEmpty()) {
                    System.err.println("Ignoring subsequent parameters after '" + arg + "'");
                }
                return;
            }
        }
    }

    private void setViewPicture(String fileName) {
        currentPictureName = fileName;
    }

}
