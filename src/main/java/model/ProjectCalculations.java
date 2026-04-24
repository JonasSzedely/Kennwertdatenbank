package model;

import java.util.List;
import java.util.Locale;

public class ProjectCalculations {
    private final Project project;
    private final ProjectData data;
    private final Locale swissLocale = Locale.of("de", "CH");
    private final int hnf;
    private final int apartmentsNr;
    private final int parcelSize;
    private final int landscapedArea;
    private final int volumeUnderground;
    private final int volumeAboveGround;
    private final int windowArea;
    private final int facadeArea;

    public ProjectCalculations(Project project) {
        this.project = project;
        this.data = project.getData();
        this.hnf = project.get(ProjectValues.HNF);
        this.apartmentsNr = project.get(ProjectValues.APARTMENTS_NR);
        this.parcelSize = project.get(ProjectValues.PARCEL_SIZE);
        this.landscapedArea = project.get(ProjectValues.LANDSCAPED_AREA);
        this.volumeUnderground = project.get(ProjectValues.VOLUME_UNDERGROUND);
        this.volumeAboveGround = project.get(ProjectValues.VOLUME_ABOVE_GROUND);
        this.windowArea = project.get(ProjectValues.WINDOW_AREA);
        this.facadeArea = project.get(ProjectValues.FACADE_AREA);
    }

    public List<Calculation> getCalculations() {
        return List.of(
                calc("Bausumme", chf(data.getTotalCost())),
                calc("Bausumme exkl. 29", chf(data.getTotalCost()-data.getBKP(29))),
                calc("BKP 1", chf(data.getBKP(1))),
                calc("BKP 2", chf(data.getBKP(2))),
                calc("BKP 211 + 212", chf(data.getBKP(211) + data.getBKP(212))),
                calc("BKP 23 (o. PV/E-Mob.)", chf(data.getBKP(23) - data.getBKP(2331) - data.getBKP(2332))),
                calc("BKP 241+242", chf(data.getBKP(241) + data.getBKP(242))),
                calc("BKP 244", chf(data.getBKP(244))),
                calc("BKP 250-257", chf(data.getBKP(25) - data.getBKP(258) - data.getBKP(259))),
                calc("Ausbau 1", chf(data.getBKP(27))),
                calc("Ausbau 2 (o. Res.)", chf(data.getBKP(28) - data.getBKP(289))),

                Calculation.SEPARATOR,

                calc("BKP 1-5/m3", perM2M3(data.getTotalCost(), getVolume())),
                calc("BKP 1-5/HNF", perM2M3(data.getTotalCost(), hnf)),
                calc("BKP 1-5/WHG", perUnit(data.getTotalCost(), apartmentsNr)),

                Calculation.SEPARATOR,

                calc("BKP 2/m3", perM2M3(data.getBKP(2), getVolume())),
                calc("BKP 2 exkl. 29/m3", perM2M3((data.getBKP(2)-data.getBKP(29)), getVolume())),
                calc("BKP 2/HNF", perM2M3(data.getBKP(2), hnf)),
                calc("BKP 2/WHG", perUnit(data.getBKP(2), apartmentsNr)),
                calc("BKP 211/m3", perM2M3(data.getBKP(211), getVolume())),

                Calculation.SEPARATOR,

                calc("BKP 230/m3", perM2M3(data.getBKP(230), getVolume())),
                calc("BKP 230/HNF", perM2M3(data.getBKP(230), hnf)),
                calc("BKP 242/HNF", perM2M3(data.getBKP(242), hnf)),
                calc("BKP 244/HNF", perM2M3(data.getBKP(244), hnf)),
                calc("BKP 250-257/HNF", perM2M3(data.getRange(250, 25799), hnf)),

                Calculation.SEPARATOR,

                calc("Ausbau 1/HNF", perM2M3(data.getBKP(27), hnf)),
                calc("Ausbau 2/HNF", perM2M3(data.getBKP(28), hnf)),
                calc("Ausbau 1+2/HNF", perM2M3(data.getBKP(27) + data.getBKP(28), hnf)),

                Calculation.SEPARATOR,

                calc("BKP4 / Grundstücksf.", perM2M3(data.getBKP(4), parcelSize)),
                calc("BKP4 / Umgebungsf.", perM2M3(data.getBKP(4), landscapedArea)),

                Calculation.SEPARATOR,

                calc("HNF/WHG", String.format(swissLocale, "%,d m²", hnf / apartmentsNr)),
                calc("Verhältnis UG/OG", String.format("%.2f", (double) volumeUnderground / volumeAboveGround)),
                calc("Fenster Anteil", (int) ((double) windowArea / facadeArea * 100) + " %"),
                calc("BKP1 % Anteil", (int) ((double) data.getBKP(1)/data.getTotalCost() * 100) + "%"),
                calc("BKP2 % Anteil", (int) ((double) data.getBKP(2)/data.getTotalCost() * 100) + "%"),
                calc("BKP3 % Anteil", (int) ((double) data.getBKP(3)/data.getTotalCost() * 100) + "%"),
                calc("BKP4 % Anteil", (int) ((double) data.getBKP(4)/data.getTotalCost() * 100) + "%"),
                calc("BKP5 % Anteil", (int) ((double) data.getBKP(5)/data.getTotalCost() * 100) + "%")
        );
    }

    private int getVolume() {
        return volumeUnderground + volumeAboveGround;
    }

    private Calculation calc(String titel, String value) {
        return new Calculation(titel, value);
    }

    private String chf(int amount) {
        return String.format(swissLocale, "%,d Fr.", amount);
    }

    private String perM2M3(int value, int volume) {
        return String.format(swissLocale, "%,d Fr.", value / volume);
    }

    private String perUnit(int value, int units) {
        return String.format(swissLocale, "%,d Fr.", value / units);
    }
}
