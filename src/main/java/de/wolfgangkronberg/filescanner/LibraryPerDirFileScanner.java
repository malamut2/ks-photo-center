package de.wolfgangkronberg.filescanner;

import de.wolfgangkronberg.filescanner.FileScanner;

import java.io.File;
import java.util.List;

public class LibraryPerDirFileScanner implements FileScanner {
    public LibraryPerDirFileScanner(File startingPoint, int fileScanSize) {
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
