package model;

import java.sql.*;

public class DeleteProjectService {

    static String delete(int projectNr, int version) {
        DBService database = new DBService();
        if (!database.isConnectionAvailable()) {
            return ("Keine Datenbankverbindung verfügbar.");
        }

        String sql = "UPDATE projects SET active = false WHERE project_nr = ? AND version = ?";
        try (Connection conn = database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, projectNr);
            pstmt.setInt(2, version);
            int editedRow = pstmt.executeUpdate();
            if (editedRow > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " Version Nr. " + rs.getInt(2) + " wurde entfernt";
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Projekt konnte nicht entfernt werden. " + projectNr + " Version " + version + " " + e.getMessage());
            return "Projekt konnte nicht entfernt werden";
        }
        return "Projekt konnte nicht entfernt werden";
    }
}
