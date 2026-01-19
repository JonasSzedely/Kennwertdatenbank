package database;

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
 *     db.url=DatabaseURL
 *     db.username=Yourusername
 *     db.password=YourPassword
 * code from: https://neon.com/postgresql/postgresql-jdbc/connecting-to-postgresql-database
 */
public class DBConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find db.properties");
                System.exit(1);
            }

            // Load the properties file
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
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
