package kennwertdatenbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.DB;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static kennwertdatenbank.PropertyType.OWN;
import static kennwertdatenbank.PropertyType.RENT;

public class Controller {
    ArrayList<Project> projects;
    public Controller(){
        var type = "CREATE TYPE property_type AS ENUM ('RENT', 'OWN')";
        //var status = "CREATE TYPE status AS ENUM ('BAUKOSTENPLANUNG', 'BAUPROJEKT', 'NACHKALKULATION')";
        var sql =
                "CREATE TABLE IF NOT EXISTS projects(" +
                        "projectnr INT PRIMARY KEY," +
                        "address VARCHAR(255) NOT NULL," +
                        "plz INT NOT NULL," +
                        "location VARCHAR(255) NOT NULL," +
                        "owner VARCHAR(255) NOT NULL," +
                        "type PROPERTY_TYPE NOT NULL," +
                        "squaremeter INT NOT NULL," +
                        "data JSONB NOT NULL," +
                        "CHECK (projectnr > 9999)" +
                        ")";

        try (var conn =  DB.connect();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(type);
            stmt.executeUpdate(sql);
            System.out.println("Tabelle erfolgreich erstellt!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        //var project1 = new Project(10005, "NeuesProjektWeg", 8004, "Zürich", "BesitzerD", RENT, 2053, "C:\\Users\\Jonas\\Nextcloud\\Jonas\\07_Programmieren\\Java\\Kennwertdatenbank\\src\\main\\resources\\kv1.csv");
        //Projects.add(project1);


        projects = getProjects();
    }


    /**
     * This method gets all projects from the PostgreSQL Database.
     * It returns them as a ArrayList of Projects.
     * code from https://neon.com/postgresql/postgresql-jdbc/insert
     */
    public ArrayList<Project> getProjects(){
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
                        OWN, //ToDO: Korrekte Daten abrufen
                        rs.getInt("squaremeter"),
                        "C:\\Users\\Jonas\\Nextcloud\\Jonas\\07_Programmieren\\Java\\Kennwertdatenbank\\src\\main\\resources\\kv1.csv"); //ToDO: Korrekte Daten abrufen
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    public ArrayList<Project> getProjects(int minCost, int maxCost, PropertyType type, Status status){

        var list = getProjects();


        for (Project project : list){
            int totalCost = (int) (project.getData().getBKP(1) + project.getData().getBKP(2) + project.getData().getBKP(3) + project.getData().getBKP(4) + project.getData().getBKP(5));
            if (!(totalCost < maxCost && totalCost > minCost) || project.getType() != type){
                list.remove(project);
            }

        }


        return list;
    }

    /**
     * This method inserts a new row into the products table.
     * Tt needs a valid project from Type Project.
     * code from https://neon.com/postgresql/postgresql-jdbc/insert
     */
    public String addProject(int projectNr, String address, int plz, String location, String owner, String type, int squareMeter, String dataPath) {

        //ToDo: übergebene Daten prüfen
        //Initialize an INSERT statement.
        //The question mark (?) is a placeholder that will be replaced by the actual values later.
        var sql = "INSERT INTO projects(projectnr, address, plz, location, owner, type, squaremeter, data) "
                + "VALUES(?,?,?,?,?,?::property_type,?,?)";


        PropertyType thisType = null;
        if (type.equals("Miete")){
            thisType = RENT;
        } else if (type.equals("Verkauf")){
            thisType = OWN;
        }
        
        try (var conn =  DB.connect();
             var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // bind the values
            pstmt.setInt(1, projectNr);
            pstmt.setString(2, address);
            pstmt.setInt(3, plz);
            pstmt.setString(4, location);
            pstmt.setString(5, owner);
            pstmt.setString(6, thisType.name());
            pstmt.setInt(7, squareMeter);

            //turns the TreeMap data into a Jsonb format (this part was written by claude.ai)
            ObjectMapper objectMapper = new ObjectMapper();
            String jacksonData = objectMapper.writeValueAsString(new ProjectData(dataPath).getData());
            pstmt.setObject(8, jacksonData, Types.OTHER);

            // execute the INSERT statement and get the inserted id
            int insertedRow = pstmt.executeUpdate();
            if (insertedRow > 0) {
                var rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " wurde hinzugefügt";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException f) {
            throw new RuntimeException(f);
        }
        return "Projekt konnte nicht hinzugefügt werden";
    }



    public int getMax(){
        int max = 0;
        for (Project project : projects){
            int c = project.getData().getTotalCost();
            if (c > max){
                max = c;
            }
        }
        return max;
    }

    public int getMin(){
        int min = Integer.MAX_VALUE;
        for (Project project : projects){
            int c = project.getData().getTotalCost();
            if (c < min){
                min = c;
            }
        }
        return min;
    }
}
