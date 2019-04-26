package de.wolfgangkronberg.kspc.files;

import java.io.File;
import java.util.Comparator;
import java.util.List;

public class TraverseTreeFileScanner implements FileScanner {

    private final Object lock = new Object();
    private final Comparator<File> comparator;
    private final int fileScanSize;

    private File startingPoint;
    private boolean ready = false;
    private int cursor;
    private File[] files;


    public TraverseTreeFileScanner(File startingPoint, boolean alphabetical, int fileScanSize) {
        this.startingPoint = startingPoint;
        comparator = alphabetical ? new AlphabeticalComparator() : new TimeComparator();
        this.fileScanSize = fileScanSize;
        // !kgb
    }

    @Override
    public void start(Runnable callback) {
        // !kgb
    }

    @Override
    public File getCurrent() {
        return null; // !kgb
    }

    @Override
    public boolean moveToNext() {
        return false; // !kgb
    }

    @Override
    public boolean moveToPrevious() {
        return false; // !kgb
    }

    @Override
    public void reload(Runnable callback) {
        // !kgb
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
    }
}
