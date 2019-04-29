package de.wolfgangkronberg.kspc;

import com.sun.javafx.application.ParametersImpl;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(ApplicationExtension.class)
class IntegrationTest {

    private final TestFileCreator tfc = new TestFileCreator();
    private File home;
    private Map<String, File> files;
    private App app;

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
    void tearDown() throws IOException, InterruptedException {
        Thread.sleep(500);  // give JavaFX some time to shut down and free DB files
        tfc.tearDown();
    }

    @Start
    private void start(Stage stage) {
        String[] params = new String[]{
                "--home=" + home.getAbsolutePath(),
                files.get("p5").getAbsolutePath()
        };
        app = new App();
        ParametersImpl.registerParameters(app, new ParametersImpl(params));
        app.start(stage);
    }

    @Test
    @Order(10)
    void testMain() throws InterruptedException, TimeoutException {
        Exchanger<List> exch = new Exchanger<>();
        GlobalElements ge = app.getGlobalElements();
        Platform.runLater(() -> {
            ge.getApplicationLayout().escape();
            try {
                exch.exchange(ge.getFilesystem().getPane().getSelectionModel().getSelectedItems());
            } catch (InterruptedException e) {
                System.err.println("Unexpected Interruption.");
                e.printStackTrace();
            }
        });
        List result = exch.exchange(null, 3000, TimeUnit.SECONDS);
        assertEquals(1, result.size());
    }

}
