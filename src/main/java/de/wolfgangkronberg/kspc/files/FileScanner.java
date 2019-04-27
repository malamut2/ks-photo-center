package de.wolfgangkronberg.kspc.files;

import java.io.File;
import java.util.List;

public interface FileScanner {

    void start(Runnable callback);

    File getCurrent();

    /**
     *
     * @return true if the cursor could indeed be moved.
     */
    boolean moveToNext();

    /**
     *
     * @return true if the cursor could indeed be moved.
     */
    boolean moveToPrevious();

    void reload(Runnable callback);

    /**
     *
     * @param num the desired number of next files
     * @return the next num available files, as sorted by comparator
     */
    List<File> getNext(int num);

    /**
     *
     * @param num the desired number of previous files
     * @return the previous num available files, as sorted by comparator
     */
    List<File> getPrevious(int num);

    void setCurrent(File newFile);

}
