package de.wolfgangkronberg;

import lombok.Data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Data
public class AppProperties {

    /**
     * The directory in which we store application-wide information, e.g. the image library
     */
    private File home = new File(System.getProperty("user.home"), ".ksPhotoCenter");

    /**
     * The file from which we read the properties defined in this class on startup
     */
    private File props = new File(home, "app.properties");

    /**
     * The Navigation strategy which is applied if the app is opened without specifying any image file as a
     * command line parameter
     */
    private NavigationStrategy defaultNavStrategy = NavigationStrategy.LibraryByTimePerDir;

    /**
     * The Navigation strategy which is applied if the app is opened with a specific image file specified as a
     * command line parameter
     */
    private NavigationStrategy openFileNavStrategy = NavigationStrategy.TraverseTreeAlphabetical;

    /**
     * The Navigation strategy which is applied if the currently active strategy is invalid, e.g. because our
     * current image is not in the library, but a library-type strategy is chosen
     */
    private NavigationStrategy failoverNavStrategy = NavigationStrategy.TraverseTreeAlphabetical;

    public void loadParameters(Map<String, String> cmdLine) {
        Map<String, String> newProps = new HashMap<>(cmdLine);
        String settingsFile = newProps.remove("props");
        if (settingsFile != null) {
            props = new File(settingsFile);
        }
        if (props.isFile()) {  // it is not an error if settings file does not exist
            Properties p = new Properties();
            try (Reader r = new FileReader(props, StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (IOException e) {
                System.err.println("Could not fully read props: " + e.toString());
                System.err.println("Attempting to continue anyway...");
            }
            for (String key: p.stringPropertyNames()) {
                setParameter(key, p.getProperty(key));
            }
        }
        for (Map.Entry<String, String> entry : newProps.entrySet()) {
            setParameter(entry.getKey(), entry.getValue());
        }
    }

    private void setParameter(String key, String value) {
        Field field;
        try {
            field = this.getClass().getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            System.err.println("Ignoring unknown property '" + key + "'.");
            return;
        }
        Object oValue;
        try {
            oValue = parseValueToType(field.getType(), value);
        } catch (IllegalArgumentException e) {
            System.err.println("Ignoring invalid property '" + key + "' = '" + value + "'.");
            return;
        }
        field.setAccessible(true);
        try {
            field.set(this, oValue);
        } catch (IllegalAccessException e) {
            System.err.println("Access denied on setting property '"
                    + key + "' = '" + value + "'. Proceeding anyway...");
        }
    }

    private Object parseValueToType(Class<?> type, String value) throws IllegalArgumentException {
        if (type == File.class) {
            return new File(value);
        } else if (type == NavigationStrategy.class) {
            return NavigationStrategy.valueOf(value);  // throws IllegalArgumentException on invalid value
        } else {
            throw new RuntimeException("Internal error: unknown AppProperties type: " + type.getName());
        }
    }

}