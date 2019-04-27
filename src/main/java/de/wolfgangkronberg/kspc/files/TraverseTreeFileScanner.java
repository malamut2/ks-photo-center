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
    private final Cursor cursor = new Cursor();

    private final Object lock = new Object();
    private File startingPoint;
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

        Cursor start;
        Cursor end;
        synchronized (lock) {
            if (Objects.equals(cursor, previousCursor)) {
                return;
            }
            previousCursor = cursor.copy();
            start = cursor.copy();
            end = cursor.copy();
        }

        int numFiles = -start.move(-fileScanSize) + 1;
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
            return cursor.isValid() ? cursor.get() : startingPoint;
        }
    }

    @Override
    public boolean moveToNext() {
        return move(1);
    }

    @Override
    public boolean moveToPrevious() {
        return move(-1);
    }

    private boolean move(int diff) {
        waitUntilReady();
        boolean result = cursor.move(diff) != 0;
        if (result) {
            executor.submit(this::updateCacheOnNewCursor);
        }
        return result;
    }

    private void waitUntilReady() {
        synchronized (lock) {
            while (!cursor.isValid()) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public void reload(Runnable callback) {
        waitUntilReady();
        synchronized (lock) {
            dirCache.clear();
            setCursorToFile(cursor.get());
            previousCursor = null;
        }
        executor.submit(this::updateCacheOnNewCursor);
    }

    @Override
    public List<File> getNext(final int num) {
        waitUntilReady();
        Cursor c = cursor.copy();
        File[] result = new File[num];
        int idx = 0;
        for (int i = 0; i < num; i++) {
            if (c.move(1) == 0) {
                break;
            }
            result[idx++] = c.get();
        }
        if (idx < num) {
            File[] newResult = new File[idx];
            System.arraycopy(result, 0, newResult, 0, newResult.length);
            result = newResult;
        }
        return Arrays.asList(result);
    }

    @Override
    public List<File> getPrevious(int num) {
        waitUntilReady();
        Cursor c = cursor.copy();
        File[] result = new File[num];
        int idx = num - 1;
        for (int i = 0; i < num; i++) {
            if (c.move(-1) == 0) {
                break;
            }
            result[idx--] = c.get();
        }
        if (idx >= 0) {
            File[] newResult = new File[num - idx - 1];
            System.arraycopy(result, idx + 1, newResult, 0, newResult.length);
            result = newResult;
        }
        return Arrays.asList(result);
    }

    @Override
    public void setCurrent(File newFile) {
        waitUntilReady();
        File current;
        synchronized (lock) {
            current = cursor.get();
        }
        if (!Objects.equals(current, newFile)) {
            setCursorToFile(newFile);
            executor.submit(this::updateCacheOnNewCursor);
        }
    }

    private DirInfo getDirInfo(File dir) {
        synchronized (lock) {
            return dirCache.computeIfAbsent(dir, DirInfo::new);
        }
    }

    private void setCursorToFile(File f) {

        DirInfo dir = getDirInfo(f.getParentFile());
        dir.scan();
        int idx = Arrays.binarySearch(dir.images, startingPoint, comparator);

        synchronized (lock) {
            cursor.dir = dir;
            cursor.filePos = idx;
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

        public int getNumImages() {
            int result = getNum(images);
            if (result < 0) {
                result = images.length;
            }
            return result;
        }

        public int getNumSubdirs() {
            int result = getNum(subDirs);
            if (result < 0) {
                result = subDirs.length;
            }
            return result;
        }

        private int getNum(File[] array) {
            synchronized (lock) {
                if (array != null) {
                    return array.length;
                }
            }
            scan();
            return -1;
        }

        public int findSubDirIndex(File dir) {
            boolean rescanNeeded;
            synchronized (lock) {
                rescanNeeded = subDirs == null;
            }
            if (rescanNeeded) {
                scan();
            }
            synchronized (lock) {
                return Arrays.binarySearch(subDirs, dir, comparator);
            }
        }
    }

    private class Cursor {
        private DirInfo dir;
        private int filePos;

        public File get() {
            synchronized (lock) {
                return filePos < 0 ? null : dir.images[filePos];
            }
        }

        public Cursor copy() {
            Cursor result = new Cursor();
            synchronized (lock) {
                result.dir = dir;
                result.filePos = filePos;
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cursor cursor = (Cursor) o;
            synchronized (lock) {
                return filePos == cursor.filePos &&
                        Objects.equals(dir, cursor.dir);
            }
        }

        @Override
        public int hashCode() {
            synchronized (lock) {
                return Objects.hash(dir, filePos);
            }
        }

        /**
         * Returns the number of positions which we could actually move until reaching the end (negative if moved back).
         * This action includes initializing DirInfo objects and storing them in the cache
         * for all image files in between.
         */
        public int move(int diff) {
            int result = 0;
            synchronized (lock) {
                int newPos = filePos + diff;
                while (newPos < 0) {
                    result -= filePos;
                    diff += filePos;
                    filePos = 0;
                    if (traverseOneBack()) {
                        result--;
                        diff++;
                        newPos = filePos + diff;
                    } else {
                        return result;
                    }
                }
                int dirNumImages = dir.getNumImages();
                while (newPos >= dirNumImages) {
                    result += dirNumImages - filePos - 1;
                    diff -= dirNumImages - filePos - 1;
                    filePos = dirNumImages - 1;
                    if (traverseOneForward()) {
                        result++;
                        diff--;
                        newPos = filePos + diff;
                        dirNumImages = dir.getNumImages();
                    } else {
                        return result;
                    }
                }
                result += newPos - filePos;
                filePos = newPos;
            }
            return result;
        }

        // loop through the next directories until an image is found, and set the cursor to it.
        // return true on success, and false if no such directory exists.
        // caller must synchronize on lock
        @SuppressWarnings("Duplicates")
        private boolean traverseOneForward() {
            DirInfo di = dir;
            while (true) {
                while (di.getNumSubdirs() > 0) {
                    di = getDirInfo(di.subDirs[0]);
                    if (di.getNumImages() > 0) {
                        dir = di;
                        filePos = 0;
                        return true;
                    }
                }
                while (true) {
                    File parentFile = di.dir.getParentFile();
                    if (parentFile == null) {
                        return false;
                    }
                    DirInfo diParent = getDirInfo(parentFile);
                    int idx = diParent.findSubDirIndex(di.dir);
                    if (idx >= 0 && idx < diParent.getNumSubdirs() - 1) {
                        di = getDirInfo(diParent.subDirs[idx + 1]);
                        if (di.getNumImages() > 0) {
                            dir = di;
                            filePos = 0;
                            return true;
                        }
                        break;
                    }
                    di = diParent;
                }
            }
        }

        // loop through the previous directories until an image is found, and set the cursor to it.
        // return true on success, and false if no such directory exists.
        // caller must synchronize on lock
        @SuppressWarnings("Duplicates")
        private boolean traverseOneBack() {
            DirInfo di = dir;
            while (true) {
                while (true) {
                    File parentFile = di.dir.getParentFile();
                    if (parentFile == null) {
                        return false;
                    }
                    DirInfo diParent = getDirInfo(parentFile);
                    int idx = diParent.findSubDirIndex(di.dir);
                    if (idx > 0 && idx < diParent.getNumSubdirs()) {
                        di = getDirInfo(diParent.subDirs[idx - 1]);
                        break;
                    }
                    if (idx == 0 && diParent.getNumImages() > 0) {
                        dir = diParent;
                        filePos = diParent.getNumImages() - 1;
                        return true;
                    }
                    di = diParent;
                }
                while (di.getNumSubdirs() > 0) {
                    di = getDirInfo(di.subDirs[di.getNumSubdirs() - 1]);
                    if (di.getNumImages() > 0) {
                        dir = di;
                        filePos = di.getNumImages() - 1;
                        return true;
                    }
                }
                if (di.getNumImages() > 0) {
                    dir = di;
                    filePos = di.getNumImages() - 1;
                    return true;
                }
            }
        }

        /**
         * Starting at current position, and for the nest numCursorPositions forward, removes all direct parent
         * folders of the image files found from the given set.
         * After execution, the cursor may have moved forward by up to numCursorPositions
         */
        public void removeReferencedNext(Set<File> dirs, int numCursorPositions) {
            while (true) {
                synchronized (lock) {
                    dirs.remove(dir.dir);
                    int numImages = dir.getNumImages();
                    if (filePos + numCursorPositions < numImages) {
                        return;
                    }
                    numCursorPositions -= numImages - filePos - 1;
                    filePos = numImages - 1;
                    if (traverseOneForward()) {
                        numCursorPositions--;
                    } else {
                        return;
                    }
                }
            }
        }

        public boolean isValid() {
            synchronized (lock) {
                return dir != null;
            }
        }
    }

    private class ScanDirRunnable implements Runnable {

        private final Runnable callback;

        public ScanDirRunnable(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            setCursorToFile(startingPoint);
            synchronized (lock) {
                lock.notifyAll();
            }
            if (callback != null) {
                callback.run();
            }
        }
    }

}
