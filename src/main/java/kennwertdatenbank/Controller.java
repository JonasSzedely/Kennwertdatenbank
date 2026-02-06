package kennwertdatenbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.DB;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

public class Controller {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private TreeMap<Integer, Project> PROJECTS;
    private boolean dbAvailable = false;
    private StringBuilder STRING_BUILDER = new StringBuilder();
    private final Locale swissLocale = new Locale("de", "CH");
    Object[] values;
    private final String[] SQL_PROJECT_DATA = {
            "project_nr,int",
            "version,int",
            "address,string",
            "plz,int",
            "location,string",
            "owner,string",
            "property_type,string",
            "construction_type,string",
            "document_phase,int",
            "calculation_phase,int",
            "apartments_nr,int",
            "bathroom_nr,int",
            "hnf,int",
            "gf,int",
            "volume_underground,int",
            "volume_above_ground,int",
            "facadearea,int",
            "windowarea,int",
            "facade_type,string",
            "window_type,string",
            "roof_type,string",
            "heating_type,string",
            "cooling_type,string",
            "ventilation_type_apartments,string",
            "ventilation_type_ug,string",
            "co_no,string",
            "special,string",
            "data,json"
    };
    private int minTotalCost;
    private int maxTotalCost;
    private int minApartments;
    private int maxApartments;
    private double averageRatioUG;
    private int averageWindowRatio;


    public Controller(){
        PROJECTS = new TreeMap<>();

        if (!initializeDatabase()) {
            System.err.println("Controller läuft im Offline-Modus");
            return;
        }

        dbAvailable = true;
        PROJECTS = getProjects();

        //for testing only, do not load more than 500 projects at once, or it will take very long.
        //addRandomProjects(500);
    }

    public boolean initializeDatabase() {

        STRING_BUILDER.append("CREATE TABLE IF NOT EXISTS projects(");

        //construct SQL-table based on array SQL_PROJECT_DATA
        for (int i = 0; i < SQL_PROJECT_DATA.length; i++) {
            var parts = SQL_PROJECT_DATA[i].split(",", 2);
            STRING_BUILDER.append(parts[0]);

            switch (parts[1]) {
                case "int" :
                    STRING_BUILDER.append(" INT NOT NULL,");
                    break;
                case "string" :
                    STRING_BUILDER.append(" VARCHAR(255) NOT NULL,");
                    break;
                case "json" :
                    STRING_BUILDER.append(" JSONB NOT NULL,");
                    break;
            }
        }
        STRING_BUILDER.append("CHECK (project_nr > 9999), PRIMARY KEY(project_nr, version))");

        var sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (var conn = DB.connect(); var stmt = conn.createStatement()) {
            if (conn == null) {
                return false;
            }
            stmt.executeUpdate(sql);
            dbAvailable = true;
            PROJECTS = getProjects();
            System.out.println("Tabelle erfolgreich erstellt oder schon vorhanden.");
            return true;
        } catch (SQLException e) {
            System.err.println("DB-Initialisierung fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }

    /**
     * check if DB-connection is available
     * @return boolean
     */
    public boolean isDatabaseAvailable() {
        return dbAvailable;
    }

    public boolean testDBConnection() {
        return DB.isConnectionAvailable();
    }

    /**
     * This method inserts a new row into the products table.
     * code from https://neon.com/postgresql/postgresql-jdbc/insert
     */
    public String addProject(
            int project_nr,
            String address,
            int plz,
            String location,
            String owner,
            String property_type,
            String construction_type,
            int document_phase,
            int calculation_phase,
            int apartments_nr,
            int bathroom_nr,
            int hnf,
            int gf,
            int volume_underground,
            int volume_above_ground,
            int facadearea,
            int windowarea,
            String facade_type,
            String window_type,
            String roof_type,
            String heating_type,
            String cooling_type,
            String ventilation_type_apartments,
            String ventilation_type_ug,
            String co_no,
            String special,
            String dataPath) {

        if (!isDatabaseAvailable()) {
            return "Keine Datenbankverbindung verfügbar. Projekte können nicht hinzugefügt werden.";
        }
        //Versionsabfrage von der DB
        int version = 1;
        try (var conn = DB.connect();
             var pstmt = conn.prepareStatement("SELECT MAX(version) FROM projects WHERE project_nr = ?")) {

            pstmt.setInt(1, project_nr);
            var rs = pstmt.executeQuery();

            //Wenn bereits min. eine Projekt-Version existiert, wird die Versionierung um eins erhöht
            if (rs.next()) {
                int maxVersion = rs.getInt(1);
                if (!rs.wasNull()) {
                    version = maxVersion + 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        var data = new ProjectData(dataPath);
        String jacksonData = null;
        try {
            jacksonData = OBJECT_MAPPER.writeValueAsString(data.getData());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return e.toString();
        }

        //input elemente in Array lesen für dynamisches einlesen in DB
        values = new Object[] {
                project_nr,
                version,
                address,
                plz,
                location,
                owner,
                property_type,
                construction_type,
                document_phase,
                calculation_phase,
                apartments_nr,
                bathroom_nr,
                hnf,
                gf,
                volume_underground,
                volume_above_ground,
                facadearea,
                windowarea,
                facade_type,
                window_type,
                roof_type,
                heating_type,
                cooling_type,
                ventilation_type_apartments,
                ventilation_type_ug,
                co_no,
                special,
                jacksonData
        };

        //Initialize an INSERT statement.
        //The question mark (?) is a placeholder that will be replaced by the actual values later.
        STRING_BUILDER.append("INSERT INTO projects(");
        for (int i = 0; i < SQL_PROJECT_DATA.length-1; i++){
            STRING_BUILDER.append(SQL_PROJECT_DATA[i].split(",")[0]).append(",");
        }
        STRING_BUILDER.append(SQL_PROJECT_DATA[SQL_PROJECT_DATA.length-1].split(",")[0]);

        STRING_BUILDER.append(") VALUES(");

        for (int i = 0; i < SQL_PROJECT_DATA.length - 1; i++) {
            STRING_BUILDER.append("?,");
        }
        STRING_BUILDER.append("?)");

        var sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (var conn =  DB.connect();
             var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < SQL_PROJECT_DATA.length; i++) {
                var parts = SQL_PROJECT_DATA[i].split(",", 2);
                String type = parts[1];

                switch (type) {
                    case "int":
                        pstmt.setInt(i + 1, (int) values[i]);
                        break;
                    case "string":
                        pstmt.setString(i + 1, (String) values[i]);
                        break;
                    case "json":
                        pstmt.setObject(i + 1, (String) values[i], Types.OTHER);
                        break;
                }
            }

            // execute the INSERT statement and get the inserted id
            int insertedRow = pstmt.executeUpdate();
            if (insertedRow > 0) {
                var rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " wurde hinzugefügt.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        return "Projekt konnte nicht hinzugefügt werden";
    }

    /**
     * This method does modify a project in the DB.
     * code from https://neon.com/postgresql/postgresql-jdbc/update
     */
    public String modifyProject(
            int project_nr,
            int version,
            String address,
            int plz,
            String location,
            String owner,
            String property_type,
            String construction_type,
            int document_phase,
            int calculation_phase,
            int apartments_nr,
            int bathroom_nr,
            int hnf,
            int gf,
            int volume_underground,
            int volume_above_ground,
            int facadearea,
            int windowarea,
            String facade_type,
            String window_type,
            String roof_type,
            String heating_type,
            String cooling_type,
            String ventilation_type_apartments,
            String ventilation_type_ug,
            String co_no,
            String special){
        if (!isDatabaseAvailable()) {
            return "Keine Datenbankverbindung verfügbar. Projekte kann nicht geändert werden.";
        }
        //Initialize an INSERT statement.
        //The question mark (?) is a placeholder that will be replaced by the actual values later.
        var sql = (
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


        try (var conn =  DB.connect(); var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // bind the values
            pstmt.setString(1, address);
            pstmt.setInt(2, plz);
            pstmt.setString(3, location);
            pstmt.setString(4, owner);
            pstmt.setString(5, property_type);
            pstmt.setString(6, construction_type);
            pstmt.setInt(7, document_phase);
            pstmt.setInt(8, calculation_phase);
            pstmt.setInt(9, apartments_nr);
            pstmt.setInt(10, bathroom_nr);
            pstmt.setInt(11, hnf);
            pstmt.setInt(12, gf);
            pstmt.setInt(13, volume_underground);
            pstmt.setInt(14, volume_above_ground);
            pstmt.setInt(15, facadearea);
            pstmt.setInt(16, windowarea);
            pstmt.setString(17, facade_type);
            pstmt.setString(18, window_type);
            pstmt.setString(19, roof_type);
            pstmt.setString(20, heating_type);
            pstmt.setString(21, cooling_type);
            pstmt.setString(22, ventilation_type_apartments);
            pstmt.setString(23, ventilation_type_ug);
            pstmt.setString(24, co_no);
            pstmt.setString(25, special);

            pstmt.setInt(26,project_nr);
            pstmt.setInt(27,version);

            // execute the INSERT statement and get the inserted id
            int editedRow = pstmt.executeUpdate();
            if (editedRow > 0) {
                var rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " wurde angepasst.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Projekt konnte nicht hinzugefügt werden";
    }

    /**
     * This method does delete a project in the DB. This cannot be undone.
     * code from https://neon.com/postgresql/postgresql-jdbc/delete
     * @param projectNr the project number of the project to be deleted.
     * @param version the version number of the project to be deleted.
     * @return if the project was deleted.
     */
    public String deleteProject(int projectNr, int version){
        if (!isDatabaseAvailable()) {
            return "Keine Datenbankverbindung verfügbar. Projekte kann nicht gelöscht werden.";
        }
        var sql = ("DELETE FROM projects WHERE project_nr = ? AND version = ?");
        try (var conn =  DB.connect(); var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, projectNr);
            pstmt.setInt(2, version);
            int editedRow = pstmt.executeUpdate();
            if (editedRow > 0) {
                var rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " version Nr. " + rs.getInt(2) + " wurde entfernt";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Projekt konnte nicht entfernt werden";
    }

    /**
     * This method gets all projects from the PostgreSQL Database.
     * It returns them as a TreeMap of Projects.
     * The key is made from project number * 100 + version number.
     * code from https://neon.com/postgresql/postgresql-jdbc/insert
     */
    public TreeMap<Integer, Project> getProjects(){
        if (!isDatabaseAvailable()) {
            return PROJECTS;
        }
        PROJECTS = new TreeMap<Integer, Project>();

        STRING_BUILDER.append("SELECT ");
        for(int i = 0; i < SQL_PROJECT_DATA.length-1; i++){
            STRING_BUILDER.append(SQL_PROJECT_DATA[i].split(",")[0] + ", ");
        }
        STRING_BUILDER.append(SQL_PROJECT_DATA[SQL_PROJECT_DATA.length-1].split(",")[0] + " FROM projects ORDER BY project_nr");
        var sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (var conn =  DB.connect();
             var stmt = conn.createStatement()) {

            var rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String data = rs.getString("data");

                Map<String, Integer> jsonMap = OBJECT_MAPPER.readValue(data, new TypeReference<Map<String, Integer>>() {});

                TreeMap<Integer, Integer> map = new TreeMap<>(new BKPComparator());
                jsonMap.forEach((k, v) -> map.put(Integer.parseInt(k), v));

                ProjectData projectData = new ProjectData(map);

                var project = new Project(
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
                        projectData,
                        swissLocale
                );
                PROJECTS.put((project.getProjectNr()*100)+project.getVersion(), project);
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

    public void calculate(){
        minTotalCost = Integer.MAX_VALUE;
        maxTotalCost = 0;
        minApartments = Integer.MAX_VALUE;
        maxApartments = 0;
        averageRatioUG = 0;
        averageWindowRatio = 0;

        for (Project project : PROJECTS.values()){
            int cost = project.getData().getTotalCost();
            int apartments = project.getApartmentsNr();
            averageRatioUG  += ((double) project.getVolumeUnderground() / (double) project.getVolumeAboveGround());
            minTotalCost = Math.min(cost, minTotalCost);
            maxTotalCost = Math.max(cost, maxTotalCost);
            minApartments = Math.min(apartments,minApartments);
            maxApartments = Math.max(apartments, maxApartments);
            averageWindowRatio += (int) (((double) project.getWindowArea() / (double) project.getFacadeArea())*100);
        }
        averageRatioUG /= PROJECTS.size();
        averageWindowRatio /= PROJECTS.size();
    }

    /**
     * @return the lowest project total cost of all projects.
     */
    public int getMinTotalCost(){
        return minTotalCost;
    }

    /**
     * @return the highest project total cost of all projects.
     */
    public int getMaxTotalCost(){
        return maxTotalCost;
    }

    /**
     * @return the lowest number of Apartment of all projects.
     */
    public int getMinApartments(){
        return minApartments;
    }

    /**
     * @return the highest number of Apartment of all projects.
     */
    public int getMaxApartments(){
        return maxApartments;
    }

    /**
     * @return the average ration UG to OG as double.
     */
    public double getAverageRatioUG(){
        return averageRatioUG;
    }

    public int getAverageWindowRatio(){
        return averageWindowRatio;
    }

    /**
     * Used to create random projects, it will use project cost from kv1-10 in resources
     * @param howMany set how many projects should be created.
     */
    private void addRandomProjects(int howMany) {
        var rand = new Random(123);
        int j = 1;
        for (int i = 0; i<howMany; i++){
            int num = 10001 + i;

            var pt = "Miete";
            if(i%2==0){
                pt = "Stockwerkeigentum";
            }

            var fassade = "AWD";
            if(i%2==0){
                fassade = "Hinterlüftet";
            }

            var window = "Kunststoff";
            if(i%2==0){
                window = "Holz";
            }

            var dach = "Flachdach";
            if(i%2==0){
                dach = "Steildach";
            }

            var heizung = "Erdsonde";
            if(i%2==0){
                heizung = "Pellet";
            }

            var kühlung = "FreeCooling";
            if(i%2==0){
                kühlung = "keine";
            }

            var lüftung = "KWL";
            if(i%2==0){
                lüftung = "keine";
            }

            var lüftungUG = "mechanisch";
            if(i%2==0){
                lüftungUG = "natürlich";
            }

            var cono = "Ja";
            if(i%2==0){
                cono = "Nein";
            }

            int s = rand.nextInt(9999)+1001;
            int w = rand.nextInt(50)+1;

            if (j == 10){
                j = 1;
            } else {
                j++;
            }

            System.out.println(addProject(num,"Projektstrasse " + num, 8001 + i, "Zürich", "Besitzer"+i,
                    pt, "Neubau",31, 41, w, (int)(w*1.8), s, (int)(s*1.2), (int)((s*2.8)*0.4),
                    (int)((s*2.8)*0.6), (int) (s*0.8), (int)(s*0.3),fassade, window, dach,
                    heizung, kühlung, lüftung,lüftungUG, cono,
                    "nichts spezielles","C:\\Users\\Jonas\\Nextcloud\\Jonas\\07_Programmieren\\Java\\Kennwertdatenbank\\src\\main\\resources\\kv" + j + ".csv"));
        }
    }
}
