package services;

import db.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Removes project versions from the database.
 * <p>
 * Projects are not deleted physically but marked as inactive, so they can be
 * restored directly in the database if needed. Inactive projects are no longer
 * loaded by the application.
 */
class DeleteProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteProjectService.class);
    private static final String SQL =
            "UPDATE projects SET active = false WHERE project_nr = ? AND version = ? AND active = true";

    private DeleteProjectService() {
    }

    /**
     * Marks a project version as inactive.
     *
     * @param projectNr the project number
     * @param version   the version to remove
     * @param database  the database connection to use
     * @return a status message for the user describing the result
     */
    static String delete(int projectNr, int version, DBConnection database) {
        if (!database.isConnectionAvailable()) {
            return "Keine Datenbankverbindung verfügbar.";
        }

        try (Connection conn = database.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, projectNr);
            pstmt.setInt(2, version);

            if (pstmt.executeUpdate() > 0) {
                LOG.info("Removed project {} version {}", projectNr, version);
                return "Projekt Nr. " + projectNr + " Version " + version + " wurde entfernt.";
            }
            LOG.warn("Project {} version {} not found or already removed", projectNr, version);
        } catch (SQLException e) {
            LOG.error("Could not remove project {} version {}", projectNr, version, e);
        }
        return "Projekt konnte nicht entfernt werden.";
    }
}