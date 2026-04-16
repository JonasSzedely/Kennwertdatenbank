package view;

import java.io.InputStream;
import java.util.Properties;

class SoftwareVersion {
    public static String get() {
        try (InputStream is = SoftwareVersion.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (is == null) return "unbekannt";

            Properties props = new Properties();
            props.load(is);
            return props.getProperty("app.version", "unbekannt");
        } catch (Exception e) {
            System.err.println("Version konnte nicht gelesen werden: " + e.getMessage());
            return "unbekannt";
        }
    }
}