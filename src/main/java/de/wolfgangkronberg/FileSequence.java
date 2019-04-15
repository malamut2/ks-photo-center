package de.wolfgangkronberg;

import de.wolfgangkronberg.filescanner.FileScanner;
import de.wolfgangkronberg.filescanner.SimpleAlphabeticalFileScanner;

import java.io.File;
import java.util.List;

public class FileSequence {

    private final FileScanner fileScanner;

    public FileSequence(AppProperties props, NavigationStrategy navStrategy, File startingPoint) {
        int fileScanSize = props.getNumTraverseFiles();
        switch (navStrategy) {
            case CurrentDirAlphabetical:
                fileScanner = new SimpleAlphabeticalFileScanner(startingPoint);
                break;
            default:
                throw new RuntimeException("Strategy not yet implemented: " + navStrategy.name());
        }
        fileScanner.start(null);
    }

    public File getCurrent() {
        return fileScanner.getCurrent();
    }

    public boolean moveToNext() {
        return fileScanner.moveToNext();
    }

    public boolean moveToPrevious() {
        return fileScanner.moveToPrevious();
    }

    public void reload(Runnable callback) {
        fileScanner.reload(callback);
    }

    public List<File> getNext(int num) {
        return fileScanner.getNext(num);
    }

    public List<File> getPrevious(int num) {
        return fileScanner.getPrevious(num);
    }


}
