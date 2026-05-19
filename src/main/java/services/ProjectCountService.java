package services;

import db.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProjectCountService {

    static boolean validate(int count, DBConnection database) {
        if (!database.isConnectionAvailable()) {
            System.err.println("Keine Datenbankverbindung verfügbar.");
        }
        String sql = "SELECT COUNT(*) FROM projects WHERE active = true";
        try (Connection conn = database.connect()) {
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1) == count;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
