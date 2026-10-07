package model;

import java.util.List;
import java.util.Locale;

/**
 * Calculates the key figures of a project for display.
 * <p>
 * All values are computed from the project's attributes and its costs per BKP code.
 * Results are formatted for the Swiss locale. If a divisor is zero, the value is
 * shown as {@value #NOT_AVAILABLE}.
 */
public class ProjectCalculations {

    private static final Locale SWISS_LOCALE = Locale.of("de", "CH");
    private static final String NOT_AVAILABLE = "-";

    private final ProjectData data;
    private final int hnf;
    private final int apartmentsNr;
    private final int parcelSize;
    private final int landscapedArea;
    private final int volumeUnderground;
    private final int volumeAboveGround;
    private final int windowArea;
    private final int facadeArea;

    /**
     * Creates the calculations for the given project.
     *
     * @param project the project, must have cost data and all area, volume and
     *                apartment attributes set
     * @throws NullPointerException if a required attribute is not set
     */
    public ProjectCalculations(Project project) {
        this.data = project.getData();
        this.hnf = project.get(ProjectAttributes.HNF);
        this.apartmentsNr = project.get(ProjectAttributes.APARTMENTS_NR);
        this.parcelSize = project.get(ProjectAttributes.PARCEL_SIZE);
        this.landscapedArea = project.get(ProjectAttributes.LANDSCAPED_AREA);
        this.volumeUnderground = project.get(ProjectAttributes.VOLUME_UNDERGROUND);
        this.volumeAboveGround = project.get(ProjectAttributes.VOLUME_ABOVE_GROUND);
        this.windowArea = project.get(ProjectAttributes.WINDOW_AREA);
        this.facadeArea = project.get(ProjectAttributes.FACADE_AREA);
    }

    /**
     * Returns all key figures in display order.
     * <p>
     * Groups of related figures are separated by {@link Calculation#SEPARATOR}.
     *
     * @return an unmodifiable list of key figures
     */
    public List<Calculation> getCalculations() {
        int totalCost = data.getTotalCost();

        return List.of(
                calc("Bausumme", chf(totalCost)),
                calc("Bausumme exkl. 29", chf(totalCost - data.getBKP(29))),
                calc("BKP 1", chf(data.getBKP(1))),
                calc("BKP 2", chf(data.getBKP(2))),
                calc("BKP 2 exkl. 29", chf(data.getBKP(2) - data.getBKP(29))),
                calc("BKP 211 + 212", chf(data.getBKP(211) + data.getBKP(212))),
                calc("BKP 23 (o. PV/E-Mob.)", chf(data.getBKP(23) - data.getBKP(2331) - data.getBKP(2332))),
                calc("BKP 241+242", chf(data.getBKP(241) + data.getBKP(242))),
                calc("BKP 244", chf(data.getBKP(244))),
                calc("BKP 250-257", chf(data.getBKP(25) - data.getBKP(258) - data.getBKP(259))),
                calc("Ausbau 1", chf(data.getBKP(27))),
                calc("Ausbau 2 (o. Res.)", chf(data.getBKP(28) - data.getBKP(289))),

                Calculation.SEPARATOR,

                calc("BKP 1-5/m3", chfPer(totalCost, getVolume())),
                calc("BKP 1-5/HNF", chfPer(totalCost, hnf)),
                calc("BKP 1-5/WHG", chfPer(totalCost, apartmentsNr)),

                Calculation.SEPARATOR,

                calc("BKP 2/m3", chfPer(data.getBKP(2), getVolume())),
                calc("BKP 2 exkl. 29/m3", chfPer(data.getBKP(2) - data.getBKP(29), getVolume())),
                calc("BKP 2/HNF", chfPer(data.getBKP(2), hnf)),
                calc("BKP 2/WHG", chfPer(data.getBKP(2), apartmentsNr)),
                calc("BKP 211/m3", chfPer(data.getBKP(211), getVolume())),

                Calculation.SEPARATOR,

                calc("BKP 230/m3", chfPer(data.getBKP(230), getVolume())),
                calc("BKP 230/HNF", chfPer(data.getBKP(230), hnf)),
                calc("BKP 242/HNF", chfPer(data.getBKP(242), hnf)),
                calc("BKP 244/HNF", chfPer(data.getBKP(244), hnf)),
                calc("BKP 250-257/HNF", chfPer(data.getBKP(25) - data.getBKP(258) - data.getBKP(259), hnf)),

                Calculation.SEPARATOR,

                calc("Ausbau 1/HNF", chfPer(data.getBKP(27), hnf)),
                calc("Ausbau 2/HNF", chfPer(data.getBKP(28), hnf)),
                calc("Ausbau 1+2/HNF", chfPer(data.getBKP(27) + data.getBKP(28), hnf)),

                Calculation.SEPARATOR,

                calc("BKP4 / Grundstücksf.", chfPer(data.getBKP(4), parcelSize)),
                calc("BKP4 / Umgebungsf.", chfPer(data.getBKP(4), landscapedArea)),

                Calculation.SEPARATOR,

                calc("HNF/WHG", hnfPerApartment()),
                calc("Verhältnis UG/OG", ratio(volumeUnderground, volumeAboveGround)),
                calc("Fenster Anteil", percent(windowArea, facadeArea)),
                calc("BKP1 % Anteil", percent(data.getBKP(1), totalCost)),
                calc("BKP2 % Anteil", percent(data.getBKP(2), totalCost)),
                calc("BKP3 % Anteil", percent(data.getBKP(3), totalCost)),
                calc("BKP4 % Anteil", percent(data.getBKP(4), totalCost)),
                calc("BKP5 % Anteil", percent(data.getBKP(5), totalCost))
        );
    }

    /**
     * Returns the total building volume.
     *
     * @return the sum of underground and above-ground volume in m³
     */
    private int getVolume() {
        return volumeUnderground + volumeAboveGround;
    }

    private Calculation calc(String name, String value) {
        return new Calculation(name, value);
    }

    /**
     * Formats an amount in Swiss francs.
     *
     * @param amount the amount in CHF
     * @return the formatted amount, for example {@code "1'234 Fr."}
     */
    private String chf(int amount) {
        return String.format(SWISS_LOCALE, "%,d Fr.", amount);
    }

    /**
     * Divides an amount by a reference quantity and formats it in Swiss francs.
     * <p>
     * The result is truncated to whole francs.
     *
     * @param amount  the amount in CHF
     * @param divisor the reference quantity, for example an area, volume or unit count
     * @return the formatted amount per unit, or {@value #NOT_AVAILABLE} if {@code divisor} is zero
     */
    private String chfPer(int amount, int divisor) {
        if (divisor == 0) {
            return NOT_AVAILABLE;
        }
        return chf(amount / divisor);
    }

    /**
     * Returns the main usable area per apartment.
     *
     * @return the formatted area, or {@value #NOT_AVAILABLE} if there are no apartments
     */
    private String hnfPerApartment() {
        if (apartmentsNr == 0) {
            return NOT_AVAILABLE;
        }
        return String.format(SWISS_LOCALE, "%,d m²", hnf / apartmentsNr);
    }

    /**
     * Formats the ratio of two values with two decimal places.
     *
     * @param value the numerator
     * @param base  the denominator
     * @return the formatted ratio, or {@value #NOT_AVAILABLE} if {@code base} is zero
     */
    private String ratio(int value, int base) {
        if (base == 0) {
            return NOT_AVAILABLE;
        }
        return String.format(SWISS_LOCALE, "%.2f", (double) value / base);
    }

    /**
     * Formats the share of a value as a whole percentage.
     * <p>
     * The result is truncated, not rounded.
     *
     * @param value the part
     * @param base  the whole
     * @return the formatted percentage, or {@value #NOT_AVAILABLE} if {@code base} is zero
     */
    private String percent(int value, int base) {
        if (base == 0) {
            return NOT_AVAILABLE;
        }
        return (int) ((double) value / base * 100) + " %";
    }
}