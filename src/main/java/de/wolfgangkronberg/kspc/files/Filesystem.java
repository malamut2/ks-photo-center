package de.wolfgangkronberg.kspc.files;

import de.wolfgangkronberg.kspc.GlobalElements;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Models the tree view of the filesystem (lower left in the application).
 */
public class Filesystem {

    private final TreeView<File> treeView;

    public Filesystem(GlobalElements ge) {

        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            roots = new File[]{new File("/")};
        }
        List<TreeItem<File>> treeRoots = files2treeItems(ge, roots);
        treeView = new TreeView<>();
        TreeItem<File> treeRoot;
        if (treeRoots.size() > 1) {
            treeRoot = new TreeItem<>(new File("This PC"));
            treeRoot.getChildren().addAll(treeRoots);
            treeView.setShowRoot(false);
        } else {
            treeRoot = treeRoots.get(0);
        }

        treeView.setRoot(treeRoot);
        treeView.setBackground(new Background(new BackgroundFill(Color.web("d0d0d0"), null, null)));

    }

    public static List<TreeItem<File>> files2treeItems(GlobalElements ge, File[] fileArray) {
        return Arrays.stream(fileArray).map((f) -> new FileTreeItem(ge, f)).collect(Collectors.toList());
    }

    public TreeView getPane() {
        return treeView;
    }

}
