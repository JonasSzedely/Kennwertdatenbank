package model;

import java.sql.*;

class ModifyProject {

    public static String modify(Controller controller, Project project) {

        // UPDATE SQL dynamisch aufbauen – gleiche Logik wie GetProjects
        StringBuilder sb = new StringBuilder("UPDATE projects SET ");

        boolean first = true;
        for (ProjectValues value : ProjectValues.values()) {
            // PROJECT_NR und VERSION kommen in die WHERE-Klausel, nicht in SET
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

        try (Connection conn = controller.connectorDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;

            // SET-Werte binden
            for (ProjectValues value : ProjectValues.values()) {
                if (value == ProjectValues.PROJECT_NR || value == ProjectValues.VERSION) {
                    continue;
                }
                pstmt.setObject(index++, project.get(value));
            }

            // WHERE-Werte binden
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