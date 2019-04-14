package de.wolfgangkronberg.filescanner;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

public class ImageFileFilter implements FileFilter {

    private final Set<String> allowed = Set.of("jpg", "jpeg", "gif", "png", "bmp");

    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName();
        int idx = name.lastIndexOf('.');
        if (idx >= 0) {
            return allowed.contains(name.substring(idx + 1).toLowerCase());
        }
        return false;
    }
}
