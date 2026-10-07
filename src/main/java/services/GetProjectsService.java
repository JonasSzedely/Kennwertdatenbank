package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.DBConnection;
import model.BKPComparator;
import model.Project;
import model.ProjectAttributes;
import model.ProjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Loads projects from the database.
 */
class GetProjectsService {

    private static final Logger LOG = LoggerFactory.getLogger(GetProjectsService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Integer>> COST_TYPE = new TypeReference<>() {
    };
    private static final String SQL = buildSelectSql();

    private GetProjectsService() {
    }

    /**
     * Loads all active projects including their cost data.
     * <p>
     * Rows that cannot be read are skipped and logged, so a single corrupt
     * project does not prevent the others from loading.
     *
     * @param database the database connection to use
     * @return all active projects ordered by project number and version,
     *         empty if the database is not available
     */
    static TreeSet<Project> get(DBConnection database) {
        TreeSet<Project> projects = new TreeSet<>();
        if (!database.isConnectionAvailable()) {
            LOG.warn("Projects not loaded, no database connection");
            return projects;
        }

        try (Connection conn = database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                try {
                    projects.add(readProject(rs));
                } catch (JsonProcessingException | RuntimeException e) {
                    LOG.error("Skipped project {} version {}, row could not be read",
                            rs.getObject(ProjectAttributes.PROJECT_NR.getSqlColumn()),
                            rs.getObject(ProjectAttributes.VERSION.getSqlColumn()), e);
                }
            }
        } catch (SQLException e) {
            LOG.error("Could not load projects", e);
        }

        LOG.debug("Loaded {} projects", projects.size());
        return projects;
    }

    /**
     * Creates a project from the current row of the result set.
     *
     * @param rs the result set positioned on a project row
     * @return the project with all attributes and cost data
     * @throws SQLException            if a column cannot be read
     * @throws JsonProcessingException if the cost data is not valid JSON
     */
    private static Project readProject(ResultSet rs) throws SQLException, JsonProcessingException {
        Project project = new Project();
        for (ProjectAttributes attribute : ProjectAttributes.values()) {
            project.set(attribute, rs.getObject(attribute.getSqlColumn(), attribute.getType()));
        }
        project.setData(readCostData(rs.getString("data")));
        return project;
    }

    /**
     * Converts the JSON cost data into a {@link ProjectData} object.
     *
     * @param json the cost data as JSON, with BKP codes as keys and CHF amounts as values
     * @return the cost data ordered by {@link BKPComparator}
     * @throws JsonProcessingException if the JSON is invalid
     * @throws NumberFormatException   if a key is not a valid BKP code
     */
    private static ProjectData readCostData(String json) throws JsonProcessingException {
        Map<String, Integer> jsonMap = OBJECT_MAPPER.readValue(json, COST_TYPE);

        TreeMap<Integer, Integer> map = new TreeMap<>(new BKPComparator());
        jsonMap.forEach((code, amount) -> map.put(Integer.parseInt(code), amount));

        ProjectData data = new ProjectData();
        data.set(map);
        return data;
    }

    /**
     * Builds the SELECT statement for all project attributes and the cost data.
     *
     * @return the SQL statement
     */
    private static String buildSelectSql() {
        StringBuilder sb = new StringBuilder("SELECT ");
        for (ProjectAttributes attribute : ProjectAttributes.values()) {
            sb.append(attribute.getSqlColumn()).append(", ");
        }
        sb.append("data FROM projects WHERE active = true");
        return sb.toString();
    }
}