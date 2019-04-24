package de.wolfgangkronberg.kspc;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Filesystem {

    private final GlobalElements ge;
    private final TreeView<File> treeView;
    private final TreeItem<File> treeRoot;
    private final boolean virtualRoot;

    private boolean alphabetical = true;

    public Filesystem(GlobalElements ge) {

        this.ge = ge;

        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            roots = new File[]{new File("/")};
        }
        List<TreeItem<File>> treeRoots = files2treeItems(roots);
        if (treeRoots.size() > 1) {
            treeRoot = new TreeItem<>(new File("This PC"));
            treeRoot.getChildren().addAll(treeRoots);
            virtualRoot = true;
        } else {
            treeRoot = treeRoots.get(0);
            virtualRoot = false;
        }

        // !kgb

        treeView = new TreeView<>();
        treeView.setRoot(treeRoot);
        treeView.setBackground(new Background(new BackgroundFill(Color.web("d0d0d0"), null, null)));

    }

    private static List<TreeItem<File>> files2treeItems(File[] fileArray) {
        return Arrays.stream(fileArray).map(FileTreeItem::new).collect(Collectors.toList());
    }

    private static boolean hasChildren(final File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath())) {
            return dirStream.iterator().hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    private static File[] getFileList(File root) {
        File[] result = root.listFiles((f) -> !f.isHidden());
        // !kgb sort by alphabet/time, with dirs in front
        return result;
    }

    public Node getPane() {
        return treeView;
    }

    private static class FileTreeItem extends TreeItem<File> {
        private long prevGetChildren = 0;
        private long prevIsLeaf = 0;
        private boolean isLeaf;
        private final long cacheTime = 5000;

        public FileTreeItem(File file) {
            super(file);
        }

        @Override
        public synchronized ObservableList<TreeItem<File>> getChildren() {
            long now = System.currentTimeMillis();
            if (prevGetChildren + cacheTime < now) {
                prevGetChildren = now;
                super.getChildren().setAll(scanChildren());  // !kgb only change what's different!
            }
            return super.getChildren();
        }

        private List<TreeItem<File>> scanChildren() {
            File[] children = getFileList(getValue());
            if (children == null) {
                children = new File[0];
            }
            return files2treeItems(children);
        }

        @Override
        public synchronized boolean isLeaf() {
            long now = System.currentTimeMillis();
            if (prevIsLeaf + cacheTime < now) {
                prevIsLeaf = now;
                isLeaf = !hasChildren(getValue());
            }
            return isLeaf;
        }
    }

}
