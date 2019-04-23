package de.wolfgangkronberg.kspc.library;

import de.wolfgangkronberg.kspc.GlobalElements;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.EnumMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Library {

    private enum View {
        /**
         * The directories / files in the library as a tree, as added / watched / deep watched / removed.
         * 'Deep Watched' means that children recursively will be added as happening on disk.
         * 'Watched' means that only the folder contents itself is watched for additions, not the subdirs.
         * A separate root is available for '(missing)' dirs and files for which relevant information
         * has been stored in the library, so that it can be recovered here.
         */
        Tree("Library Definition"),

        /**
         * All directories which are present in the library and contain library pictures, in flat view
         */
        DirsFlat("Image Folders"),

        /**
         * All files which are present in the library, in flat view.
         */
        FilesFlat("Images"),

        /**
         * Albums can be created at any place in the filesystem, or just 'in the library'. This view
         * collects those as well as the ones which reside in the filesystem and are from that location
         * part of the library.
         */
        Albums("Library Albums"),

        /**
         * Collages can be created at any place in the filesystem, or just 'in the library'. This view
         * collects those as well as the ones which reside in the filesystem and are from that location
         * part of the library.
         */
        Collages("Library Collages"),

        /**
         * The images which are currently selected for some future action. This selection may or may not
         * be automatically persisted in the library, as per user's setting.
         */
        CurrentlySelectedImages("Current Selection");

        private final String title;

        View(String title) {
            this.title = title;
        }
    }

    private final GlobalElements ge;
    private final BorderPane library;
    private final ComboBox<View> selector;
    private final LibraryDB db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread result = new Thread(r, "Library-Runner");
        result.setDaemon(true);
        return result;
    });
    private final EnumMap<View, Node> panes = new EnumMap<>(View.class);

    private View currentView = View.DirsFlat;

    public Library(GlobalElements ge) {

        this.ge = ge;
        library = new BorderPane();
        library.setBackground(new Background(new BackgroundFill(Color.web("d0d0d0"), null, null)));

        selector = createSelector();
        selector.setValue(currentView);
        selector.prefWidthProperty().bind(library.widthProperty());
        selector.valueProperty().addListener((observable, oldValue, newValue) -> {
            synchronized (panes) {
                currentView = newValue;
            }
            showSelectedPane();
        });
        library.setTop(selector);

        library.setCenter(new Label("Initializing Library..."));
        db = new LibraryDB(ge);
        initDB();

    }

    public Node getPane() {
        return library;
    }

    private static ComboBox<View> createSelector() {
        ComboBox<View> selector = new ComboBox<>();
        selector.getItems().addAll(View.values());
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(View view) {
                return view == null ? null : view.title;
            }

            @Override
            public View fromString(String s) {
                if (s == null) {
                    return null;
                }
                for (View v : View.values()) {
                    if (s.equals(v.title)) {
                        return v;
                    }
                }
                throw new IllegalArgumentException("Unknown View title: " + s);
            }
        });
        return selector;
    }

    private void initDB() {
        executor.submit(() -> {
            db.init();
            showSelectedPane();
        });
    }

    private void showSelectedPane() {
        executor.submit(() -> {
            final Node pane;
            synchronized (panes) {
                pane = panes.computeIfAbsent(currentView, this::createPane);
            }
            Platform.runLater(() -> library.setCenter(pane));
        });
    }

    private Node createPane(View view) {
        switch (view) {
            // !kgb implement various library views...
            default:
                return new Label("coming soon:\n" + view.title);
        }
    }

}
