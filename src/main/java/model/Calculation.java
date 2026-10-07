package model;

/**
 * A single key figure of a project, consisting of a label and its formatted value.
 * <p>
 * Instances are created by {@link ProjectCalculations}.
 */
public class Calculation {

    /** Marker used to separate groups of key figures in a list. Name and value are empty. */
    public static final Calculation SEPARATOR = new Calculation("", "");

    private final String name;
    private final String calculation;

    /**
     * Creates a key figure.
     *
     * @param name        the label, for example {@code "BKP 2/HNF"}
     * @param calculation the formatted value, for example {@code "1'234 Fr."}
     */
    Calculation(String name, String calculation) {
        this.name = name;
        this.calculation = calculation;
    }

    /**
     * Returns the label of the key figure.
     *
     * @return the label
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the formatted value of the key figure.
     *
     * @return the value including its unit
     */
    public String getCalculation() {
        return calculation;
    }
}