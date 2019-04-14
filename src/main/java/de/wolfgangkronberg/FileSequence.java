package de.wolfgangkronberg;

import java.io.File;

public class FileSequence {

    private final NavigationStrategy navStrategy;

    private File current;

    public FileSequence(AppProperties props, NavigationStrategy navStrategy, File startingPoint) {
        // !kgb take from props: how far should we look into past/future?
        this.navStrategy = navStrategy;
        current = startingPoint;
    }

    public File getCurrent() {
        return current;
    }

    public boolean moveToNext() {
        return false;  // !kgb
    }

    public boolean moveToPrevious() {
        return false;  // !kgb
    }

    public void reload() {
        // !kgb
    }
}
