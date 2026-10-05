package services;

import db.DBConnection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

class GetProjectsService {
    private static StringBuilder STRING_BUILDER = new StringBuilder();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * This method gets all projects from the PostgreSQL Database.
     * It returns them as a TreeMap of Projects.
     * The key is made from project number * 100 + version number.
     */
    static TreeSet<Project> get(DBConnection database) {
        TreeSet<Project> projects = new TreeSet<>();
        if (!database.isConnectionAvailable()) {
            System.err.println("Keine Datenbankverbindung verfügbar.");
            return projects;
        }

        STRING_BUILDER.append("SELECT ");
        for (int i = 0; i < ProjectValues.values().length; i++) {
            ProjectValues value = ProjectValues.values()[i];
            STRING_BUILDER.append(value.getSqlColumn()).append(", ");
        }
        STRING_BUILDER.append("data FROM projects WHERE active = true ORDER BY project_nr");

        String sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (Connection conn = database.connect();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String data = rs.getString("data");

                Map<String, Integer> jsonMap = OBJECT_MAPPER.readValue(data, new TypeReference<>() {
                });

                TreeMap<Integer, Integer> map = new TreeMap<>(new BKPComparator());
                jsonMap.forEach((k, v) -> map.put(Integer.parseInt(k), v));

                ProjectData projectData = new ProjectData();
                projectData.set(map);

                Project project = new Project();
                project.setData(projectData);

                for (int i = 0; i < ProjectValues.values().length; i++) {
                    ProjectValues value = ProjectValues.values()[i];
                    project.set(value, getData(rs, value.getSqlColumn(), value.getType()));
                }

                projects.add(project);
            }
        } catch (SQLException e) {
            AppLogger.error("SQL-DB Problem: " + e);
        } catch (JsonProcessingException e) {
            AppLogger.error("JSON Problem: " + e);
            throw new RuntimeException(e);
        }
        return projects;
    }

    private static <T> T getData(ResultSet rs, String column, Class<?> type) throws SQLException {
        return (T) rs.getObject(column, type);
    }

}
