package view.bottompane;

import java.io.InputStream;
import java.util.Properties;

class SoftwareVersion {
    static String get() {
        try (InputStream is = SoftwareVersion.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (is == null) return "unbekannt";

            Properties props = new Properties();
            props.load(is);
            return props.getProperty("app.version", "0.0.0");
        } catch (Exception e) {
            System.err.println("Version konnte nicht gelesen werden: " + e.getMessage());
            return "error";
        }
    }
}