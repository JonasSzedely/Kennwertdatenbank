package model;

import java.sql.*;
import java.util.HashSet;
import java.util.TreeMap;

public class Controller {
    private TreeMap<Integer, Project> PROJECTS;
    private StringBuilder STRING_BUILDER = new StringBuilder();
    private int minTotalCost;
    private int maxTotalCost;
    private int minApartments;
    private int maxApartments;
    private double averageRatioUG;
    private int averageWindowRatio;
    private int minVolume;
    private int maxVolume;
    private final DB database = new DB();

    public Controller() {
        PROJECTS = new TreeMap<>();
        if (!initializeDatabase()) {
            System.err.println("Controller läuft im Offline-Modus");
            return;
        }
        checkAndRepairColumns();
        PROJECTS = getProjects();
    }

    public boolean initializeDatabase() {
        if (!isDatabaseAvailable()) {
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

        try (Connection conn = connectorDB(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            PROJECTS = getProjects();
            System.out.println("Tabelle erfolgreich erstellt oder schon vorhanden.");
            return true;
        } catch (SQLException e) {
            System.err.println("DB-Initialisierung fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }

    public boolean isDatabaseAvailable() {
        return database.isConnectionAvailable();
    }

    public boolean testDBConnection(String url, String username, String password) {
        return database.isConnectionAvailable(url, username, password);
    }

    public Connection connectorDB() throws SQLException {
        return database.connect();
    }

    public String addProject(Project project) {
        if (isDatabaseAvailable()) {
            return AddProject.add(this, project);
        }
        return "Keine Datenbankverbindung verfügbar. Projekte können nicht hinzugefügt werden.";
    }

    public String modifyProject(Project project) {
        if (isDatabaseAvailable()) {
            return ModifyProject.modify(this, project);
        }
        return "Keine Datenbankverbindung verfügbar. Projekte kann nicht geändert werden.";
    }

    public String deleteProject(int projectNr, int version) {
        if (!isDatabaseAvailable()) {
            return "Keine Datenbankverbindung verfügbar. Projekte kann nicht gelöscht werden.";
        }
        String sql = "UPDATE projects SET active = false WHERE project_nr = ? AND version = ?";
        try (Connection conn = database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, projectNr);
            pstmt.setInt(2, version);
            int editedRow = pstmt.executeUpdate();
            if (editedRow > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " Version Nr. " + rs.getInt(2) + " wurde entfernt";
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Projekt konnte nicht entfernt werden. " + projectNr + " Version " + version + " " + e.getMessage());
            return "Projekt konnte nicht entfernt werden";
        }
        return "Projekt konnte nicht entfernt werden";
    }

    public TreeMap<Integer, Project> getProjects() {
        if (!isDatabaseAvailable()) {
            return PROJECTS;
        }
        PROJECTS = new TreeMap<>();
        PROJECTS = GetProjects.get(this);
        return PROJECTS;
    }

    public void calculate() {
        minTotalCost = Integer.MAX_VALUE;
        maxTotalCost = 0;
        minApartments = Integer.MAX_VALUE;
        maxApartments = 0;
        averageRatioUG = 0;
        averageWindowRatio = 0;
        minVolume = Integer.MAX_VALUE;
        maxVolume = 0;

        if (PROJECTS == null || PROJECTS.isEmpty()) {
            System.out.println("Keine Projekte zum Berechnen vorhanden");
            return;
        }

        for (Project project : PROJECTS.values()) {
            int cost = project.getData().getTotalCost();
            int apartments = project.get(ProjectValues.APARTMENTS_NR);
            int volumeUG = project.get(ProjectValues.VOLUME_UNDERGROUND);
            int volumeOG = project.get(ProjectValues.VOLUME_ABOVE_GROUND);
            int volume = volumeUG + volumeOG;
            int windowArea = project.get(ProjectValues.WINDOW_AREA);
            int facadeArea = project.get(ProjectValues.FACADE_AREA);

            averageRatioUG += (double) volumeUG / volumeOG;
            minTotalCost = Math.min(cost, minTotalCost);
            maxTotalCost = Math.max(cost, maxTotalCost);
            minApartments = Math.min(apartments, minApartments);
            maxApartments = Math.max(apartments, maxApartments);
            averageWindowRatio += (int) (((double) windowArea / (double) facadeArea) * 100);
            minVolume = Math.min(volume, minVolume);
            maxVolume = Math.max(volume, maxVolume);
        }
        averageRatioUG /= PROJECTS.size();
        averageWindowRatio /= PROJECTS.size();
    }

    public void checkAndRepairColumns() {
        if (!isDatabaseAvailable()) {
            System.err.println("Keine DB-Verbindung für Spaltenprüfung.");
        }
        HashSet<String> existingColumns = new HashSet<>();
        try (Connection conn = connectorDB()) {
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
                    String colType   = value.getType() == Integer.class ? "INT" : "VARCHAR(255)";

                    String sql = "ALTER TABLE projects ADD COLUMN IF NOT EXISTS "
                            + colName + " " + colType + " NOT NULL DEFAULT " + defaultVal;

                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(sql);
                    }
                }
            }

            if (!existingColumns.contains("data")){
                String sql = "ALTER TABLE projects ADD COLUMN IF NOT EXISTS data JSONB NOT NULL DEFAULT '{}'";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }
            }

            if (!existingColumns.contains("active")){
                String sql = "ALTER TABLE projects ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Spaltenprüfung fehlgeschlagen: " + e.getMessage());
        }
    }

    public int getMinVolume() {
        return minVolume;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public int getMinTotalCost() {
        return minTotalCost;
    }

    public int getMaxTotalCost() {
        return maxTotalCost;
    }

    public int getMinApartments() {
        return minApartments;
    }

    public int getMaxApartments() {
        return maxApartments;
    }

    public double getAverageRatioUG() {
        return averageRatioUG;
    }

    public int getAverageWindowRatio() {
        return averageWindowRatio;
    }

    public String getDBUrl() {
        return database.getURL();
    }

    public String getDBUsername() {
        return database.getUsername();
    }

    public String getDBPassword() {
        return database.getPassword();
    }

    public boolean setDBConfig(String url, String username, String password) {
        return database.setConfig(url, username, password);
    }
}
