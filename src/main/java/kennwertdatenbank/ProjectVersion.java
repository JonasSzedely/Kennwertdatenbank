package kennwertdatenbank;

import database.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectVersion {

    public static int get(int projectNr){
        try (Connection conn = DB.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT MAX(version) FROM projects WHERE project_nr = ?")) {

            pstmt.setInt(1, projectNr);
            ResultSet rs = pstmt.executeQuery();

            //if the same project exists, the highest version will be set. (1,3,4 -> 5)
            if (rs.next()) {
                int maxVersion = rs.getInt(1); //if no project exist it will be 0s
                return maxVersion + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
