package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;

class AddProject {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * This method inserts a new row into the products table.
     *
     * @param project an object of type Project
     */
    static String add(Controller controller, Project project) {

        // Version vor dem INSERT setzen
        project.set(ProjectValues.VERSION, ProjectVersion.get(controller, project.get(ProjectValues.PROJECT_NR)));

        // INSERT SQL aufbauen
        StringBuilder sb = new StringBuilder("INSERT INTO projects(");
        for (ProjectValues value : ProjectValues.values()) {
            sb.append(value.getSqlColumn()).append(",");
        }
        sb.append("data) VALUES(");
        sb.repeat("?,", ProjectValues.values().length);
        sb.append("?)");

        String sql = sb.toString();

        try (Connection conn = controller.connectorDB();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int index = 1;
            for (ProjectValues value : ProjectValues.values()) {
                Object val = project.get(value);
                if (val == null) {
                    pstmt.setNull(index++, Types.NULL);
                } else if (value.getType() == Integer.class) {
                    pstmt.setInt(index++, (Integer) val);
                } else {
                    pstmt.setString(index++, (String) val);
                }
            }

            // data-Spalte (JSON) separat – gleiche Logik wie GetProjects
            try {
                String jsonString = OBJECT_MAPPER.writeValueAsString(project.getData().getData());
                pstmt.setObject(index, jsonString, Types.OTHER);
            } catch (JsonProcessingException e) {
                return "JSON-Konvertierungsfehler: " + e.getMessage();
            }

            int insertedRow = pstmt.executeUpdate();
            if (insertedRow > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " Version " + rs.getInt(2) + " wurde hinzugefügt.";
                }
            }

        } catch (SQLException e) {
            AppLogger.error("SQL-DB problem when adding Project: " + e.getMessage());
            return "SQL-Fehler: " + e.getMessage();
        }

        return "Projekt konnte nicht hinzugefügt werden";
    }
}