package db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections to the PostgreSQL database.
 * <p>
 * Connection settings are provided by {@link DBConfig}.
 *
 * @see <a href="https://neon.com/postgresql/postgresql-jdbc/connecting-to-postgresql-database">
 *      Connecting to the PostgreSQL Database</a>
 */
public class DBConnection {

    private final DBConfig config;
    private static final Logger LOG = LoggerFactory.getLogger(DBConnection.class);

    /**
     * Creates a connection factory using the stored configuration.
     */
    public DBConnection() {
        this.config = new DBConfig();
    }

    /**
     * Opens a new connection using the configured settings.
     * <p>
     * The caller is responsible for closing the connection.
     *
     * @return a new database connection
     * @throws SQLException if the connection cannot be established
     */
    public Connection connect() throws SQLException {
        try {
            return DriverManager.getConnection(
                    config.getDbUrl(),
                    config.getDbUsername(),
                    config.getDbPassword()
            );
        } catch (SQLException e) {
            LOG.error("DB connection error", e);
            throw e;
        }
    }

    /**
     * Checks whether a connection can be established with the configured settings.
     *
     * @return {@code true} if a connection could be established, {@code false} otherwise
     */
    public boolean isConnectionAvailable() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Checks whether a connection can be established with the given settings.
     * <p>
     * The stored configuration is not changed.
     *
     * @param url      the JDBC URL of the database
     * @param username the database user
     * @param password the password of the database user
     * @return {@code true} if a connection could be established, {@code false} otherwise
     */
    public boolean isConnectionAvailable(String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Updates and persists the connection settings.
     *
     * @param url      the JDBC URL of the database
     * @param username the database user
     * @param password the password of the database user
     * @return {@code true} if all settings were saved successfully, {@code false} otherwise
     * @see DBConfig#update(String, String, String)
     */
    public boolean setConfig(String url, String username, String password) {
        return config.update(url, username, password);
    }

    /**
     * Returns the configured JDBC URL.
     *
     * @return the database URL
     */
    public String getURL() {
        return config.getDbUrl();
    }

    /**
     * Returns the configured database user.
     *
     * @return the database username
     */
    public String getUsername() {
        return config.getDbUsername();
    }

    /**
     * Returns the configured database password.
     * <p>
     * The value is served from the cache in {@link DBConfig}.
     *
     * @return the database password
     */
    public String getPassword() {
        return config.getDbPassword();
    }
}