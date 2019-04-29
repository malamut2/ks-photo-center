package de.wolfgangkronberg.kspc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

public class TestFileCreator {

    private static final boolean debug = false;

    private File root;
    private final Map<String, File> files = new HashMap<>();
    private final Map<File, Long> dirTimes = new HashMap<>();

    public void init() throws IOException {

        root = Files.createTempDirectory("filescanner-test").toFile();
        if (debug) {
            System.out.println("Created temporary root folder: " + root.getAbsolutePath());
        }

        createTestFiles();

        SecurityManager sm = new SecurityManager() {

            private final File javaHome = new File(System.getProperty("java.home"));

            @Override
            public void checkPermission(Permission perm) {
                if (perm instanceof FilePermission) {
                    final File file = new File(perm.getName());
                    if (file.equals(root.getParentFile())) {
                        throw new SecurityException("You may not read " + file.getAbsolutePath());
                    }
                }
            }

        };
        System.setSecurityManager(sm);

    }

    public void tearDown() throws IOException {
        System.setSecurityManager(null);
        deltree(root);
    }

    public File getRoot() {
        return root;
    }

    public Map<String, File> getFiles() {
        return files;
    }

    private void createTestFiles() throws IOException {

        mkdir(root, "d1", 100);
        mkdir(root, "d2", 0);
        mkdir(root, "d3", 200);
        mkdir(files.get("d1"), "d4", 100);
        mkdir(files.get("d1"), "d5", 150);
        mkdir(files.get("d3"), "d6", 100);
        mkdir(files.get("d3"), "d7", 200);
        mkdir(files.get("d7"), "d8", 300);
        mkdir(files.get("d3"), "d9", 400);

        mkfile("d4", "f1", 100);
        mkfile("d4", "f2", 200);
        mkfile("d5", "f3", 100);
        mkfile("d6", "f4", 100);

        mkimage("d1", "p1", 100, "gif");
        mkimage("d1", "p2", 200, "bmp");
        mkimage("d5", "p3", 100, "jpg");
        mkimage("d2", "p4", 400, "jpeg");
        mkimage("d2", "p5", 100, "png");
        mkimage("d2", "p6", 200, "png");
        mkimage("d6", "p7", 200, "png");
        mkimage("d6", "p8", 100, "png");
        mkimage("d8", "p9", 100, "png");
        mkimage("d8", "p10", 200, "png");
        mkimage("d9", "p11", 800, "png");

        setDirTimes();

    }

    private void mkimage(String dirName, String fileName, int timeInSeconds, String fileType) throws IOException {

        File parent = files.get(dirName);
        File file = new File(parent, fileName + "." + fileType);
        String format = "jpeg".equals(fileType) ? "jpg" : fileType;

        int size = 200;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, size, size);
            g2d.setColor(Color.black);
            g2d.drawString(file.getName(), 20, 80);
        } finally {
            g2d.dispose();
        }
        ImageIO.write(image, format, file);

        if (!file.setLastModified(1000L * timeInSeconds)) {
            throw new IOException("Could not set time of image file " + file.getAbsolutePath());
        }
        files.put(fileName, file);

    }

    private void mkfile(String dirName, String fileName, int timeInSeconds) throws IOException {
        File parent = files.get(dirName);
        File file = new File(parent, fileName);
        if (!file.createNewFile()) {
            throw new IOException("Could not create plain file " + file.getAbsolutePath());
        }
        if (!file.setLastModified(1000L * timeInSeconds)) {
            throw new IOException("Could not set time of plain file " + file.getAbsolutePath());
        }
        files.put(fileName, file);
    }

    private void mkdir(File parent, String name, int timeInSeconds) throws IOException {
        File result = new File(parent, name);
        if (!result.mkdirs()) {
            throw new IOException("Could not create directory " + result.getAbsolutePath());
        }
        dirTimes.put(result, 1000L * timeInSeconds);
        files.put(name, result);
    }

    private void setDirTimes() throws IOException {
        for (Map.Entry<File, Long> entry : dirTimes.entrySet()) {
            if (entry.getKey().lastModified() != entry.getValue()) {
                if (!entry.getKey().setLastModified(entry.getValue())) {
                    throw new IOException("Could not set time of directory " + entry.getKey().getAbsolutePath());
                }
            }
        }
    }

    private void deltree(File dir) throws IOException {
        // taken from https://stackoverflow.com/a/27917071/725192
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}