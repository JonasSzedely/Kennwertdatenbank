package model;

import db.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Determines the next version number of a project.
 */
public final class ProjectVersion {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectVersion.class);

    private ProjectVersion() {
    }

    /**
     * Returns the next version number for the given project.
     * <p>
     * Removed versions are included, so version numbers are never reused.
     * The result is not checked against {@link ProjectAttributes#VERSION}'s maximum.
     *
     * @param projectNr the project number
     * @param database  the database connection to use
     * @return the highest existing version plus one, {@code 1} for a new project,
     *         or {@code -1} if the query failed
     */
    public static int get(int projectNr, DBConnection database) {
        String sql = "SELECT MAX(version) FROM projects WHERE "
                + ProjectAttributes.PROJECT_NR.getSqlColumn() + " = ?";

        try (Connection conn = database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectNr);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) + 1;
                }
            }
        } catch (SQLException e) {
            LOG.error("Could not determine next version of project {}", projectNr, e);
        }
        return -1;
    }
}