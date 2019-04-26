package de.wolfgangkronberg.kspc.files;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Traverses files across directory. Within each directory, the directly included files appear first, and then
 * the direct sub-directories.
 */
public class TraverseTreeFileScanner implements FileScanner {

    private final Comparator<File> comparator;
    private final int fileScanSize;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread result = new Thread(r, "TraverseTreeFileScanner-Initial");
        result.setDaemon(true);
        return result;
    });
    private final Map<File, DirInfo> dirCache = new HashMap<>();

    private final Object lock = new Object();
    private File startingPoint;
    private Cursor cursor = null;
    private File[] files;


    public TraverseTreeFileScanner(File startingPoint, boolean alphabetical, int fileScanSize) {
        this.startingPoint = startingPoint;
        comparator = alphabetical ? new AlphabeticalComparator() : new TimeComparator();
        this.fileScanSize = fileScanSize;
    }

    @Override
    public void start(Runnable callback) {
        executor.submit(new ScanDirRunnable(callback));
        executor.submit(this::updateCache);
    }

    // make sure that +/- fileScanSize is available, and dump contents not needed anymore
    private void updateCache() {
        // !kgb remember previous cursor, so only do anything if cursor has changed
    }

    @Override
    public File getCurrent() {
        synchronized (lock) {
            return cursor == null ? startingPoint : cursor.dir.images[cursor.filePos];
        }
    }

    @Override
    public boolean moveToNext() {
        // !kgb
        executor.submit(this::updateCache);
        return false; // !kgb
    }

    @Override
    public boolean moveToPrevious() {
        // !kgb
        executor.submit(this::updateCache);
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
        executor.submit(this::updateCache);
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
            // !kgb
        }
    }

    private static class Cursor {
        private DirInfo dir;
        private int filePos;

        public File get() {
            return filePos < 0 ? null : dir.images[filePos];
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
