package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.*;

class AddProject {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * This method inserts a new row into the products table.
     * @param project an object of type Project
     * @param SQL_PROJECT_DATA the list of project data definied in the controller
     */
    public static String add(Controller controller, Project project, String[] SQL_PROJECT_DATA) {

        ArrayList<Object> values = new ArrayList<>(List.of(project.getAttributes()));

        if (values.size() != SQL_PROJECT_DATA.length) {
            return "Projekt konnte nicht hinzugefügt werden. Datenfehler, bitte Support kontaktieren.";
        }
        // set the latest version
        values.set(1,ProjectVersion.get(controller, project.getProjectNr()));

        // prepare SQL-Statement
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO projects(");

        // compute SQL_PROJECT_DATA
        for (int i = 0; i < SQL_PROJECT_DATA.length - 1; i++) {
            sqlBuilder.append(SQL_PROJECT_DATA[i].split(",")[0]).append(",");
        }
        sqlBuilder.append(SQL_PROJECT_DATA[SQL_PROJECT_DATA.length - 1].split(",")[0]);
        sqlBuilder.append(") VALUES(");

        for (int i = 0; i < values.size() - 1; i++) {
            sqlBuilder.append("?,");
        }
        sqlBuilder.append("?)");

        String sql = sqlBuilder.toString();

        try (Connection conn = controller.connectorDB();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                String[] parts = SQL_PROJECT_DATA[i].split(",", 2);
                String columnName = parts[0];
                String dataType = parts[1];

                if (value == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                    continue;
                }

                switch (dataType) {
                    case "int":
                        if (value instanceof Number) {
                            pstmt.setInt(i + 1, ((Number) value).intValue());
                        } else {
                            return "Datenfehler bei " + columnName + ": erwartet Integer, bekommen " + value.getClass().getSimpleName();
                        }
                        break;

                    case "string":
                        if (value instanceof String) {
                            pstmt.setString(i + 1, (String) value);
                        } else {
                            pstmt.setString(i + 1, String.valueOf(value));
                        }
                        break;

                    case "json":
                        try {
                            String jsonString = OBJECT_MAPPER.writeValueAsString(project.getData().getData());
                            pstmt.setObject(i + 1, jsonString, Types.OTHER);
                        } catch (JsonProcessingException e) {
                            return "JSON-Konvertierungsfehler bei " + columnName + ": " + e.getMessage();
                        }
                        break;

                    default:
                        return "Datenfehler, unbekannter Datentyp: " + dataType + " für Spalte " + columnName;
                }
            }
            //insert into DB
            int insertedRow = pstmt.executeUpdate();
            if (insertedRow > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " Version " + rs.getInt(2) + " wurde hinzugefügt.";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL-Fehler: " + e.getMessage();
        }

        return "Projekt konnte nicht hinzugefügt werden";
    }
}