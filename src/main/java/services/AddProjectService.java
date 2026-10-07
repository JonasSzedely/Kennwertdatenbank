package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.DBConnection;
import model.Project;
import model.ProjectAttributes;
import model.ProjectVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Inserts new projects into the database.
 */
class AddProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(AddProjectService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AddProjectService() {
    }

    /**
     * Inserts a project as a new version into the {@code projects} table.
     * <p>
     * The next free version number is determined automatically and set on the
     * given project. The cost data is stored as JSON in the {@code data} column.
     *
     * @param project  the project to insert, must have a project number and cost data
     * @param database the database connection to use
     * @return a status message for the user describing the result
     */
    static String add(Project project, DBConnection database) {
        if (!database.isConnectionAvailable()) {
            return "Keine Datenbankverbindung verfügbar.";
        }

        int projectNr = project.get(ProjectAttributes.PROJECT_NR);
        int version = ProjectVersion.get(projectNr, database);
        int maxVersion = ProjectAttributes.VERSION.getMax();

        if (version < 1) {
            return "Version konnte nicht ermittelt werden.";
        }
        if (version > maxVersion) {
            return "Projekt Nr. " + projectNr + " hat bereits die maximale Anzahl von "
                    + maxVersion + " Versionen.";
        }
        project.set(ProjectAttributes.VERSION, version);

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(project.getData().getData());
        } catch (JsonProcessingException e) {
            LOG.error("Could not convert cost data of project {} to JSON", projectNr, e);
            return "JSON-Konvertierungsfehler: " + e.getMessage();
        }

        try (Connection conn = database.connect();
             PreparedStatement pstmt = conn.prepareStatement(buildInsertSql())) {

            int index = 1;
            for (ProjectAttributes attribute : ProjectAttributes.values()) {
                Object value = project.get(attribute);
                if (value == null) {
                    pstmt.setNull(index++, Types.NULL);
                } else if (attribute.getType() == Integer.class) {
                    pstmt.setInt(index++, (Integer) value);
                } else {
                    pstmt.setString(index++, (String) value);
                }
            }
            pstmt.setObject(index, json, Types.OTHER);

            if (pstmt.executeUpdate() > 0) {
                LOG.info("Added project {} version {}", projectNr, version);
                return "Projekt Nr. " + projectNr + " Version " + version + " wurde hinzugefügt.";
            }
        } catch (SQLException e) {
            LOG.error("Could not add project {} version {}", projectNr, version, e);
            return "SQL-Fehler: " + e.getMessage();
        }

        return "Projekt konnte nicht hinzugefügt werden.";
    }

    /**
     * Builds the INSERT statement for all project attributes and the cost data.
     *
     * @return the SQL statement with one placeholder per column
     */
    private static String buildInsertSql() {
        StringBuilder sb = new StringBuilder("INSERT INTO projects(");
        for (ProjectAttributes attribute : ProjectAttributes.values()) {
            sb.append(attribute.getSqlColumn()).append(",");
        }
        sb.append("data) VALUES(");
        sb.repeat("?,", ProjectAttributes.values().length);
        sb.append("?)");
        return sb.toString();
    }
}