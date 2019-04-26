package de.wolfgangkronberg.kspc.files;

import de.wolfgangkronberg.kspc.AppProperties;

import java.io.File;
import java.util.List;

public class FileSequence {

    private final FileScanner fileScanner;

    public FileSequence(AppProperties props, NavigationStrategy navStrategy, File startingPoint) {
        int fileScanSize = props.getNumTraverseFiles();
        switch (navStrategy) {
            case CurrentDirAlphabetical:
            case CurrentDirByTime:
                fileScanner = new SimpleFileScanner(startingPoint, navStrategy.isAlphabetical());
                break;
            case TraverseTreeAlphabetical:
            case TraverseTreeByTime:
                fileScanner = new TraverseTreeFileScanner(startingPoint, navStrategy.isAlphabetical(), fileScanSize);
                break;
            case LibraryByTimePerDir:
                fileScanner = new LibraryPerDirFileScanner(startingPoint, fileScanSize);
                break;
            case LibraryByTimeFlat:
                fileScanner = new LibraryFlatFileScanner(startingPoint, fileScanSize);
                break;
            default:
                throw new RuntimeException("Unknown Strategy: " + navStrategy.name());
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

    public void setCurrent(File newFile) {
        fileScanner.setCurrent(newFile);
    }
}
