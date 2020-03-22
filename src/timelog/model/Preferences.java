package timelog.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class Preferences {
    public static final String DEFAULTS_FILE_NAME = "defaults.properties";
    public static final String FILE_NAME = "preferences.properties";
    private static final Properties PROPERTIES = new Properties();

    private Preferences() {
    }

    public static void loadPropertiesFile(String fileName) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(Path.of(fileName), StandardOpenOption.CREATE)) {
            PROPERTIES.load(inputStream);
        } catch (NoSuchFileException ignored) {
        }
    }

    public static void savePropertiesFile(String fileName) throws IOException {
        try (final OutputStream outputStream = Files.newOutputStream(Path.of(fileName), StandardOpenOption.CREATE)) {
            PROPERTIES.store(outputStream, "");
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static void set(String key, String value) {
        PROPERTIES.setProperty(key, value);
    }
}
