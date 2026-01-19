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
    public static Connection connect() throws SQLException {

        try {
            // Get database credentials from DatabaseConfig class
            var jdbcUrl = DBConfig.getDbUrl();
            var user = DBConfig.getDbUsername();
            var password = DBConfig.getDbPassword();

            // Open a connection
            return DriverManager.getConnection(jdbcUrl, user, password);

        } catch (SQLException  e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
