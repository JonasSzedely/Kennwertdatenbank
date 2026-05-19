package model;

import db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectVersion {
    public static int get(int projectNr){
        DBConnection database = new DBConnection();

        try(Connection conn = database.connect();
            PreparedStatement pstmt = conn.prepareStatement("SELECT MAX(version) FROM projects WHERE " + ProjectValues.PROJECT_NR.getSqlColumn() + " = ?")){
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