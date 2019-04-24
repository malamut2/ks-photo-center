package de.wolfgangkronberg.kspc;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Filesystem {

    private final GlobalElements ge;
    private final TreeView<File> treeView;
    private final TreeItem<File> treeRoot;

    private boolean alphabetical = true;

    public Filesystem(GlobalElements ge) {

        this.ge = ge;

        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            roots = new File[]{new File("/")};
        }
        List<TreeItem<File>> treeRoots = files2treeItems(roots);
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

    }

    private static List<TreeItem<File>> files2treeItems(File[] fileArray) {
        return Arrays.stream(fileArray).map(FileTreeItem::new).collect(Collectors.toList());
    }

    private static boolean hasChildren(final File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath())) {
            return dirStream.iterator().hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    private static File[] getFileList(File root) {
        File[] result = root.listFiles((f) -> !f.isHidden());
        // !kgb for files, only allow known extensions
        // !kgb sort by alphabet/time, with dirs in front
        return result;
    }

    public TreeView getPane() {
        return treeView;
    }

    private static class FileTreeItem extends TreeItem<File> {

        private final long cacheTime = 5000;
        private final File file;

        private final Object lock = new Object();
        private boolean isUpdatingChildren = false;
        private boolean isUpdatingLeafProp = false;
        private long prevGetChildren = 0;
        private long prevIsLeaf = 0;
        private boolean isLeaf = false;
        private List<TreeItem<File>> newChildren = null;

        private static final ExecutorService executor =
                Executors.newSingleThreadExecutor(r -> new Thread(r, "FileTree-Scanner"));

        public FileTreeItem(File file) {
            super(new File(file.getAbsolutePath()){
                @Override
                public String toString() {
                    String result = super.getName();
                    if (result.isEmpty()) {
                        return super.toString();
                    }
                    return result;
                }
            });
            this.file = file;
        }

        @Override
        public ObservableList<TreeItem<File>> getChildren() {
            long now = System.currentTimeMillis();
            synchronized (lock) {
                if (prevGetChildren + cacheTime < now) {
                    prevGetChildren = now;
                    requestChildrenUpdate();
                    while (newChildren == null) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.err.println("Interrupted while waiting for file tree item children");
                            newChildren = new ArrayList<>();
                        }
                    }
                    applyNewChildren();
                }
            }
            return super.getChildren();
        }

        // caller must synchronize on lock
        private void requestChildrenUpdate() {
            if (isUpdatingChildren) {
                return;
            }
            isUpdatingChildren = true;
            executor.submit(() -> {
                List<TreeItem<File>> children = scanChildren();
                boolean waiting;
                synchronized (lock) {
                    waiting = newChildren == null;
                    isUpdatingChildren = false;
                    newChildren = children;
                    lock.notifyAll();
                }
                if (!waiting) {
                    Platform.runLater(this::applyNewChildren);
                }
            });
        }

        // must only be called from JavaFX thread
        private void applyNewChildren() {
            synchronized (lock) {
                ObservableList<TreeItem<File>> previous = super.getChildren();
                Set<TreeItem<File>> previousSet = new HashSet<>(previous);
                Set<TreeItem<File>> newSet = new HashSet<>(newChildren);
                previousSet.removeAll(newChildren);
                newSet.removeAll(previous);
                if (newSet.isEmpty() && previousSet.isEmpty()) {
                    return;
                }
                if (!previousSet.isEmpty()) {
                    previous.removeIf(previousSet::contains);
                }
                if (!newSet.isEmpty()) {
                    final int n = newChildren.size();
                    Iterator<TreeItem<File>> it = newChildren.iterator();
                    for (int i = 0; i < n; i++) {
                        TreeItem<File> item = it.next();
                        if (newSet.contains(item)) {
                            previous.add(i, item);
                        }
                    }
                }
            }
        }

        private List<TreeItem<File>> scanChildren() {
            File[] children = getFileList(getValue());
            if (children == null) {
                children = new File[0];
            }
            return files2treeItems(children);
        }

        @Override
        public boolean isLeaf() {
            long now = System.currentTimeMillis();
            synchronized (lock) {
                if (prevIsLeaf + cacheTime < now) {
                    requestLeafUpdate();
                    while (isUpdatingLeafProp && prevIsLeaf == 0) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.err.println("Interrupted while waiting for file tree item leaf property");
                            isLeaf = true;
                        }
                    }
                    prevIsLeaf = now;
                }
                return isLeaf;
            }
        }

        // caller must synchronize on lock
        private void requestLeafUpdate() {
            if (isUpdatingLeafProp) {
                return;
            }
            isUpdatingLeafProp = true;
            executor.submit(() -> {
                boolean leafProp = !hasChildren(file);
                synchronized (lock) {
                    isUpdatingLeafProp = false;
                    isLeaf = leafProp;
                    lock.notifyAll();
                }
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileTreeItem that = (FileTreeItem) o;
            return Objects.equals(file, that.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file);
        }
    }

}
