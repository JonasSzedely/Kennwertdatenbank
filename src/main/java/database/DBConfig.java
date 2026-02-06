package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The DatabaseConfig class is responsible for loading database configuration from the db.properties file.
 * The DatabaseConfig has three static methods that expose the database configuration:
 *     getDbUrl() – Return the database URL.
 *     getDbUsername() – Return the username.
 *     getDbPassword() – Return the password.
 * A db.properties file in the src directory of the project is needed with
 *     db.url=Database URL
 *     db.username=Your Username
 *     db.password=Your Password
 * code from: https://neon.com/postgresql/postgresql-jdbc/connecting-to-postgresql-database
 */
public class DBConfig {
    private static final Properties properties = new Properties();
    private static final String PROPERTIES_PATH = "src/main/resources/db.properties";

    public static void loadProperties(){
        try {
            // Versuche zuerst aus der Datei zu laden (damit Änderungen erkannt werden)
            FileInputStream fileInput = new FileInputStream(PROPERTIES_PATH);
            properties.load(fileInput);
            fileInput.close();
        } catch (IOException e) {
            // Fallback: Lade aus Classpath (für Deployment)
            try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    System.out.println("Sorry, unable to find db.properties");
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
}
