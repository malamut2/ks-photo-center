package de.wolfgangkronberg.kspc.files;

import de.wolfgangkronberg.kspc.GlobalElements;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
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
    private final TreeItem<File> treeRoot;
    private final GlobalElements ge;

    public Filesystem(GlobalElements ge) {

        this.ge = ge;
        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            roots = new File[]{new File("/")};
        }
        List<TreeItem<File>> treeRoots = files2treeItems(ge, roots);
        treeView = new TreeView<>();
        if (treeRoots.size() > 1) {
            treeRoot = new TreeItem<>(new File("This PC"));
            treeRoot.getChildren().addAll(treeRoots);
            treeView.setShowRoot(false);
        } else {
            treeRoot = treeRoots.get(0);
        }

        treeView.setRoot(treeRoot);
        treeView.setBackground(new Background(new BackgroundFill(Color.web("d0d0d0"), null, null)));
        select(ge.getCurrentImage());

    }

    private void select(File current) {
        if (current == null) {
            return;
        }
        LinkedList<File> dirChain = new LinkedList<>();
        File dir = current.getParentFile();
        while (dir != null) {
            dirChain.addFirst(dir);
            dir = dir.getParentFile();
        }
        TreeItem<File> treeItem = treeRoot;
        for (File dirPart : dirChain) {
            if (dirPart.equals(treeItem.getValue())) {
                continue;
            }
            TreeItem<File> childItem = findChild(dirPart, treeItem);
            if (childItem == null) {
                childItem = new FileTreeItem(ge, dirPart);
                ((FileTreeItem)treeItem).addPermanently(childItem);
            }
            treeItem = childItem;
        }
        selectInView(treeItem);
    }

    private TreeItem<File> findChild(File dirPart, TreeItem<File> treeItem) {
        ObservableList<TreeItem<File>> treeItems = treeItem.getChildren();
        for (TreeItem<File> childItem : treeItems) {
            if (dirPart.equals(childItem.getValue())) {
                childItem.setExpanded(true);
                return childItem;
            }
        }
        return null;
    }

    private void selectInView(TreeItem<File> item) {
        MultipleSelectionModel selectionModel = treeView.getSelectionModel();
        int row = treeView.getRow(item);
        if (row >= 0) {
            selectionModel.select(row);
        }
    }

    public static List<TreeItem<File>> files2treeItems(GlobalElements ge, File[] fileArray) {
        return Arrays.stream(fileArray).map((f) -> new FileTreeItem(ge, f)).collect(Collectors.toList());
    }

    public TreeView getPane() {
        return treeView;
    }

}
