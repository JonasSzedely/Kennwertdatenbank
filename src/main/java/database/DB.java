package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DB class has the static method, connect(), which connects to the sales database on the local PostgreSQL server.
 * The connect() method utilizes the DatabaseConfig class to load the connection parameters and establish a connection to the database using the getConnection() method of the DriverManager class.
 * The connect() method returns a Connection object if it successfully established a connection to PostgreSQL, or null otherwise.
 * If any SQLException occurs during the connection process, the connect() method displays the details of the exception.
 * code from: https://neon.com/postgresql/postgresql-jdbc/connecting-to-postgresql-database
 */

public class DB {
    private static boolean connectionAvailable = true;

    public static Connection connect() throws SQLException {

        try {
            // Get database credentials from DatabaseConfig class
            DBConfig.loadProperties();
            var jdbcUrl = DBConfig.getDbUrl();
            var user = DBConfig.getDbUsername();
            var password = DBConfig.getDbPassword();

            // Open a connection
            Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
            connectionAvailable = true; // Verbindung erfolgreich
            return conn;

        } catch (SQLException  e) {
            System.err.println("DB-Verbindungsfehler: " + e.getMessage());
            connectionAvailable = false;
            throw e;
        }
    }

    /**
     * Test if DB connection is possible
     */
    public static boolean isConnectionAvailable() {
        try {
            DBConfig.loadProperties();
            Connection conn = connect();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    /**
     * will not test the connection.
     * @return returns last known connection status.
     */
    public static boolean wasLastConnectionSuccessful() {
        return connectionAvailable;
    }
}
