package model;

import java.io.*;
import java.util.Properties;


class DBConfig {

    private static final String PROPERTIES_PATH = System.getProperty("user.dir") + "/db.properties";
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/kennwertdatenbank";
    private static final String DEFAULT_USERNAME = "postgres";
    private static final String DEFAULT_PASSWORD = "password";

    private final Properties properties = new Properties();

    DBConfig() {
        load();
    }

    private void load() {
        File file = new File(PROPERTIES_PATH);
        if (!file.exists()) {
            setDefaults();
            saveToFile(file);
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            AppLogger.error("Error loading db.properties: " + e.getMessage());
            System.err.println("Fehler beim Laden von db.properties: " + e.getMessage());
            setDefaults();
        }
    }

    private void setDefaults() {
        properties.setProperty("db.url", DEFAULT_URL);
        properties.setProperty("db.username", DEFAULT_USERNAME);
        properties.setProperty("db.password", DEFAULT_PASSWORD);
    }

    private boolean saveToFile(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            AppLogger.error("Directory could not be created: " + parent);
            System.err.println("Verzeichnis konnte nicht erstellt werden: " + parent);
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Datenbank Konfiguration");
            return true;
        } catch (IOException e) {
            AppLogger.error("Error when saving db.properties " + e.getMessage());
            System.err.println("Fehler beim Speichern von db.properties: " + e.getMessage());
            return false;
        }
    }

    private boolean save() {
        return saveToFile(new File(PROPERTIES_PATH));
    }

    public String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public boolean update(String url, String username, String password) {
        properties.setProperty("db.url", url);
        properties.setProperty("db.username", username);
        properties.setProperty("db.password", password);
        return save();
    }
}
