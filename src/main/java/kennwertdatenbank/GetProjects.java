package kennwertdatenbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

public class GetProjects {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static StringBuilder STRING_BUILDER = new StringBuilder();

    /**
     * This method gets all projects from the PostgreSQL Database.
     * It returns them as a TreeMap of Projects.
     * The key is made from project number * 100 + version number.
     */
    public static TreeMap<Integer, Project> get(String[] SQL_PROJECT_DATA) {

        TreeMap<Integer, Project> PROJECTS = new TreeMap<>();

        STRING_BUILDER.append("SELECT ");
        for (int i = 0; i < SQL_PROJECT_DATA.length - 1; i++) {
            STRING_BUILDER.append(SQL_PROJECT_DATA[i].split(",")[0]).append(", ");
        }
        STRING_BUILDER.append(SQL_PROJECT_DATA[SQL_PROJECT_DATA.length - 1].split(",")[0]).append(" FROM projects WHERE active = true ORDER BY project_nr");
        String sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String data = rs.getString("data");

                Map<String, Integer> jsonMap = OBJECT_MAPPER.readValue(data, new TypeReference<Map<String, Integer>>() {
                });

                TreeMap<Integer, Integer> map = new TreeMap<>(new BKPComparator());
                jsonMap.forEach((k, v) -> map.put(Integer.parseInt(k), v));

                ProjectData projectData = new ProjectData(map);

                Project project = new Project(
                        rs.getInt("project_nr"),
                        rs.getInt("version"),
                        rs.getString("address"),
                        rs.getInt("plz"),
                        rs.getString("location"),
                        rs.getString("owner"),
                        rs.getString("property_type"),
                        rs.getString("construction_type"),
                        rs.getInt("document_phase"),
                        rs.getInt("calculation_phase"),
                        rs.getInt("apartments_nr"),
                        rs.getInt("bathroom_nr"),
                        rs.getInt("hnf"),
                        rs.getInt("gf"),
                        rs.getInt("volume_underground"),
                        rs.getInt("volume_above_ground"),
                        rs.getInt("facadearea"),
                        rs.getInt("windowarea"),
                        rs.getString("facade_type"),
                        rs.getString("window_type"),
                        rs.getString("roof_type"),
                        rs.getString("heating_type"),
                        rs.getString("cooling_type"),
                        rs.getString("ventilation_type_apartments"),
                        rs.getString("ventilation_type_ug"),
                        rs.getString("co_no"),
                        rs.getString("special"),
                        projectData
                );
                PROJECTS.put((project.getProjectNr() * 100) + project.getVersion(), project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return PROJECTS;
    }
}
