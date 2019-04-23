package de.wolfgangkronberg.kspc;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;

public class Filesystem {

    private final GlobalElements ge;
    private final TreeView<File> treeView;
    private final TreeItem<File> treeRoot;

    private boolean alphabetical = true;

    public Filesystem(GlobalElements ge) {

        this.ge = ge;

        treeRoot = new TreeItem<>(new File("/"));

        // !kgb

        treeView = new TreeView<>(treeRoot);
        treeView.setBackground(new Background(new BackgroundFill(Color.web("d0d0d0"), null, null)));

    }

    public Node getPane() {
        return treeView;
    }

}
