package de.wolfgangkronberg.kspc.filescanner;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

class AlphabeticalComparator implements Comparator<File> {

    private final Collator coll = Collator.getInstance();

    @Override
    public int compare(File f1, File f2) {
        String name1 = f1.getName();
        String name2 = f2.getName();
        int result = coll.compare(name1, name2);
        if (result == 0) {
            result = name1.compareTo(name2);
        }
        return result;
    }
}
