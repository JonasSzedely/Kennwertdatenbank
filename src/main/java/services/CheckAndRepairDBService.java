package model;

import java.sql.*;
import java.util.HashSet;


class CheckAndRepairDB {

    static String check() {
        DBService database = new DBService();
        if (!database.isConnectionAvailable()) {
            return ("Keine Datenbankverbindung verfügbar.");
        }
        HashSet<String> existingColumns = new HashSet<>();
        try (Connection conn = database.connect()) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getColumns(null, null, "projects", null)) {
                while (rs.next()) {
                    existingColumns.add(rs.getString("COLUMN_NAME"));
                }
            }

            for (ProjectValues value : ProjectValues.values()) {
                String colName = value.getSqlColumn().toLowerCase();

                if (!existingColumns.contains(colName)) {
                    String defaultVal = value.getType() == Integer.class ? "-1" : "'xyz'";
                    String colType = value.getType() == Integer.class ? "INT" : "VARCHAR(255)";

                    String sql = "ALTER TABLE projects ADD COLUMN IF NOT EXISTS "
                            + colName + " " + colType + " NOT NULL DEFAULT " + defaultVal;

                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(sql);
                    }
                }
            }

            if (!existingColumns.contains("data")) {
                String sql = "ALTER TABLE projects ADD COLUMN IF NOT EXISTS data JSONB NOT NULL DEFAULT '{}'";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }
            }

            if (!existingColumns.contains("active")) {
                String sql = "ALTER TABLE projects ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }
            }

            return "DB ist synchron.";
        } catch (SQLException e) {
            AppLogger.error("Spaltenprüfung fehlgeschlagen: " + e.getMessage());
        }
        return "DB konnte nicht geprüft werden.";
    }
}
