package kennwertdatenbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.DB;

import java.sql.SQLException;
import java.sql.Statement;


public class Projects {

    /**
     * This method inserts a new row into the products table.
     * Tt needs a valid project from Type Project.
     * code from https://neon.com/postgresql/postgresql-jdbc/insert
     */
    public static int add(Project project) {

        //Initialize an INSERT statement.
        //The question mark (?) is a placeholder that will be replaced by the actual values later.
        var sql = "INSERT INTO projects(projectnr, address, plz, location, owner, type, squaremeter, data) "
                + "VALUES(?,?,?,?,?,?::property_type,?,?)";


        try (var conn =  DB.connect();
             var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // bind the values
            pstmt.setInt(1, project.getProjectNr());
            pstmt.setString(2, project.getAddress());
            pstmt.setInt(3, project.getPlz());
            pstmt.setString(4, project.getLocation());
            pstmt.setString(5, project.getOwner());
            pstmt.setString(6, project.getPropertyType().toString());
            pstmt.setInt(7, project.getBathroomNr());

            //turns the TreeMap data into a Jsonb format (this part was written by claude.ai)
            ObjectMapper objectMapper = new ObjectMapper();
            String jacksonData = objectMapper.writeValueAsString(project.getData());
            pstmt.setObject(8, jacksonData, java.sql.Types.OTHER);

            // execute the INSERT statement and get the inserted id
            int insertedRow = pstmt.executeUpdate();
            if (insertedRow > 0) {
                var rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }


    //code from https://neon.com/postgresql/postgresql-jdbc/query
    /**
     * This method gets all projects from the PostgreSQL Database.
     * It returns them as a ArrayList of Projects.
     * code from https://neon.com/postgresql/postgresql-jdbc/insert
     */
    /*
    public static ArrayList<Project> getAll() {
        var projects = new ArrayList<Project>();

        var sql = "SELECT projectnr, address, plz, location, owner, type, squaremeter, data FROM projects ORDER BY projectnr";

        try (var conn =  DB.connect();
             var stmt = conn.createStatement()) {

            var rs = stmt.executeQuery(sql);

            while (rs.next()) {
                var project = new Project(
                        rs.getInt("projectnr"),
                        rs.getString("address"),
                        rs.getInt("plz"),
                        rs.getString("location"),
                        rs.getString("owner"),
                        //rs.getString("type"),
                        PropertyType.OWN,
                        rs.getInt("squaremeter"),
                        //rs.getObject("data"));
                        "C:\\Users\\Jonas\\Nextcloud\\Jonas\\07_Programmieren\\Java\\Kennwertdatenbank\\src\\main\\resources\\kv1.csv");
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

     */


}
