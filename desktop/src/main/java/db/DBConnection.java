package db;

import model.AppLogger;

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

public class DBConnection {

    private final DBConfig config;

    public DBConnection() {
        this.config = new DBConfig();
    }

    public Connection connect() throws SQLException {
        try {
            return DriverManager.getConnection(
                    config.getDbUrl(),
                    config.getDbUsername(),
                    config.getDbPassword()
            );
        } catch (SQLException e) {
            AppLogger.error("DB-Connection error: " + e.getMessage());
            throw e;
        }
    }

    public boolean isConnectionAvailable() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isConnectionAvailable(String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean setConfig(String url, String username, String password) {
        return config.update(url, username, password);
    }

    public String getURL() {
        return config.getDbUrl();
    }

    public String getUsername() {
        return config.getDbUsername();
    }

    public String getPassword() {
        return config.getDbPassword();
    }
}
