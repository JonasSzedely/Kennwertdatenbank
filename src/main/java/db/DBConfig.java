package db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Holds the database connection settings.
 * <p>
 * The URL and username are stored in a {@code db.properties} file in the user's
 * configuration directory. The password is stored in the operating system's
 * keyring via {@link PasswordStore} and cached in memory after the first read.
 */
class DBConfig {

    private static final Path CONFIG_DIR = resolveConfigDir();
    private static final Path PROPERTIES_PATH = CONFIG_DIR.resolve("db.properties");

    private static final String DEFAULT_URL = "jdbc:postgresql://ip/db-name";
    private static final String DEFAULT_USERNAME = "username";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String URL = "db.url";
    private static final String USERNAME = "db.username";
    private static final String PASSWORD = "db.password";

    private final Properties properties = new Properties();
    private static final Logger LOG = LoggerFactory.getLogger(DBConfig.class);

    /** Cached password, read once from the keyring and updated on {@link #update}. */
    private volatile String password;

    /**
     * Creates a configuration and loads the stored settings.
     * <p>
     * If no configuration file exists, one is created with default values.
     * The password is read from the keyring once and cached.
     */
    DBConfig() {
        load();
        password = loadPassword();
    }

    /**
     * Determines the directory for the configuration file.
     *
     * @return {@code %APPDATA%\Kennwertdatenbank} if {@code APPDATA} is set,
     *         otherwise {@code ~/.kennwertdatenbank}
     */
    private static Path resolveConfigDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "Kennwertdatenbank");
        }
        return Path.of(System.getProperty("user.home"), ".kennwertdatenbank");
    }

    /**
     * Loads the settings from the configuration file.
     * <p>
     * Creates the file with default values if it does not exist. Falls back to
     * the default values if the file cannot be read.
     */
    private void load() {
        File file = PROPERTIES_PATH.toFile();
        if (!file.exists()) {
            setDefaults();
            saveToFile(file);
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            LOG.error("Error loading db.properties", e);
            setDefaults();
        }
    }

    /**
     * Reads the password from the operating system's keyring.
     *
     * @return the stored password, or a default value if none is stored
     */
    private static String loadPassword() {
        String pw = PasswordStore.load();
        return pw != null ? pw : DEFAULT_PASSWORD;
    }

    /**
     * Sets all properties to their default values.
     */
    private void setDefaults() {
        properties.setProperty(URL, DEFAULT_URL);
        properties.setProperty(USERNAME, DEFAULT_USERNAME);
        properties.setProperty(PASSWORD, DEFAULT_PASSWORD);
    }

    /**
     * Writes the current properties to the given file.
     * <p>
     * Creates the configuration directory if necessary.
     *
     * @param file the target file
     * @return {@code true} if the file was written successfully, {@code false} otherwise
     */
    private boolean saveToFile(File file) {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            LOG.error("Config directory could not be created", e);
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Datenbank Konfiguration");
            return true;
        } catch (IOException e) {
            LOG.error("Error when saving db.properties", e);
            return false;
        }
    }

    /**
     * Writes the current properties to the default configuration file.
     *
     * @return {@code true} if the file was written successfully, {@code false} otherwise
     */
    private boolean save() {
        return saveToFile(PROPERTIES_PATH.toFile());
    }

    /**
     * Returns the configured JDBC URL.
     *
     * @return the database URL
     */
    public String getDbUrl() {
        return properties.getProperty(URL);
    }

    /**
     * Returns the configured database user.
     *
     * @return the database username
     */
    public String getDbUsername() {
        return properties.getProperty(USERNAME);
    }

    /**
     * Returns the cached database password.
     * <p>
     * The keyring is not accessed by this method.
     *
     * @return the database password
     */
    public String getDbPassword() {
        return password;
    }

    /**
     * Updates and persists the connection settings.
     * <p>
     * The URL and username are written to the configuration file, the password
     * to the operating system's keyring. The cached password is only replaced
     * if it was stored in the keyring successfully.
     *
     * @param url      the JDBC URL of the database
     * @param username the database user
     * @param password the password of the database user
     * @return {@code true} if all settings were saved successfully, {@code false} otherwise
     */
    public boolean update(String url, String username, String password) {
        properties.setProperty(URL, url);
        properties.setProperty(USERNAME, username);

        if (save() && PasswordStore.save(password)) {
            this.password = password;
            return true;
        }
        return false;
    }
}