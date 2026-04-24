package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

class GetProjects {
    private static StringBuilder STRING_BUILDER = new StringBuilder();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * This method gets all projects from the PostgreSQL Database.
     * It returns them as a TreeMap of Projects.
     * The key is made from project number * 100 + version number.
     */
    public static TreeMap<Integer, Project> get(Controller controller) {

        TreeMap<Integer, Project> PROJECTS = new TreeMap<>();

        STRING_BUILDER.append("SELECT ");
        for (int i = 0; i < ProjectValues.values().length; i++) {
            ProjectValues value = ProjectValues.values()[i];
            STRING_BUILDER.append(value.getSqlColumn()).append(", ");
        }
        STRING_BUILDER.append("data FROM projects WHERE active = true ORDER BY project_nr");

        String sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (Connection conn = controller.connectorDB();
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

                int number = project.get(ProjectValues.PROJECT_NR);
                int version = project.get(ProjectValues.VERSION);

                PROJECTS.put(((number * 100) + version), project);
            }
        } catch (SQLException e) {
            AppLogger.error("SQL-DB Problem: " + e);
        } catch (JsonProcessingException e) {
            AppLogger.error("JSON Problem: " + e);
            throw new RuntimeException(e);
        }
        return PROJECTS;
    }

    private static <T> T getData(ResultSet rs, String column, Class<?> type) throws SQLException {
        return (T) rs.getObject(column, type);
    }

}
