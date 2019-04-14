package de.wolfgangkronberg.filescanner;

import java.io.File;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;

public class SimpleAlphabeticalFileScanner implements FileScanner {

    private final Object lock = new Object();
    private final File startingPoint;

    private boolean ready = false;
    private int cursor;
    private File[] files;

    public SimpleAlphabeticalFileScanner(File startingPoint) {
        this.startingPoint = startingPoint;
    }

    @Override
    public void start(Runnable callback) {
        new Thread(new ScanDirRunnable(callback), "FileScanner-Initial").start();
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
        return null;  // !kgb
    }

    @Override
    public List<File> getPrevious(int num) {
        return null;  // !kgb
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

                final Collator coll = Collator.getInstance();
                Arrays.sort(files_, (f1, f2) -> {
                    String name1 = f1.getName();
                    String name2 = f2.getName();
                    int result = coll.compare(name1, name2);
                    if (result == 0) {
                        result = name1.compareTo(name2);
                    }
                    return result;
                });
                cursor_ = Arrays.binarySearch(files_, startingPoint);
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
