package de.wolfgangkronberg.kspc.files;

import de.wolfgangkronberg.kspc.GlobalElements;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class FileTreeItem extends TreeItem<File> {

    private static final ImageFileFilter imageFilter = new ImageFileFilter();
    private static final Comparator<File> alphabeticalComparator =
            new DirectoryFirstComparator(new AlphabeticalComparator());
    private static final Comparator<File> timeComparator = new DirectoryFirstComparator(new TimeComparator());
    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "FileTree-Scanner"));

    private final long cacheTime = 5000;
    private final GlobalElements ge;
    private final File file;

    private final Object lock = new Object();
    private boolean isUpdatingChildren = false;
    private boolean isUpdatingLeafProp = false;
    private long prevGetChildren = 0;
    private long prevIsLeaf = 0;
    private boolean isLeaf = false;
    private List<TreeItem<File>> newChildren = null;

    public FileTreeItem(GlobalElements ge, File file) {
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
        this.ge = ge;
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

    private File[] getFileList(File root) {
        File[] result = root.listFiles((f) ->
                !f.isHidden() && (f.isDirectory() || (f.isFile() && imageFilter.accept(f))));
        boolean alphabetical = ge.getNavigator().getCurrentFilesystemStrategy().isAlphabetical();
        Comparator<File> comparator = alphabetical ? alphabeticalComparator : timeComparator;
        // !kgb sort by alphabet/time, with dirs in front
        return result;
    }

    private List<TreeItem<File>> scanChildren() {
        File[] children = getFileList(getValue());
        if (children == null) {
            children = new File[0];
        }
        return Filesystem.files2treeItems(ge, children);
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
