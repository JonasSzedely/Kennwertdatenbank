package view;

import java.util.Locale;

public final class FormatConfig {
    public static final Locale NUMBER_FORMAT = Locale.of("de", "CH");

    private FormatConfig() {
    }

    public static Locale numberFormat() {
        return NUMBER_FORMAT;
    }
}