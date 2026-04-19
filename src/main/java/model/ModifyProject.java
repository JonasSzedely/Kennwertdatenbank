package model;

import java.sql.*;

class ModifyProject {

    /**
     * This method does modify a project in the DB.
     */
    public static String modify(Controller controller, Project project){
        //Initialize an INSERT statement.
        //The question mark (?) is a placeholder that will be replaced by the actual values later.
        String sql = (
                "UPDATE projects SET " +
                        "address = ?," +
                        "plz = ?," +
                        "location = ?," +
                        "owner = ?," +
                        "property_type = ?," +
                        "construction_type = ?," +
                        "document_phase = ?," +
                        "calculation_phase = ?," +
                        "apartments_nr = ?," +
                        "bathroom_nr = ?," +
                        "hnf = ?," +
                        "gf = ?," +
                        "volume_underground = ?," +
                        "volume_above_ground = ?," +
                        "facadearea = ?," +
                        "windowarea = ?," +
                        "facade_type = ?," +
                        "window_type = ?," +
                        "roof_type = ?," +
                        "heating_type = ?," +
                        "cooling_type = ?," +
                        "ventilation_type_apartments = ?," +
                        "ventilation_type_ug = ?," +
                        "co_no = ?," +
                        "special = ?" +
                        " WHERE project_nr = ? AND version = ?");


        try (Connection conn = controller.connectorDB(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // bind the values
            pstmt.setString(1, project.getAddress());
            pstmt.setInt(2, project.getPlz());
            pstmt.setString(3, project.getLocation());
            pstmt.setString(4, project.getOwner());
            pstmt.setString(5, project.getPropertyType());
            pstmt.setString(6, project.getConstructionType());
            pstmt.setInt(7, project.getDocumentPhase());
            pstmt.setInt(8, project.getCalculationPhase());
            pstmt.setInt(9, project.getApartmentsNr());
            pstmt.setInt(10, project.getBathroomNr());
            pstmt.setInt(11, project.getHnf());
            pstmt.setInt(12, project.getGf());
            pstmt.setInt(13, project.getVolumeUnderground());
            pstmt.setInt(14, project.getVolumeAboveGround());
            pstmt.setInt(15, project.getFacadeArea());
            pstmt.setInt(16, project.getWindowArea());
            pstmt.setString(17, project.getFacadeType());
            pstmt.setString(18, project.getWindowType());
            pstmt.setString(19, project.getRoofType());
            pstmt.setString(20, project.getHeatingType());
            pstmt.setString(21, project.getCoolingType());
            pstmt.setString(22, project.getVentilationTypeApartments());
            pstmt.setString(23, project.getVentilationTypeUg());
            pstmt.setString(24, project.getCoNo());
            pstmt.setString(25, project.getSpecial());

            pstmt.setInt(26, project.getProjectNr());
            pstmt.setInt(27, project.getVersion());

            // execute the INSERT statement and check if it was successful
            if (pstmt.executeUpdate() > 0) {
                return "Projekt Nr. " + project.getProjectNr() + " Version " + project.getVersion() + " wurde angepasst.";
            } else {
                return "Projekt konnte nicht angepasst werden";
            }
        } catch (SQLException e) {
            AppLogger.error("SQL-DB problem when modifying Project: " + project.getProjectNr() + " Version " + project.getVersion() + e);
        }
        return "Projekt konnte nicht angepasst werden";
    }
}