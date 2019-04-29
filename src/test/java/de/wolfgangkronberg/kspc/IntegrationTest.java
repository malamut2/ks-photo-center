package de.wolfgangkronberg.kspc;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTest {

    private final TestFileCreator tfc = new TestFileCreator();
    private File home;
    private Map<String, File> files;

    @BeforeAll
    void setup() throws IOException {
        tfc.init();
        files = tfc.getFiles();
        File root = tfc.getRoot();
        home = new File(root, "home");
        if (!home.mkdirs()) {
            throw new IOException("Cannot create folder " + home.getAbsolutePath());
        }
    }

    @AfterAll
    void tearDown() throws IOException {
        tfc.tearDown();
    }

    @Test
    @Order(10)
    void testMain() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Class<?> main = Class.forName("Main");
            String[] params = new String[]{
                    "--home=" + home.getAbsolutePath(),
                    files.get("p5").getAbsolutePath()
            };
            Method mainMethod = main.getMethod("main", params.getClass());
            mainMethod.invoke(null, (Object) params);
        });
    }

}
