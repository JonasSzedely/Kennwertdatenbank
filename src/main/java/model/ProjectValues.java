package model;

public enum ProjectValues {
    PROJECT_NR("project_nr", "Projekt-Nr.", Integer.class, "", 10000, 99999),
    VERSION("version", "Version", Integer.class, "", 1, Integer.MAX_VALUE),
    ADDRESS("address", "Adresse", String.class, "", 1, 22),
    PLZ("plz", "PLZ", Integer.class, "", 1000, 9999),
    LOCATION("location", "Ort", String.class, "", 1, 22),
    OWNER("owner", "Eigentümer", String.class, "", 1, 22),
    PROPERTY_TYPE("property_type", "Liegenschaftsart", String.class, "Miete|Stockwerkeigentum|Gewerbe/Industrie|Wohnen+Gewerbe", 0, 0),
    CONSTRUCTION_TYPE("construction_type", "Bauart", String.class, "Neubau|Sanierung|Umbau|Anbau|Ausbau", 0, 0),
    DOCUMENT_PHASE("document_phase", "Planungsphase", Integer.class, "2|31|32|33|41|5", 0, 0),
    CALCULATION_PHASE("calculation_phase", "Kalkulationsphase", Integer.class, "2|31|32|33|41|5", 0, 0),
    APARTMENTS_NR("apartments_nr", "Anzahl Wohnungen", Integer.class, "", 1, Integer.MAX_VALUE),
    BATHROOM_NR("bathroom_nr", "Anzahl Badezimmer", Integer.class, "", 1, Integer.MAX_VALUE),
    HNF("hnf", "HNF inkl. Reduit m²", Integer.class, "", 1, Integer.MAX_VALUE),
    GF("gf", "GF m²", Integer.class, "", 1, Integer.MAX_VALUE),
    PARCEL_SIZE("parcelsize", "Grundstücksfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    LANDSCAPED_AREA("landscapedarea", "Umgebungsfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    VOLUME_UNDERGROUND("volume_underground", "Volumen UG m³", Integer.class, "", 1, Integer.MAX_VALUE),
    VOLUME_ABOVE_GROUND("volume_above_ground", "Volumen OG m³", Integer.class, "", 1, Integer.MAX_VALUE),
    FACADE_AREA("facadearea", "Fassadenfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    WINDOW_AREA("windowarea", "Fensterfläche m²", Integer.class, "", 1, Integer.MAX_VALUE),
    FACADE_TYPE("facade_type", "Fassade", String.class, "AWD-Standard|AWD-Hochwertig|Zweischallen-Mauerwerk|Hinterlüftet-Holz|Hinterlüftete-Stein|Hinterlüftete-Metall", 0, 0),
    WINDOW_TYPE("window_type", "Fenster", String.class, "Kunststoff|Kunststoff-Metall|Metall|Holz|Holz-Metall", 0, 0),
    ROOF_TYPE("roof_type", "Dach", String.class, "Flachdach|Steildach|Flach-Steildach-Kombi", 0, 0),
    HEATING_TYPE("heating_type", "Heizung", String.class, "Luft-Luft|Luft-Wasser|Erdsonde|Gas|Öl|Pellet|Fernwärme|Unklar", 0, 0),
    COOLING_TYPE("cooling_type", "Kühlung", String.class, "keine|FreeCooling|Unklar", 0, 0),
    VENTILATION_TYPE_APARTMENTS("ventilation_type_apartments", "Lüftung Wohnungen", String.class, "keine|Abluft|KWL zentral|KWL je Whg|Unklar", 0, 0),
    VENTILATION_TYPE_UG("ventilation_type_ug", "Lüftung UG", String.class, "natürlich|Abluft|Zu- & Abluft|Unklar", 0, 0),
    CO_NO("co_no", "CO/NO-Anlage", String.class, "Ja|Nein|Unklar", 0, 0),
    SPECIAL("special", "Spezielles", String.class, "", 1, 65);

    private final String sqlColumn;
    private final String label;
    private final Class<?> type;
    private final String options;
    private final int min;
    private final int max;

    ProjectValues(String sqlColumn, String label, Class<?> type, String options, int min, int max) {
        this.sqlColumn = sqlColumn;
        this.label = label;
        this.type = type;
        this.options = options;
        this.min = min;
        this.max = max;
    }

    public String getSqlColumn() {
        return sqlColumn;
    }

    public String getLabel() {
        return label;
    }

    public Class<?> getType() {
        return type;
    }

    public String getOptions() {
        return options;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean isDropdown() {
        return options != null && !options.isEmpty();
    }
}