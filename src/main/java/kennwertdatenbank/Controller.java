package kennwertdatenbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.DB;
import java.sql.*;
import java.util.*;

public class Controller {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private TreeMap<Integer, Project> PROJECTS;
    private boolean dbAvailable = false;
    private StringBuilder STRING_BUILDER = new StringBuilder();
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
    private int minVolume;
    private int maxVolume;


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
        for (String str : SQL_PROJECT_DATA) {
            String[] parts = str.split(",", 2);
            STRING_BUILDER.append(parts[0]);

            switch (parts[1]) {
                case "int":
                    STRING_BUILDER.append(" INT NOT NULL,");
                    break;
                case "string":
                    STRING_BUILDER.append(" VARCHAR(255) NOT NULL,");
                    break;
                case "json":
                    STRING_BUILDER.append(" JSONB NOT NULL,");
                    break;
            }
        }
        STRING_BUILDER.append("active BOOLEAN NOT NULL DEFAULT true, CHECK (project_nr > 9999), PRIMARY KEY(project_nr, version))");

        String sql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        try (Connection conn = DB.connect(); Statement stmt = conn.createStatement()) {
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
     * Add a new Project to the DB.
     * @param project an object of type Project is needed
     */
    public String addProject(Project project){
        if (isDatabaseAvailable()) {
            return AddProject.add(project, SQL_PROJECT_DATA);
        } else {
            return "Keine Datenbankverbindung verfügbar. Projekte können nicht hinzugefügt werden.";
        }
    }

    /**
     * Modify an existing Project in the DB.
     * @param project an object of type Project is needed
     */
    public String modifyProjects(Project project){
        if (isDatabaseAvailable()) {
            return ModifyProject.modify(project);
        } else {
            return "Keine Datenbankverbindung verfügbar. Projekte kann nicht geändert werden.";
        }
    }

    /**
     * This method does delete a project in the DB. This cannot be undone.
     * @param projectNr the project number of the project to be deleted.
     * @param version the version number of the project to be deleted.
     * @return if the project was deleted.
     */
    public String deleteProject(int projectNr, int version){
        if (!isDatabaseAvailable()) {
            return "Keine Datenbankverbindung verfügbar. Projekte kann nicht gelöscht werden.";
        }
        String sql = ("UPDATE projects SET active = false WHERE project_nr = ? AND version = ?");
        try (Connection conn =  DB.connect(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, projectNr);
            pstmt.setInt(2, version);
            int editedRow = pstmt.executeUpdate();
            if (editedRow > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return "Projekt Nr. " + rs.getInt(1) + " version Nr. " + rs.getInt(2) + " wurde entfernt";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Projekt konnte nicht entfernt werden";
        }
        return "Projekt konnte nicht entfernt werden";
    }

    /**
     * Returns the project list as TreeMap
     * Key = project number * 100 + version number.
     */
    public TreeMap<Integer, Project> getProjects() {
        if (!isDatabaseAvailable()) {
            return PROJECTS;
        }
        PROJECTS = new TreeMap<>();
        PROJECTS = GetProjects.get(SQL_PROJECT_DATA);
        return PROJECTS;
    }


    public void calculate(){
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
            int apartments = project.getApartmentsNr();
            int volume = project.getVolume();
            averageRatioUG += (double) ((project.getVolumeUnderground() * 100) / project.getVolumeAboveGround());
            minTotalCost = Math.min(cost, minTotalCost);
            maxTotalCost = Math.max(cost, maxTotalCost);
            minApartments = Math.min(apartments, minApartments);
            maxApartments = Math.max(apartments, maxApartments);
            averageWindowRatio += (int) (((double) project.getWindowArea() / (double) project.getFacadeArea()) * 100);
            minVolume = Math.min(volume, minVolume);
            maxVolume = Math.max(volume, maxVolume);
        }
        averageRatioUG /= PROJECTS.size();
        averageWindowRatio /= PROJECTS.size();

    }

    /**
     * @return the highest volume of all projects.
     */
    public int getMinVolume(){
        return minVolume;
    }

    /**
     * @return the lowest volume of all projects.
     */
    public int getMaxVolume(){
        return maxVolume;
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

    /**
     * @return the average window to fassade are ratio for all projects.
     */
    public int getAverageWindowRatio(){
        return averageWindowRatio;
    }

    /**
     * Used to create random projects, it will use project cost from kv1-10 in resources
     * @param howMany set how many projects should be created.
     */
    private void addRandomProjects(int howMany) {
        Random rand = new Random(123);
        int j = 1;
        for (int i = 0; i<howMany; i++){
            int num = 10001 + i;

            String pt = "Miete";
            if(i%2==0){
                pt = "Stockwerkeigentum";
            }

            String fassade = "AWD";
            if(i%2==0){
                fassade = "Hinterlüftet";
            }

            String window = "Kunststoff";
            if(i%2==0){
                window = "Holz";
            }

            String dach = "Flachdach";
            if(i%2==0){
                dach = "Steildach";
            }

            String heizung = "Erdsonde";
            if(i%2==0){
                heizung = "Pellet";
            }

            String kühlung = "FreeCooling";
            if(i%2==0){
                kühlung = "keine";
            }

            String lüftung = "KWL";
            if(i%2==0){
                lüftung = "keine";
            }

            String lüftungUG = "mechanisch";
            if(i%2==0){
                lüftungUG = "natürlich";
            }

            String cono = "Ja";
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

            System.out.println(addProject(new Project(num,1,"Projektstrasse " + num, 8001 + i, "Zürich", "Besitzer"+i,
                    pt, "Neubau",31, 41, w, (int)(w*1.8), s, (int)(s*1.2), (int)((s*2.8)*0.4),
                    (int)((s*2.8)*0.6), (int) (s*0.8), (int)(s*0.3),fassade, window, dach,
                    heizung, kühlung, lüftung,lüftungUG, cono,
                    "nichts spezielles",new ProjectData("C:\\Users\\Jonas\\Nextcloud\\Jonas\\07_Programmieren\\Java\\Kennwertdatenbank\\src\\main\\resources\\kv" + j + ".csv"))));
        }
    }
}
