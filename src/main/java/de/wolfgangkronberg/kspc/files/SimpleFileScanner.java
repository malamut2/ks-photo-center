package de.wolfgangkronberg.kspc.files;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Traverses files within a single directory.
 */
public class SimpleFileScanner implements FileScanner {

    private final Comparator<File> comparator;

    private final Object lock = new Object();
    private File startingPoint;
    private boolean ready = false;
    private int cursor;
    private File[] files;

    public SimpleFileScanner(File startingPoint, boolean alphabetical) {
        this.startingPoint = startingPoint;
        comparator = alphabetical ? new AlphabeticalComparator() : new TimeComparator();
    }

    @Override
    public void start(Runnable callback) {
        new Thread(new ScanDirRunnable(callback), "SimpleFileScanner-Initial").start();
    }

    @Override
    public File getCurrent() {
        synchronized (lock) {
            return ready ? files[cursor] : startingPoint;
        }
    }

    @Override
    public boolean moveToNext() {
        synchronized (lock) {
            waitForReady();
            if (cursor < files.length - 1) {
                cursor++;
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean moveToPrevious() {
        synchronized (lock) {
            waitForReady();
            if (cursor > 0) {
                cursor--;
                return true;
            }
            return false;
        }
    }

    @Override
    public void reload(Runnable callback) {
        new Thread(new ScanDirRunnable(callback), "FileScanner-Reload").start();
    }

    @Override
    public List<File> getNext(int num) {
        synchronized (lock) {
            waitForReady();
            int to = Math.min(cursor + num + 1, files.length);
            return Collections.unmodifiableList(Arrays.asList(files).subList(cursor + 1, to));
        }
    }

    @Override
    public List<File> getPrevious(int num) {
        synchronized (lock) {
            waitForReady();
            int from = Math.max(0, cursor - num);
            return Collections.unmodifiableList(Arrays.asList(files).subList(from, cursor));
        }
    }

    @Override
    public void setCurrent(File newFile) {
        synchronized (lock) {
            waitForReady();  // don't start several init threads in parallel
            ready = false;
            startingPoint = newFile;
        }
        start(null);
    }

    // caller must synchronize on lock
    private void waitForReady() {
        while (!ready) {
            try {
                lock.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private class ScanDirRunnable implements Runnable {

        private final Runnable callback;

        private ScanDirRunnable(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {

            File dir = startingPoint.getParentFile();
            File[] files_ = dir.listFiles(new ImageFileFilter());
            int cursor_;
            if (files_ == null) {
                files_ = new File[]{startingPoint};
                cursor_ = 0;
            } else {
                Arrays.sort(files_, comparator);
                cursor_ = Arrays.binarySearch(files_, startingPoint, comparator);
                assert (cursor_ >= 0);
            }

            synchronized (lock) {
                ready = true;
                files = files_;
                cursor = cursor_;
                lock.notifyAll();
            }

            if (callback != null) {
                callback.run();
            }

        }

    }

}
