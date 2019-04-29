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

    private static final Comparator<File> alphabeticalComparator = new AlphabeticalComparator();
    private static final Comparator<File> timeComparator = new TimeComparator();
    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> {
                Thread result = new Thread(r, "FileTree-Scanner");
                result.setDaemon(true);
                return result;
            });

    private final long cacheTime = 5000;
    private final GlobalElements ge;
    private final File file;
    private final List<TreeItem<File>> manuallyAdded = new ArrayList<>(0);

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

    /**
     * While we generally do not traverse or show hidden or inaccessible items, we will show them when it is
     * required. In that case, we will statically add the item, and never update that (even if the underlying
     * file is moved or deleted).
     * @param item the item to add permanently to this FileTree
     */
    public void addPermanently(TreeItem<File> item) {
        synchronized (lock) {
            if (!manuallyAdded.contains(item)) {
                manuallyAdded.add(item);
            }
        }
        synchronized (lock) {
            while (!newChildren.contains(item)) {
                try {
                    requestChildrenUpdate();
                    lock.wait(50);
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while waiting for file tree item children");
                }
            }
        }
    }

    // caller must synchronize on lock
    private void requestChildrenUpdate() {
        if (isUpdatingChildren) {
            return;
        }
        isUpdatingChildren = true;
        executor.execute(() -> {
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

    private File[] getList(File root, File[] result, Comparator<File> ac, Comparator<File> tc) {
        if (result == null) {
            System.err.println("Could not scan directory: " + root.getAbsolutePath());
            return new File[0];
        }
        boolean alphabetical = ge.getNavigator().getCurrentFilesystemStrategy().isAlphabetical();
        Comparator<File> comparator = alphabetical ? ac : tc;
        Arrays.sort(result, comparator);
        return result;
    }

    private File[] getDirList(File root) {
        File[] result = root.listFiles(this::shouldShowInTree);  // !kgb take care of SecurityException
        return getList(root, result, alphabeticalComparator, timeComparator);
    }

    private List<TreeItem<File>> scanChildren() {
        File[] children = getDirList(getValue());
        if (children == null) {
            children = new File[0];
        }
        List<TreeItem<File>> treeItems = Filesystem.files2treeItems(ge, children);
        synchronized (lock) {
            if (!manuallyAdded.isEmpty()) {
                for (TreeItem<File> item : manuallyAdded) {
                    if (!treeItems.contains(item)) {
                        treeItems.add(item);
                    }
                }
            }
        }
        return treeItems;
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
        executor.execute(() -> {
            boolean leafProp = !hasChildren(file);
            synchronized (lock) {
                isUpdatingLeafProp = false;
                isLeaf = leafProp;
                lock.notifyAll();
            }
        });
    }

    private boolean hasChildren(final File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath())) {
            for (Path path : dirStream) {
                File file = path.toFile();
                if (shouldShowInTree(file)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean shouldShowInTree(File file) {
        try {
            return file.isDirectory() && !file.isHidden();
        } catch (SecurityException e) {
            synchronized (lock) {
                for (TreeItem<File> item : manuallyAdded) {
                    if (file.equals(item.getValue())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    // !kgb check what happens if the user opens a hidden file,
    //  and a file where any directory in the structure is hidden

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
