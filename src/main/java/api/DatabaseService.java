package api;

/**
 * Manages the database connection and its configuration.
 */
public interface DatabaseService {

    /**
     * Creates the required tables, metadata and triggers if they do not exist yet.
     * <p>
     * Existing data is not modified.
     *
     * @return {@code true} if the database was initialized successfully,
     *         {@code false} if no connection is available or an error occurred
     */
    boolean initializeDatabase();

    /**
     * Stores new connection settings.
     * <p>
     * The settings are persisted and used for all subsequent connections.
     *
     * @param url      the JDBC URL of the database
     * @param username the database user
     * @param password the password of the database user
     * @return {@code true} if the settings were saved successfully, {@code false} otherwise
     */
    boolean setDBConfig(String url, String username, String password);

    /**
     * Tests whether a connection can be established with the given settings.
     * <p>
     * The current configuration is not changed.
     *
     * @param url      the JDBC URL of the database
     * @param username the database user
     * @param password the password of the database user
     * @return {@code true} if a connection could be established, {@code false} otherwise
     */
    boolean testDBConnection(String url, String username, String password);

    /**
     * Returns the configured JDBC URL.
     *
     * @return the database URL
     */
    String getDBUrl();

    /**
     * Returns the configured database user.
     *
     * @return the database username
     */
    String getDBUsername();

    /**
     * Returns the configured database password.
     *
     * @return the database password
     */
    String getDBPassword();
}