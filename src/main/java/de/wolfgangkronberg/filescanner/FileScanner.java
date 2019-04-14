package de.wolfgangkronberg.filescanner;

import java.io.File;
import java.util.List;

public interface FileScanner {

    void start(Runnable callback);
    File getCurrent();
    boolean moveToNext();
    boolean moveToPrevious();
    void reload(Runnable callback);
    List<File> getNext(int num);
    List<File> getPrevious(int num);

}
