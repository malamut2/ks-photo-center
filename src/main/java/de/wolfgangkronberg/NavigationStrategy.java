package de.wolfgangkronberg;

/**
 * Models the various strategies for moving forward/backward through images
 */
public enum NavigationStrategy {

    CurrentDirAlphabetical(false, true, false),
    TraverseTreeAlphabetical(false, true, true),
    CurrentDirByTime(false, false, false),
    TraverseTreeByTime(false, false, true),
    LibraryByTimeFlat(true, false, true),
    LibraryByTimePerDir(true, false, false);

    private NavigationStrategy(boolean library, boolean alphabetical, boolean traverseAcrossDirs) {
        this.library = library;
        this.alphabetical = alphabetical;
        this.traverseAcrossDirs = traverseAcrossDirs;
    }

    private final boolean library;
    private final boolean alphabetical;
    private final boolean traverseAcrossDirs;

    /**
     * @return true if this strategy depends on information from our image library
     */
    public boolean isLibrary() {
        return library;
    }

    /**
     * @return true if images and directories are to be sorted alphabetically by file name
     */
    public boolean isAlphabetical() {
        return alphabetical;
    }

    /**
     * @return true if traversing should continue even after the end of the current directory has been reached.
     * In the case of using our image library, that means that images are sorted flatly, not heeding their physical
     * location at all.
     */
    public boolean isTraverseAcrossDirs() {
        return traverseAcrossDirs;
    }
}
