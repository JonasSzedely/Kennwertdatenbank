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
    private StringBuilder STRING_BUILDER = new StringBuilder();
    private final Locale swissLocale = new Locale("de", "CH");
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
    private double avarageRatioUG;


    public Controller(){
        /*
        var type = "CREATE TYPE property_type AS ENUM ('RENT', 'OWN')";
        try (var conn =  DB.connect();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(type);
            System.out.println("Enum property_type erfolgreich erstellt!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

         */
        //var status = "CREATE TYPE status AS ENUM ('BAUKOSTENPLANUNG', 'BAUPROJEKT', 'NACHKALKULATION')";

        //SQL-Tabelle erstellen, fals nicht vorhanden
        STRING_BUILDER.append("CREATE TABLE IF NOT EXISTS projects(");

        //SQL-Tabelle anhand dem Array SQL_PROJECT_DATA erstellen
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
        //CHECK hinzufügen
        STRING_BUILDER.append("CHECK (project_nr > 9999), PRIMARY KEY(project_nr, version))");

        var sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (var conn =  DB.connect();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Tabelle erfolgreich erstellt oder schon vorhanden.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        //for testing only, do not load more than 1000 projects at once, or it may take long.
        //addRandomProjects(100);

        PROJECTS = getProjects();
    }

    /**
     * This method inserts a new row into the products table.
     * Tt needs a valid project from Type Project.
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

        //ToDo: übergebene Daten prüfen
        //Initialize an INSERT statement.
        //The question mark (?) is a placeholder that will be replaced by the actual values later.
        STRING_BUILDER.append("INSERT INTO projects(");
        for (int i = 0; i < SQL_PROJECT_DATA.length; i++) {
            var parts = SQL_PROJECT_DATA[i].split(",", 2);
            STRING_BUILDER.append(parts[0]);

            if (i < SQL_PROJECT_DATA.length - 1) {
                STRING_BUILDER.append(",");
            }
        }
        STRING_BUILDER.append(") VALUES(");

        for(int i = 0; i < SQL_PROJECT_DATA.length-1; i++){
            STRING_BUILDER.append("?,");
        }
        STRING_BUILDER.append("?)");

        var sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        //Versionsabfrage von der DB
        var version = 1;
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


        try (var conn =  DB.connect();
             var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // bind the values
            var data = new ProjectData(dataPath);
            pstmt.setInt(1, project_nr);
            pstmt.setInt(2, version);
            pstmt.setString(3, address);
            pstmt.setInt(4, plz);
            pstmt.setString(5, location);
            pstmt.setString(6, owner);
            pstmt.setString(7, property_type);
            pstmt.setString(8, construction_type);
            pstmt.setInt(9, document_phase);
            pstmt.setInt(10, calculation_phase);
            pstmt.setInt(11, apartments_nr);
            pstmt.setInt(12, bathroom_nr);
            pstmt.setInt(13, hnf);
            pstmt.setInt(14, gf);
            pstmt.setInt(15, volume_underground);
            pstmt.setInt(16, volume_above_ground);
            pstmt.setInt(17, facadearea);
            pstmt.setInt(18, windowarea);
            pstmt.setString(19, facade_type);
            pstmt.setString(20, window_type);
            pstmt.setString(21, roof_type);
            pstmt.setString(22, heating_type);
            pstmt.setString(23, cooling_type);
            pstmt.setString(24, ventilation_type_apartments);
            pstmt.setString(25, ventilation_type_ug);
            pstmt.setString(26, co_no);
            pstmt.setString(27, special);

            //turns the TreeMap data into a Jsonb format (this part was written by claude.ai)
            String jacksonData = OBJECT_MAPPER.writeValueAsString(data.getData());
            pstmt.setObject(28, jacksonData, Types.OTHER);

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
        } catch (JsonProcessingException f) {
            throw new RuntimeException(f);
        }
        return "Projekt konnte nicht hinzugefügt werden";
    }

    public String addCalculation(String name, int position, int number1, String operator, int number2) {
        int lastId = 1;

        try (var conn =  DB.connect();
             var stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT MAX(id) FROM calculations");
            if (rs != null)
                lastId = rs.getInt("id");
            System.out.println("Projects Tabelle erfolgreich erstellt!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }


        var sql = "INSERT INTO calculations(id, position, name, number1, operator, number2)"
                + "VALUES(?,?,?,?)";

        try (var conn =  DB.connect();
             var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // bind the values
            pstmt.setInt(1, lastId+1);
            pstmt.setInt(2, position);
            pstmt.setString(3, name);
            pstmt.setInt(4, number1);
            pstmt.setString(5, operator);
            pstmt.setInt(6, number2);

            // execute the INSERT statement and get the inserted id
            int insertedRow = pstmt.executeUpdate();
            if (insertedRow > 0) {
                var rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Neue Kalkulation " + name + " wurde hinzugefügt";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Projekt konnte nicht hinzugefügt werden";
    }


    /**
     * This method gets all projects from the PostgreSQL Database.
     * It returns them as a ArrayList of Projects.
     * code from https://neon.com/postgresql/postgresql-jdbc/insert
     */
    public TreeMap<Integer, Project> getProjects(){
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

                // Wiederverwendeter ObjectMapper!
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
        avarageRatioUG = 0;
        double totalRatioUG = 0;
        for (Project project : PROJECTS.values()){
            int cost = project.getData().getTotalCost();
            int apartments = project.getApartmentsNr();
            totalRatioUG += ((double) project.getVolumeUnderground() / (double) project.getVolumeAboveGround());
            minTotalCost = Math.min(cost, minTotalCost);
            maxTotalCost = Math.max(cost, maxTotalCost);
            minApartments = Math.min(apartments,minApartments);
            maxApartments = Math.max(apartments, maxApartments);
        }
        avarageRatioUG = totalRatioUG / PROJECTS.size();
    }

    public int getMinTotalCost(){
        return minTotalCost;
    }

    public int getMaxTotalCost(){
        return maxTotalCost;
    }

    public int getMinApartments(){
        return minApartments;
    }

    public int getMaxApartments(){
        return maxApartments;
    }

    public double getAvarageRatioUG(){
        return avarageRatioUG;
    }

    private void addRandomProjects(int howMany) {
        var rand = new Random(123);
        int j = 0;
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

            if (j == 9){
                j = 0;
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
