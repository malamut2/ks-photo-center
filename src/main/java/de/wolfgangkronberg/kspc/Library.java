package de.wolfgangkronberg.kspc;

import javafx.scene.control.ComboBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public class Library {

    private enum View {
        /**
         * The directories / files in the library as a tree, as added / watched / deep watched / removed.
         * 'Deep Watched' means that children recursively will be added as happening on disk.
         * 'Watched' means that only the folder contents itself is watched for additions, not the subdirs.
         * A separate root is available for '(missing)' dirs and files for which relevant information
         * has been stored in the library, so that it can be recovered here.
         */
        Tree,

        /**
         * All directories which are present in the library and contain library pictures, in flat view
         */
        DirsFlat,

        /**
         * All files which are present in the library, in flat view.
         */
        FilesFlat,

        /**
         * Albums can be created at any place in the filesystem, or just 'in the library'. This view
         * collects those as well as the ones which reside in the filesystem and are from that location
         * part of the library.
         */
        Albums,

        /**
         * Collages can be created at any place in the filesystem, or just 'in the library'. This view
         * collects those as well as the ones which reside in the filesystem and are from that location
         * part of the library.
         */
        Collages,

        /**
         * The images which are currently selected for some future action. This selection may or may not
         * be automatically persisted in the library, as per user's setting.
         */
        CurrentlySelectedImages
    }

    private final GlobalElements ge;
    private final BorderPane library;
    private final ComboBox<View> selector;

    private View currentView = View.DirsFlat;

    public Library(GlobalElements ge, Pane parent) {
        this.ge = ge;
        library = new BorderPane();
        parent.getChildren().add(library);
        library.setBackground(new Background(new BackgroundFill(Color.web("202020"), null, null)));
        selector = new ComboBox<>();
        selector.getItems().addAll(View.values());
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(View view) {
                return view == null ? null : view.name();
            }

            @Override
            public View fromString(String s) {
                return s == null ? null : View.valueOf(s);
            }
        });
        selector.setValue(currentView);
        selector.prefWidthProperty().bind(library.widthProperty());
        library.setTop(selector);
    }

}
