package model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class InitializeDB {

    private static StringBuilder STRING_BUILDER = new StringBuilder();

    static boolean initialize() {
        DBService database = new DBService();
        if (!database.isConnectionAvailable()) {
            return false;
        }
        STRING_BUILDER.append("CREATE TABLE IF NOT EXISTS projects(");

        for (ProjectValues value : ProjectValues.values()) {
            STRING_BUILDER.append(value.getSqlColumn());
            if (value.getType() == Integer.class) {
                STRING_BUILDER.append(" INT NOT NULL,");
            } else {
                STRING_BUILDER.append(" VARCHAR(255) NOT NULL,");
            }
        }
        STRING_BUILDER.append("data JSONB NOT NULL,");
        STRING_BUILDER.append("active BOOLEAN NOT NULL DEFAULT true, CHECK (project_nr > 9999), PRIMARY KEY(project_nr, version))");

        String sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (Connection conn = database.connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Tabelle erfolgreich erstellt oder schon vorhanden.");
            return true;
        } catch (SQLException e) {
            System.err.println("DB-Initialisierung fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }
}
