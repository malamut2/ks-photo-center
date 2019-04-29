package de.wolfgangkronberg.kspc.files;

import de.wolfgangkronberg.kspc.TestFileCreator;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilesystemFileScannerTest {

    private final TestFileCreator tfc = new TestFileCreator();
    private Map<String, File> files;

    @BeforeAll
    void setup() throws IOException {
        tfc.init();
        files = tfc.getFiles();
    }

    @AfterAll
    void tearDown() throws IOException {
        tfc.tearDown();
    }

    @Test
    @Order(10)
    void allFileTypesTested() {
        Set<String> remaining = new HashSet<>();
        for (String type : ImageFileFilter.allowed) {
            remaining.add("." + type);
        }
        for (File file : files.values()) {
            String name = file.getName().toLowerCase();
            for (String type : remaining) {
                if (name.endsWith(type)) {
                    remaining.remove(type);
                    break;
                }
            }
        }
        assertTrue(remaining.isEmpty(), "Unused image types should be empty but is " + remaining.toString());
    }

    @Test
    @Order(100)
    void simpleFileScannerAlphabeticalSequence() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {

            FileScanner scanner = new SimpleFileScanner(files.get("p5"), true);
            scanner.start(null);

            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p4"), scanner.getPrevious(2));
            assertEquals(get("p6"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p4"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p5", "p6"), scanner.getNext(2));

            assertFalse(scanner.moveToPrevious());
            assertEquals(files.get("p4"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p5", "p6"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p4"), scanner.getPrevious(2));
            assertEquals(get("p6"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p6"), scanner.getCurrent());
            assertEquals(get("p4", "p5"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

            assertFalse(scanner.moveToNext());
            assertEquals(files.get("p6"), scanner.getCurrent());
            assertEquals(get("p4", "p5"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

        });
    }

    @Test
    @Order(110)
    void simpleFileScannerTimeSequence() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {

            FileScanner scanner = new SimpleFileScanner(files.get("p6"), false);
            scanner.start(null);

            assertEquals(files.get("p6"), scanner.getCurrent());
            assertEquals(get("p4"), scanner.getPrevious(2));
            assertEquals(get("p5"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p4"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p6", "p5"), scanner.getNext(2));

            assertFalse(scanner.moveToPrevious());
            assertEquals(files.get("p4"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p6", "p5"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p6"), scanner.getCurrent());
            assertEquals(get("p4"), scanner.getPrevious(2));
            assertEquals(get("p5"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p4", "p6"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

            assertFalse(scanner.moveToNext());
            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p4", "p6"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

        });
    }

    @Test
    @Order(200)
    void traverseTreeFileScannerAlphabeticalSequence() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {

            FileScanner scanner = new TraverseTreeFileScanner(files.get("p5"), true, 100);
            scanner.start(null);

            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p3", "p4"), scanner.getPrevious(2));
            assertEquals(get("p6", "p7"), scanner.getNext(2));
            assertEquals(get("p1", "p2", "p3", "p4"), scanner.getPrevious(100));
            assertEquals(get("p6", "p7", "p8", "p10", "p9", "p11"), scanner.getNext(100));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p4"), scanner.getCurrent());
            assertEquals(get("p2", "p3"), scanner.getPrevious(2));
            assertEquals(get("p5", "p6"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p3"), scanner.getCurrent());
            assertEquals(get("p1", "p2"), scanner.getPrevious(2));
            assertEquals(get("p4", "p5"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p2"), scanner.getCurrent());
            assertEquals(get("p1"), scanner.getPrevious(2));
            assertEquals(get("p3", "p4"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p1"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p2", "p3"), scanner.getNext(2));

            assertFalse(scanner.moveToPrevious());
            assertEquals(files.get("p1"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p2", "p3"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p2"), scanner.getCurrent());
            assertEquals(get("p1"), scanner.getPrevious(2));
            assertEquals(get("p3", "p4"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p3"), scanner.getCurrent());
            assertEquals(get("p1", "p2"), scanner.getPrevious(2));
            assertEquals(get("p4", "p5"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p4"), scanner.getCurrent());
            assertEquals(get("p2", "p3"), scanner.getPrevious(2));
            assertEquals(get("p5", "p6"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p3", "p4"), scanner.getPrevious(2));
            assertEquals(get("p6", "p7"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p6"), scanner.getCurrent());
            assertEquals(get("p4", "p5"), scanner.getPrevious(2));
            assertEquals(get("p7", "p8"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p7"), scanner.getCurrent());
            assertEquals(get("p5", "p6"), scanner.getPrevious(2));
            assertEquals(get("p8", "p10"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p8"), scanner.getCurrent());
            assertEquals(get("p6", "p7"), scanner.getPrevious(2));
            assertEquals(get("p10", "p9"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p10"), scanner.getCurrent());
            assertEquals(get("p7", "p8"), scanner.getPrevious(2));
            assertEquals(get("p9", "p11"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p9"), scanner.getCurrent());
            assertEquals(get("p8", "p10"), scanner.getPrevious(2));
            assertEquals(get("p11"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p11"), scanner.getCurrent());
            assertEquals(get("p10", "p9"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

            assertFalse(scanner.moveToNext());
            assertEquals(files.get("p11"), scanner.getCurrent());
            assertEquals(get("p10", "p9"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

        });
    }

    @Test
    @Order(210)
    void traverseTreeFileScannerTimeSequence() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {

            FileScanner scanner = new TraverseTreeFileScanner(files.get("p1"), false, 100);
            scanner.start(null);

            assertEquals(files.get("p1"), scanner.getCurrent());
            assertEquals(get("p8", "p2"), scanner.getPrevious(2));
            assertEquals(get("p3", "p4"), scanner.getNext(2));
            assertEquals(get("p11", "p10", "p9", "p7", "p8", "p2"), scanner.getPrevious(100));
            assertEquals(get("p3", "p4", "p6", "p5"), scanner.getNext(100));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p2"), scanner.getCurrent());
            assertEquals(get("p7", "p8"), scanner.getPrevious(2));
            assertEquals(get("p1", "p3"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p8"), scanner.getCurrent());
            assertEquals(get("p9", "p7"), scanner.getPrevious(2));
            assertEquals(get("p2", "p1"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p7"), scanner.getCurrent());
            assertEquals(get("p10", "p9"), scanner.getPrevious(2));
            assertEquals(get("p8", "p2"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p9"), scanner.getCurrent());
            assertEquals(get("p11", "p10"), scanner.getPrevious(2));
            assertEquals(get("p7", "p8"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p10"), scanner.getCurrent());
            assertEquals(get("p11"), scanner.getPrevious(2));
            assertEquals(get("p9", "p7"), scanner.getNext(2));

            assertTrue(scanner.moveToPrevious());
            assertEquals(files.get("p11"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p10", "p9"), scanner.getNext(2));

            assertFalse(scanner.moveToPrevious());
            assertEquals(files.get("p11"), scanner.getCurrent());
            assertEquals(get(), scanner.getPrevious(2));
            assertEquals(get("p10", "p9"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p10"), scanner.getCurrent());
            assertEquals(get("p11"), scanner.getPrevious(2));
            assertEquals(get("p9", "p7"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p9"), scanner.getCurrent());
            assertEquals(get("p11", "p10"), scanner.getPrevious(2));
            assertEquals(get("p7", "p8"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p7"), scanner.getCurrent());
            assertEquals(get("p10", "p9"), scanner.getPrevious(2));
            assertEquals(get("p8", "p2"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p8"), scanner.getCurrent());
            assertEquals(get("p9", "p7"), scanner.getPrevious(2));
            assertEquals(get("p2", "p1"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p2"), scanner.getCurrent());
            assertEquals(get("p7", "p8"), scanner.getPrevious(2));
            assertEquals(get("p1", "p3"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p1"), scanner.getCurrent());
            assertEquals(get("p8", "p2"), scanner.getPrevious(2));
            assertEquals(get("p3", "p4"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p3"), scanner.getCurrent());
            assertEquals(get("p2", "p1"), scanner.getPrevious(2));
            assertEquals(get("p4", "p6"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p4"), scanner.getCurrent());
            assertEquals(get("p1", "p3"), scanner.getPrevious(2));
            assertEquals(get("p6", "p5"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p6"), scanner.getCurrent());
            assertEquals(get("p3", "p4"), scanner.getPrevious(2));
            assertEquals(get("p5"), scanner.getNext(2));

            assertTrue(scanner.moveToNext());
            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p4", "p6"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

            assertFalse(scanner.moveToNext());
            assertEquals(files.get("p5"), scanner.getCurrent());
            assertEquals(get("p4", "p6"), scanner.getPrevious(2));
            assertEquals(get(), scanner.getNext(2));

        });
    }

    private List<File> get(String... args) {
        return Arrays.stream(args).map(files::get).collect(Collectors.toList());
    }

}