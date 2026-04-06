package view;

import java.util.Locale;

public final class FormatConfig {
    private FormatConfig() {}

    public static final Locale NUMBER_FORMAT = Locale.of("de", "CH");

    public static Locale numberFormat() {
        return Locale.of("de", "CH");
    }
}