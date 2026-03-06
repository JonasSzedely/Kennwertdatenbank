package database;

import java.io.*;
import java.util.Properties;

/**
 * The DatabaseConfig class is responsible for loading database configuration from the db.properties file.
 * The DatabaseConfig has three static methods that expose the database configuration:
 * getDbUrl() – Return the database URL.
 * getDbUsername() – Return the username.
 * getDbPassword() – Return the password.
 * A db.properties file in the src directory of the project is needed with
 * db.url=Database URL
 * db.username=Your Username
 * db.password=Your Password
 */
public class DBConfig {
    private static final Properties properties = new Properties();
    private static final String PROPERTIES_PATH = System.getProperty("user.dir") + "/db.properties";
    private static boolean loaded = false;

    private static void createDefaultProperties(File file) throws IOException {
        file.getParentFile().mkdirs();
        Properties defaultProps = new Properties();
        defaultProps.setProperty("db.url", "jdbc:postgresql://localhost:5432/kennwertdatenbank");
        defaultProps.setProperty("db.username", "postgres");
        defaultProps.setProperty("db.password", "password");
        try (FileOutputStream out = new FileOutputStream(file)) {
            defaultProps.store(out, "Datenbank Konfiguration");
        }
    }

    public static void loadProperties() {
        if (loaded) return;

        File file = new File(PROPERTIES_PATH);
        if (!file.exists()) {
            try {
                createDefaultProperties(file);
            } catch (IOException e) {
                System.err.println("Konnte db.properties nicht erstellen: " + e.getMessage());
            }
        }
        try {
            FileInputStream fileInput = new FileInputStream(PROPERTIES_PATH);
            properties.load(fileInput);
            fileInput.close();
            loaded = true;
        } catch (IOException e) {
            try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    System.out.println("Konnte db.properties nicht finden");
                    return;
                }
                properties.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public static String getPropertiesPath() {
        return PROPERTIES_PATH;
    }

    public static void reloadProperties() {
        loaded = false;
        loadProperties();
    }
}
