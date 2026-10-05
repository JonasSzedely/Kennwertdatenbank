package services;

import java.io.InputStream;
import java.util.Properties;

public class SoftwareVersion {
    public static String get() {
        try (InputStream is = SoftwareVersion.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (is == null) return "0.0.0";

            Properties props = new Properties();
            props.load(is);
            return props.getProperty("app.version", "0.0.0");
        } catch (Exception e) {
            System.err.println("Version konnte nicht gelesen werden: " + e.getMessage());
            return "0.0.0";
        }
    }
}