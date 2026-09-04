package db;

import model.AppLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

class DBConfig {

    private static final Path CONFIG_DIR = resolveConfigDir();
    private static final Path PROPERTIES_PATH = CONFIG_DIR.resolve("db.properties");

    private static final String DEFAULT_URL = "jdbc:postgresql://ip/db-name";
    private static final String DEFAULT_USERNAME = "username";
    private static final String DEFAULT_PASSWORD = "password";

    private final Properties properties = new Properties();

    DBConfig() {
        load();
    }

    private static Path resolveConfigDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "Kennwertdatenbank");
        }
        return Path.of(System.getProperty("user.home"), ".kennwertdatenbank");
    }

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
            AppLogger.error("Error loading db.properties: " + e.getMessage());
            setDefaults();
        }
    }

    private void setDefaults() {
        properties.setProperty("url", DEFAULT_URL);
        properties.setProperty("username", DEFAULT_USERNAME);
        properties.setProperty("password", DEFAULT_PASSWORD);
    }

    private boolean saveToFile(File file) {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            AppLogger.error("Config directory could not be created: " + e.getMessage());
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "DB configuration");
            return true;
        } catch (IOException e) {
            AppLogger.error("Error when saving db.properties: " + e.getMessage());
            return false;
        }
    }

    private boolean save() {
        return saveToFile(PROPERTIES_PATH.toFile());
    }

    public String getDbUrl() {
        return properties.getProperty("url");
    }

    public String getDbUsername() {
        return properties.getProperty("username");
    }

    public String getDbPassword() {
        String pw = PasswordStore.load();
        return pw != null ? pw : "password";
    }

    public boolean update(String url, String username, String password) {
        properties.setProperty("url", url);
        properties.setProperty("username", username);

        return save() && PasswordStore.save(password);
    }
}