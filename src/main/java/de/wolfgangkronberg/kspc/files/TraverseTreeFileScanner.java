package de.wolfgangkronberg.kspc.files;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Traverses files across directory. Within each directory, the directly included files appear first, and then
 * the direct sub-directories.
 */
public class TraverseTreeFileScanner implements FileScanner {

    private final Comparator<File> comparator;
    private final FileFilter basicImageFileFilter = new ImageFileFilter();
    private final FileFilter imageFileFilter = (f) -> basicImageFileFilter.accept(f) && !f.isHidden();
    private final FileFilter folderFileFilter = (f) -> f.isDirectory() && !f.isHidden();
    private final int fileScanSize;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread result = new Thread(r, "TraverseTreeFileScanner-Initial");
        result.setDaemon(true);
        return result;
    });
    private final Map<File, DirInfo> dirCache = new HashMap<>();  // folder -> DirInfo

    private final Object lock = new Object();
    private File startingPoint;
    private Cursor cursor = null;
    private Cursor previousCursor = null;


    public TraverseTreeFileScanner(File startingPoint, boolean alphabetical, int fileScanSize) {
        this.startingPoint = startingPoint;
        comparator = new DirectoryFirstComparator(alphabetical ? new AlphabeticalComparator() : new TimeComparator());
        this.fileScanSize = fileScanSize;
    }

    @Override
    public void start(Runnable callback) {
        executor.submit(new ScanDirRunnable(callback));
        executor.submit(this::updateCacheOnNewCursor);
    }

    // make sure that +/- fileScanSize is available
    private void updateCacheOnNewCursor() {

        if (Objects.equals(cursor, previousCursor)) {
            return;
        }

        previousCursor = cursor.copy();
        Cursor start = cursor.copy();
        Cursor end = cursor.copy();
        int numFiles = start.move(-fileScanSize) + 1;
        numFiles += end.move(fileScanSize);

        synchronized (lock) {
            Set<File> toRemove = new HashSet<>(dirCache.keySet());
            start.removeReferencedNext(toRemove, numFiles);
            for (File dir : toRemove) {
                dirCache.remove(dir);
            }
        }
    }

    @Override
    public File getCurrent() {
        synchronized (lock) {
            return cursor == null ? startingPoint : cursor.get();
        }
    }

    @Override
    public boolean moveToNext() {
        // !kgb
        executor.submit(this::updateCacheOnNewCursor);
        return false; // !kgb
    }

    @Override
    public boolean moveToPrevious() {
        // !kgb
        executor.submit(this::updateCacheOnNewCursor);
        return false; // !kgb
    }

    @Override
    public void reload(Runnable callback) {
        // !kgb drop cache completely, run initial steps again, but make sure cursor != null all the time
    }

    @Override
    public List<File> getNext(int num) {
        return null; // !kgb
    }

    @Override
    public List<File> getPrevious(int num) {
        return null; // !kgb
    }

    @Override
    public void setCurrent(File newFile) {
        // !kgb
        executor.submit(this::updateCacheOnNewCursor);
    }

    private DirInfo getDirInfo(File dir) {
        synchronized (lock) {
            return dirCache.computeIfAbsent(dir, DirInfo::new);
        }
    }

    private class DirInfo {
        private final File dir;

        private File[] images;
        private File[] subDirs;

        public DirInfo(File dir) {
            this.dir = dir;
        }

        public void scan() {
            File[] newImages = sort(dir.listFiles(imageFileFilter));
            File[] newSubDirs = sort(dir.listFiles(folderFileFilter));
            synchronized (lock) {
                images = newImages;
                subDirs = newSubDirs;
            }
        }

        // sorts in place if files != null
        private File[] sort(File[] files) {
            if (files == null) {
                files = new File[0];
            }
            Arrays.sort(files, comparator);
            return files;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DirInfo dirInfo = (DirInfo) o;
            return Objects.equals(dir, dirInfo.dir);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dir);
        }
    }

    private static class Cursor {
        private DirInfo dir;
        private int filePos;

        public File get() {
            return filePos < 0 ? null : dir.images[filePos];
        }

        public Cursor copy() {
            Cursor result = new Cursor();
            result.dir = dir;
            result.filePos = filePos;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cursor cursor = (Cursor) o;
            return filePos == cursor.filePos &&
                    Objects.equals(dir, cursor.dir);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dir, filePos);
        }

        /**
         * Returns the number of positions which we could actually move until reaching the end.
         * This action includes initializing DirInfo objects and storing them in the cache
         * for all image files in between.
         */
        public int move(int diff) {
            return 0;  // !kgb
        }

        /**
         * Starting at current position, and for the nest numCursorPositions forward, removes all direct parent
         * folders of the image files found from the given set.
         */
        public void removeReferencedNext(Set<File> dirs, int numCursorPositions) {
            // !kgb
        }
    }

    private class ScanDirRunnable implements Runnable {

        private final Runnable callback;

        public ScanDirRunnable(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {

            DirInfo dir = getDirInfo(startingPoint.getParentFile());
            dir.scan();
            int idx = Arrays.binarySearch(dir.images, startingPoint, comparator);

            synchronized (lock) {
                cursor = new Cursor();
                cursor.dir = dir;
                cursor.filePos = idx;
                lock.notifyAll();
            }

            if (callback != null) {
                callback.run();
            }

        }
    }
}
