package services;

import db.DBConnection;
import model.Project;
import model.ProjectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Updates existing project versions in the database.
 */
class ModifyProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyProjectService.class);
    private static final String SQL = buildUpdateSql();

    private ModifyProjectService() {
    }

    /**
     * Updates the attributes of an existing project version.
     * <p>
     * The project is identified by its project number and version, which are
     * not changed. The cost data is not updated, since changed costs require
     * a new version.
     *
     * @param project  the project with the updated attributes
     * @param database the database connection to use
     * @return a status message for the user describing the result
     */
    static String modify(Project project, DBConnection database) {
        if (!database.isConnectionAvailable()) {
            return "Keine Datenbankverbindung verfügbar.";
        }

        int projectNr = project.get(ProjectAttributes.PROJECT_NR);
        int version = project.get(ProjectAttributes.VERSION);

        try (Connection conn = database.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            int index = 1;
            for (ProjectAttributes attribute : ProjectAttributes.values()) {
                if (isKey(attribute)) {
                    continue;
                }
                pstmt.setObject(index++, project.get(attribute));
            }
            pstmt.setInt(index++, projectNr);
            pstmt.setInt(index, version);

            if (pstmt.executeUpdate() > 0) {
                LOG.info("Modified project {} version {}", projectNr, version);
                return "Projekt Nr. " + projectNr + " Version " + version + " wurde angepasst.";
            }
            LOG.warn("Project {} version {} not found or removed", projectNr, version);
        } catch (SQLException e) {
            LOG.error("Could not modify project {} version {}", projectNr, version, e);
        }
        return "Projekt konnte nicht angepasst werden.";
    }

    /**
     * Returns whether the attribute is part of the primary key.
     *
     * @param attribute the attribute to check
     * @return {@code true} for project number and version
     */
    private static boolean isKey(ProjectAttributes attribute) {
        return attribute == ProjectAttributes.PROJECT_NR || attribute == ProjectAttributes.VERSION;
    }

    /**
     * Builds the UPDATE statement for all attributes except the primary key.
     *
     * @return the SQL statement with one placeholder per updated column and
     *         two placeholders for project number and version
     */
    private static String buildUpdateSql() {
        StringBuilder sb = new StringBuilder("UPDATE projects SET ");
        boolean first = true;
        for (ProjectAttributes attribute : ProjectAttributes.values()) {
            if (isKey(attribute)) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            sb.append(attribute.getSqlColumn()).append(" = ?");
            first = false;
        }
        sb.append(" WHERE ").append(ProjectAttributes.PROJECT_NR.getSqlColumn()).append(" = ?")
                .append(" AND ").append(ProjectAttributes.VERSION.getSqlColumn()).append(" = ?")
                .append(" AND active = true");
        return sb.toString();
    }
}