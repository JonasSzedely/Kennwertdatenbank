package kennwertdatenbank;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.Locale;
import java.util.TreeMap;

public class Project {
    private int projectNr;
    private int version;
    private String address;
    private int plz;
    private String location;
    private String owner;
    private String propertyType;
    private String constructionType;
    private int documentPhase;
    private int calculationPhase;
    private int apartmentsNr;
    private int bathroomNr;
    private int hnf;
    private int gf;
    private int volumeUnderground;
    private int volumeAboveGround;
    private int facadeArea;
    private int windowArea;
    private String facadeType;
    private String windowType;
    private String roofType;
    private String heatingType;
    private String coolingType;
    private String ventilationTypeApartments;
    private String ventilationTypeUg;
    private String coNo;
    private String special;
    private ProjectData data;
    private TreeMap<Integer, Calculation> calculations;
    private Locale swissLocale = Locale.of("de", "CH");
    private BooleanProperty pinned = new SimpleBooleanProperty(false); //code from claude.ai

    public Project(int projectNr, int version, String address, int plz, String location, String owner, String propertyType,
                   String constructionType, int document_phase, int calculationPhase, int apartmentsNr, int bathroomNr,
                   int hnf, int gf, int volumeUnderground, int volumeAboveGround, int facadeArea, int windowArea,
                   String facadeType, String windowType, String roofType, String heatingType, String coolingType,
                   String ventilationTypeApartments, String ventilationTypeUg, String coNo, String special,
                   ProjectData projectData)
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
        this.calculations = calculations();

    }

    public TreeMap<Integer, Calculation> calculations(){
        TreeMap<Integer, Calculation> map = new TreeMap<>();

        String[] titel = {
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
                "BKP 1-5/m3",
                "BKP 1-5/HNF",
                "BKP 1-5/WHG",
                "",
                "BKP 2/m3",
                "BKP 2/HNF",
                "BKP 2/WHG",
                "",
                "BKP 211/m3",
                "",
                "BKP 230/m3",
                "BKP 230/HNF",
                "BKP 242/HNF",
                "BKP 244/HNF",
                "BKP 250-257/HNF",
                "",
                "Ausbau 1/HNF",
                "Ausbau 2/HNF",
                "Ausbau 1+2/HNF",
                "",
                "HNF/WHG",
                "Verhältnis UG/OG",
                "Fenster Anteil"
        };

        //ToDo Kalkluation muss erkennen wenn eine BKP nicht vorhanden ist.

        String[] numbers = {
                String.format(swissLocale, "%,d", getData().getTotalCost()) + " Fr.",
                String.format(swissLocale, "%,d", data.getBKP(2)) + " Fr.",
                String.format(swissLocale, "%,d", data.getBKP(211) + data.getBKP(212)) + " Fr.",
                String.format(swissLocale, "%,d",data.getBKP(23) - data.getBKP(2331) - data.getBKP(2332)) + " Fr.",
                String.format(swissLocale, "%,d",data.getBKP(241) + data.getBKP(242)) + " Fr.",
                String.format(swissLocale, "%,d",data.getBKP(244)) + " Fr.",
                String.format(swissLocale, "%,d",data.getBKP(25) - data.getBKP(258) - data.getBKP(259)) + " Fr.",
                String.format(swissLocale, "%,d",data.getBKP(27)) + " Fr.",
                String.format(swissLocale, "%,d",data.getBKP(28)-data.getBKP(289)) + " Fr.",
                "",
                String.format(swissLocale, "%,d", (getData().getTotalCost() / (volumeUnderground + volumeAboveGround))),
                String.format(swissLocale, "%,d", (getData().getTotalCost() / hnf)),
                String.format(swissLocale, "%,d", (getData().getTotalCost() / apartmentsNr)),
                "",
                String.format(swissLocale, "%,d", (data.getBKP(2) / (volumeUnderground + volumeAboveGround))),
                String.format(swissLocale, "%,d", (data.getBKP(2) / hnf)),
                String.format(swissLocale, "%,d", (data.getBKP(2) / apartmentsNr)),
                "",
                String.format(swissLocale, "%,d", (data.getBKP(211) / (volumeUnderground + volumeAboveGround))),
                "",
                String.format(swissLocale, "%,d", (data.getBKP(230) / (volumeUnderground + volumeAboveGround))),
                String.format(swissLocale, "%,d", (data.getBKP(230) / hnf)),
                String.format(swissLocale, "%,d", (data.getBKP(242) / hnf)),
                String.format(swissLocale, "%,d", (data.getBKP(244) / hnf)),
                String.format(swissLocale, "%,d", (data.getRange(250,25799) / hnf)),
                "",
                String.format(swissLocale, "%,d", (data.getBKP(27) / hnf)),
                String.format(swissLocale, "%,d", (data.getBKP(28) / hnf)),
                String.format(swissLocale, "%,d", ((data.getBKP(27) + data.getBKP(28)) / hnf)),
                "",
                String.format(swissLocale, "%,d",hnf / apartmentsNr) + " m²",
                String.format("%.2f", (double) volumeUnderground / volumeAboveGround),
                String.valueOf((int) (((double) windowArea / (double) facadeArea)*100)) + " %"
        };

        if (titel.length == numbers.length){
            for (int i = 0; i < titel.length; i++){
                map.put(i, new Calculation(titel[i], numbers[i]));
            }
        } else {
            System.err.println("Error in calculations. Arrays do not have the same length!");
        }
        return map;
    }

    //code from claude.ai
    public BooleanProperty pinnedProperty() {
        return pinned;
    }

    //code from claude.ai
    public boolean isPinned() {
        return pinned.get();
    }

    //code from claude.ai
    public void setPinned(boolean pinned) {
        this.pinned.set(pinned);
    }

    public int getProjectNr(){
        return projectNr;
    }

    public int getVersion() {
        return version;
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

    public int getVolume(){
        return volumeAboveGround + volumeUnderground;
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

    public Object[] getAttributes(){
        return new Object[]{
                projectNr,
                version,
                address,
                plz,
                location,
                owner,
                propertyType,
                constructionType,
                documentPhase,
                calculationPhase,
                apartmentsNr,
                bathroomNr,
                hnf,
                gf,
                volumeUnderground,
                volumeAboveGround,
                facadeArea,
                windowArea,
                facadeType,
                windowType,
                roofType,
                heatingType,
                coolingType,
                ventilationTypeApartments,
                ventilationTypeUg,
                coNo,
                special,
                data
        };
    }

    public void setVersion(int version){
        this.version = version;
    }

    public void setAddress(String address) { this.address = address; }

    public void setPlz(int plz) { this.plz = plz; }

    public void setLocation(String location) { this.location = location; }

    public void setOwner(String owner) { this.owner = owner; }

    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public void setConstructionType(String constructionType) { this.constructionType = constructionType; }

    public void setDocumentPhase(int documentPhase) { this.documentPhase = documentPhase; }

    public void setCalculationPhase(int calculationPhase) { this.calculationPhase = calculationPhase; }

    public void setApartmentsNr(int apartmentsNr) { this.apartmentsNr = apartmentsNr; }

    public void setBathroomNr(int bathroomNr) { this.bathroomNr = bathroomNr; }

    public void setHnf(int hnf) { this.hnf = hnf; }

    public void setGf(int gf) { this.gf = gf; }

    public void setVolumeUnderground(int volumeUnderground) { this.volumeUnderground = volumeUnderground; }

    public void setVolumeAboveGround(int volumeAboveGround) { this.volumeAboveGround = volumeAboveGround; }

    public void setFacadeArea(int facadeArea) { this.facadeArea = facadeArea; }

    public void setWindowArea(int windowArea) { this.windowArea = windowArea; }

    public void setFacadeType(String facadeType) { this.facadeType = facadeType; }

    public void setWindowType(String windowType) { this.windowType = windowType; }

    public void setRoofType(String roofType) { this.roofType = roofType; }

    public void setHeatingType(String heatingType) { this.heatingType = heatingType; }

    public void setCoolingType(String coolingType) { this.coolingType = coolingType; }

    public void setVentilationTypeApartments(String ventilationTypeApartments) { this.ventilationTypeApartments = ventilationTypeApartments; }

    public void setVentilationTypeUg(String ventilationTypeUg) { this.ventilationTypeUg = ventilationTypeUg; }

    public void setCoNo(String coNo) { this.coNo = coNo; }

    public void setSpecial(String special) { this.special = special; }

    public void setData(ProjectData data) { this.data = data; }
}