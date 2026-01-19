package kennwertdatenbank;

import java.util.TreeMap;

public class Project {
    private final int projectNr;
    private final String dadress;
    private final int plz;
    private final String location;
    private final String owner;
    private final PropertyType type;
    private final int squareMeter;
    private final ProjectData data;

    public Project(int projectNr, String address, int plz, String location, String owner, PropertyType type, int squareMeter, String dataPath){
        this.projectNr = projectNr;
        this.dadress = address;
        this.plz = plz;
        this.location = location;
        this.owner = owner;
        this.type = type;
        this.squareMeter = squareMeter;
        this.data = new ProjectData(dataPath);
    }

    public int getProjectNr(){
        return projectNr;
    }

    public String getAddress(){
        return dadress;
    }

    public int getPlz() {
        return plz;
    }

    public String getLocation(){
        return location;
    }

    public String getOwner(){
        return owner;
    }

    public PropertyType getType() {
        return type;
    }

    public int getSquareMeter(){
        return squareMeter;
    }

    public ProjectData getData(){
        return data;
    }
}