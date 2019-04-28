package de.wolfgangkronberg.kspc.files;

import java.io.File;
import java.util.Comparator;

/**
 * Sorts directories in front of files, using a provided fallback for sorting within directories resp. within files.
 */
public class DirectoryFirstComparator implements Comparator<File> {

    private final Comparator<File> fallback;

    public DirectoryFirstComparator(Comparator<File> fallback) {
        this.fallback = fallback;
    }

    @Override
    public int compare(File f1, File f2) {
        boolean d1 = isDirectory(f1);
        boolean d2 = isDirectory(f2);
        if (d1 && !d2) {
            return -1;
        }
        if (!d1 && d2) {
            return 1;
        }
        return fallback.compare(f1, f2);
    }

    private boolean isDirectory(File f) {
        try {
            return f.isDirectory();
        } catch (SecurityException ignored) {
            return false;
        }
    }

}
