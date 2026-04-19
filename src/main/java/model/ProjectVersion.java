package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class ProjectVersion {

    public static int get(Controller controller, int projectNr){
        try (Connection conn = controller.connectorDB();
             PreparedStatement pstmt = conn.prepareStatement("SELECT MAX(version) FROM projects WHERE project_nr = ?")) {

            pstmt.setInt(1, projectNr);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int maxVersion = rs.getInt(1);
                return maxVersion + 1;
            }
        } catch (SQLException e) {
            AppLogger.error("DB-Error: " + e.getMessage());
        }
        return -1;
    }
}