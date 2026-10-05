package services;

import db.DBConnection;
import model.*;

import java.sql.*;

class ModifyProjectService {

    static String modify(Project project, DBConnection database) {
        if (!database.isConnectionAvailable()) {
            return ("Keine Datenbankverbindung verfügbar.");
        }

        StringBuilder sb = new StringBuilder("UPDATE projects SET ");

        boolean first = true;
        for (ProjectValues value : ProjectValues.values()) {
            if (value == ProjectValues.PROJECT_NR || value == ProjectValues.VERSION) {
                continue;
            }
            if (!first) sb.append(", ");
            sb.append(value.getSqlColumn()).append(" = ?");
            first = false;
        }

        sb.append(" WHERE ")
                .append(ProjectValues.PROJECT_NR.getSqlColumn()).append(" = ?")
                .append(" AND ")
                .append(ProjectValues.VERSION.getSqlColumn()).append(" = ?");

        String sql = sb.toString();

        try (Connection conn = database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;

            for (ProjectValues value : ProjectValues.values()) {
                if (value == ProjectValues.PROJECT_NR || value == ProjectValues.VERSION) {
                    continue;
                }
                pstmt.setObject(index++, project.get(value));
            }

            pstmt.setObject(index++, project.get(ProjectValues.PROJECT_NR));
            pstmt.setObject(index,   project.get(ProjectValues.VERSION));

            if (pstmt.executeUpdate() > 0) {
                int nr      = project.get(ProjectValues.PROJECT_NR);
                int version = project.get(ProjectValues.VERSION);
                return "Projekt Nr. " + nr + " Version " + version + " wurde angepasst.";
            } else {
                return "Projekt konnte nicht angepasst werden";
            }

        } catch (SQLException e) {
            AppLogger.error("SQL-DB problem when modifying Project: "
                    + project.get(ProjectValues.PROJECT_NR)
                    + " Version " + project.get(ProjectValues.VERSION) + e);
        }
        return "Projekt konnte nicht angepasst werden";
    }
}