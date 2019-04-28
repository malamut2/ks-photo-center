package de.wolfgangkronberg.kspc.files;

import java.io.File;
import java.util.Comparator;

/**
 * Sorts descending by file modification time. On equal time, falls back to alphabetical sorting.
 */
public class TimeComparator implements Comparator<File> {

    private final Comparator<File> fallback = new AlphabeticalComparator();

    @Override
    public int compare(File f1, File f2) {
        long result = lastModified(f2) - lastModified(f1);
        if (result < 0) {
            return -1;
        }
        if (result > 0) {
            return 1;
        }
        return fallback.compare(f1, f2);
    }

    private long lastModified(File f) {
        try {
            return f.lastModified();
        } catch (SecurityException ignored) {
            return 0;
        }
    }

}
