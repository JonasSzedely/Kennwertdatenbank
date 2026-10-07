package model;

/**
 * Defines all attributes of a project.
 * <p>
 * Each attribute describes how it is stored, displayed and validated:
 * <ul>
 *   <li>{@code sqlColumn}: the column name in the {@code projects} table</li>
 *   <li>{@code label}: the German label shown in the user interface</li>
 *   <li>{@code type}: the value type, either {@link Integer} or {@link String}</li>
 *   <li>{@code options}: the allowed values for dropdowns, separated by {@code |},
 *       or empty for free input</li>
 *   <li>{@code min}, {@code max}: the allowed value range for numbers, or the
 *       allowed text length for strings. Not used for dropdowns.</li>
 * </ul>
 */
public enum ProjectAttributes {
    PROJECT_NR("project_nr", "Projekt-Nr.", Integer.class, "", 10000, 99999),
    VERSION("version", "Version", Integer.class, "", 1, 99),
    ADDRESS("address", "Adresse", String.class, "", 1, 22),
    /** Swiss postal code. */
    PLZ("plz", "PLZ", Integer.class, "", 1000, 9999),
    LOCATION("location", "Ort", String.class, "", 1, 22),
    OWNER("owner", "Eigentümer", String.class, "", 1, 22),
    PROPERTY_TYPE("property_type", "Nutzung", String.class, "Miete|Stockwerkeigentum|Gewerbe/Industrie|Wohnen+Gewerbe", 0, 0),
    CONSTRUCTION_TYPE("construction_type", "Bauart", String.class, "Neubau|Sanierung|Umbau|Anbau|Ausbau", 0, 0),
    /** SIA phase of the planning documents the cost estimate is based on. */
    DOCUMENT_PHASE("document_phase", "Planungsphase", Integer.class, "2|31|32|33|41|5", 0, 0),
    /** SIA phase in which the cost estimate was made. */
    CALCULATION_PHASE("calculation_phase", "Kalkulationsphase", Integer.class, "2|31|32|33|41|5", 0, 0),
    APARTMENTS_NR("apartments_nr", "Anzahl Wohnungen", Integer.class, "", 1, Integer.MAX_VALUE),
    BATHROOM_NR("bathroom_nr", "Anzahl Badezimmer", Integer.class, "", 1, Integer.MAX_VALUE),
    /** Main usable area (Hauptnutzfläche) including storage rooms, in m². */
    HNF("hnf", "HNF inkl. Reduit m²", Integer.class, "", 1, Integer.MAX_VALUE),
    /** Gross floor area (Geschossfläche), in m². */
    GF("gf", "GF m²", Integer.class, "", 1, Integer.MAX_VALUE),
    PARCEL_SIZE("parcelsize", "Grundstücksfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    LANDSCAPED_AREA("landscapedarea", "Umgebungsfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    VOLUME_UNDERGROUND("volume_underground", "Volumen UG m³", Integer.class, "", 1, Integer.MAX_VALUE),
    VOLUME_ABOVE_GROUND("volume_above_ground", "Volumen OG m³", Integer.class, "", 1, Integer.MAX_VALUE),
    FACADE_AREA("facadearea", "Fassadenfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    WINDOW_AREA("windowarea", "Fensterfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    FACADE_TYPE("facade_type", "Fassade", String.class, "AWD-Standard|AWD-Hochwertig|Zweischallen-Mauerwerk|Hinterlüftet-Holz|Hinterlüftet-Stein|Hinterlüftet-Metall", 0, 0),
    WINDOW_TYPE("window_type", "Fenster", String.class, "Kunststoff|Kunststoff-Metall|Metall|Holz|Holz-Metall", 0, 0),
    ROOF_TYPE("roof_type", "Dach", String.class, "Flachdach|Steildach|Flach-Steildach-Kombi", 0, 0),
    HEATING_TYPE("heating_type", "Heizung", String.class, "Luft-Luft|Luft-Wasser|Erdsonde|Gas|Öl|Pellet|Fernwärme|Unklar", 0, 0),
    COOLING_TYPE("cooling_type", "Kühlung", String.class, "keine|FreeCooling|Unklar", 0, 0),
    VENTILATION_TYPE_APARTMENTS("ventilation_type_apartments", "Lüftung Wohnungen", String.class, "keine|Abluft|KWL zentral|KWL je WHG|Unklar", 0, 0),
    VENTILATION_TYPE_UG("ventilation_type_ug", "Lüftung UG", String.class, "natürlich|Abluft|Zu- & Abluft|Unklar", 0, 0),
    /** Carbon monoxide and nitrogen oxide detection system in the parking garage. */
    CO_NO("co_no", "CO/NO-Anlage", String.class, "Ja|Nein|Unklar", 0, 0),
    SPECIAL("special", "Spezielles", String.class, "", 1, 65);

    private final String sqlColumn;
    private final String label;
    private final Class<?> type;
    private final String options;
    private final int min;
    private final int max;

    ProjectAttributes(String sqlColumn, String label, Class<?> type, String options, int min, int max) {
        this.sqlColumn = sqlColumn;
        this.label = label;
        this.type = type;
        this.options = options;
        this.min = min;
        this.max = max;
    }

    /**
     * Returns the column name in the {@code projects} table.
     *
     * @return the SQL column name
     */
    public String getSqlColumn() {
        return sqlColumn;
    }

    /**
     * Returns the label shown in the user interface.
     *
     * @return the German label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the value type of this attribute.
     *
     * @return {@link Integer} or {@link String}
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the allowed values for a dropdown.
     *
     * @return the options separated by {@code |}, or an empty string for free input
     * @see #isDropdown()
     */
    public String getOptions() {
        return options;
    }

    /**
     * Returns the lower limit.
     *
     * @return the minimum value for numbers or the minimum length for text
     */
    public int getMin() {
        return min;
    }

    /**
     * Returns the upper limit.
     *
     * @return the maximum value for numbers or the maximum length for text
     */
    public int getMax() {
        return max;
    }

    /**
     * Returns whether this attribute is selected from a fixed list of options.
     *
     * @return {@code true} if options are defined, {@code false} for free input
     */
    public boolean isDropdown() {
        return options != null && !options.isEmpty();
    }
}