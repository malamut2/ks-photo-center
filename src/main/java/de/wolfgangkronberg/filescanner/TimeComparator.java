package de.wolfgangkronberg.filescanner;

import java.io.File;
import java.util.Comparator;

class TimeComparator implements Comparator<File> {

    private final Comparator<File> fallback = new AlphabeticalComparator();

    @Override
    public int compare(File f1, File f2) {
        long result = f2.lastModified() - f1.lastModified();
        if (result < 0) {
            return -1;
        }
        if (result > 0) {
            return 1;
        }
        return fallback.compare(f1, f2);
    }
}
