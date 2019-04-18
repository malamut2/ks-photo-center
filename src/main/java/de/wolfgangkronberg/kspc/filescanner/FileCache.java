package de.wolfgangkronberg.kspc.filescanner;

import de.wolfgangkronberg.kspc.FileSequence;
import de.wolfgangkronberg.kspc.GroupedCacheLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileCache<T> {

    private final FileSequence files;
    private final GroupedCacheLoader<File, T> groupedCache;

    public FileCache(FileSequence files, GroupedCacheLoader<File, T> groupedCache) {
        this.files = files;
        this.groupedCache = groupedCache;
    }

    public File prefetch(String group, int numPrefetch) {
        File result = files.getCurrent();
        List<File> previous = files.getPrevious(numPrefetch);
        List<File> next = files.getNext(numPrefetch);
        Iterator<File> itP = previous.iterator();
        Iterator<File> itN = next.iterator();
        List<File> prefetchList = new ArrayList<>(previous.size() + next.size() + 1);
        prefetchList.add(result);
        while (itP.hasNext() || itN.hasNext()) {
            if (itN.hasNext()) {
                prefetchList.add(itN.next());
            }
            if (itP.hasNext()) {
                prefetchList.add(itP.next());
            }
        }
        groupedCache.prefetch(group, prefetchList);
        return result;
    }


    public void setCurrent(File newFile) {
        files.setCurrent(newFile);
    }
}
