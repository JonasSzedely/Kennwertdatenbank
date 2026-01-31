package kennwertdatenbank;

import javafx.scene.control.Label;

import java.util.Locale;
import java.util.TreeMap;

public class Project {
    private final int projectNr;
    private final int version;
    private final String address;
    private final int plz;
    private final String location;
    private final String owner;
    private final String propertyType;
    private final String constructionType;
    private final int documentPhase;
    private final int calculationPhase;
    private final int apartmentsNr;
    private final int bathroomNr;
    private final int hnf;
    private final int gf;
    private final int volumeUnderground;
    private final int volumeAboveGround;
    private final int facadeArea;
    private final int windowArea;
    private final String facadeType;
    private final String windowType;
    private final String roofType;
    private final String heatingType;
    private final String coolingType;
    private final String ventilationTypeApartments;
    private final String ventilationTypeUg;
    private final String coNo;
    private final String special;
    private final ProjectData data;
    private final TreeMap<Integer, Calculation> calculations;
    private final Locale swissLocale;

    public Project(int projectNr, int version, String address, int plz, String location, String owner, String propertyType,
                   String constructionType, int document_phase, int calculationPhase, int apartmentsNr, int bathroomNr,
                   int hnf, int gf, int volumeUnderground, int volumeAboveGround, int facadeArea, int windowArea,
                   String facadeType, String windowType, String roofType, String heatingType, String coolingType,
                   String ventilationTypeApartments, String ventilationTypeUg, String coNo, String special,
                   ProjectData projectData, Locale swissLocale)
    {
        this.projectNr = projectNr;
        this.version = version;
        this.address = address;
        this.plz = plz;
        this.location = location;
        this.owner = owner;
        this.propertyType = propertyType;
        this.constructionType = constructionType;
        this.documentPhase = document_phase;
        this.calculationPhase = calculationPhase;
        this.apartmentsNr = apartmentsNr;
        this.bathroomNr = bathroomNr;
        this.hnf = hnf;
        this.gf = gf;
        this.volumeUnderground = volumeUnderground;
        this.volumeAboveGround = volumeAboveGround;
        this.facadeArea = facadeArea;
        this.windowArea = windowArea;
        this.facadeType = facadeType;
        this.windowType = windowType;
        this.roofType = roofType;
        this.heatingType = heatingType;
        this.coolingType = coolingType;
        this.ventilationTypeApartments = ventilationTypeApartments;
        this.ventilationTypeUg = ventilationTypeUg;
        this.coNo = coNo;
        this.special = special;
        this.data = projectData;
        this.swissLocale = swissLocale;
        this.calculations = calculations();
    }

    public TreeMap<Integer, Calculation> calculations(){
        TreeMap<Integer, Calculation> map = new TreeMap<>();

        String[] strings = {
                "Bausumme",
                "BKP 2",
                "BKP 211 + 212",
                "BKP 23 (o. PV/E-Mob.)",
                "BKP 241+242",
                "BKP 244",
                "BKP 250-257",
                "Ausbau 1",
                "Ausbau 2 (o. Res.)",
                "",
                "HNF/WHG",
                "Verhältnis UG/OG"
        };

        //ToDo Kalkluation muss erkennen wenn eine BKP nicht vorhanden ist.

        String[] numbers = {
                String.format(swissLocale, "%,d", getData().getTotalCost()),
                String.format(swissLocale, "%,d", data.getBKP(2)),
                String.format(swissLocale, "%,d", data.getBKP(211) + data.getBKP(212)),
                String.valueOf(data.getBKP(23) - data.getBKP(2331) - data.getBKP(2332)),
                String.valueOf(data.getBKP(241) + data.getBKP(242)),
                String.valueOf(data.getBKP(244)),
                String.valueOf(data.getBKP(25) - data.getBKP(258) - data.getBKP(259)),
                String.valueOf(data.getBKP(27)),
                String.valueOf(data.getBKP(28)-data.getBKP(289)),
                "",
                String.valueOf(hnf / apartmentsNr),
                String.format("%.2f", (double) volumeUnderground / (double) volumeAboveGround)
        };

        if (strings.length == numbers.length){
            for (int i = 0; i < strings.length; i++){
                map.put(i, new Calculation(strings[i], numbers[i]));
            }
        } else {
            System.err.println("Error in calculations. Arrays do not have the same length!");
        }
        return map;
    }



    public int getProjectNr(){
        return projectNr;
    }

    public String getAddress(){
        return address;
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

    public String getPropertyType() {
        return propertyType;
    }

    public int getBathroomNr(){
        return bathroomNr;
    }

    public int getApartmentsNr(){
        return apartmentsNr;
    }

    public ProjectData getData(){
        return data;
    }

    public TreeMap<Integer, Calculation> getCalculations() {
        return calculations;
    }

    public int getVersion() {
        return version;
    }

    public int getDocumentPhase() {
        return documentPhase;
    }

    public int getCalculationPhase() {
        return calculationPhase;
    }

    public int getHnf() {
        return hnf;
    }

    public int getGf() {
        return gf;
    }

    public int getVolumeUnderground() {
        return volumeUnderground;
    }

    public int getVolumeAboveGround() {
        return volumeAboveGround;
    }

    public int getFacadeArea() {
        return facadeArea;
    }

    public int getWindowArea() {
        return windowArea;
    }

    public String getFacadeType() {
        return facadeType;
    }

    public String getWindowType() {
        return windowType;
    }

    public String getRoofType() {
        return roofType;
    }

    public String getConstructionType() {
        return constructionType;
    }

    public String getHeatingType() {
        return heatingType;
    }

    public String getCoolingType() {
        return coolingType;
    }

    public String getVentilationTypeApartments() {
        return ventilationTypeApartments;
    }

    public String getVentilationTypeUg() {
        return ventilationTypeUg;
    }

    public String getCoNo() {
        return coNo;
    }

    public String getSpecial() {
        return special;
    }
}