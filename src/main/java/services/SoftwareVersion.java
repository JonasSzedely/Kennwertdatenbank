package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides the version number of the running application.
 * <p>
 * The version is read from {@code version.properties}, which Maven fills with
 * the project version during the build.
 */
public final class SoftwareVersion {

    private static final Logger LOG = LoggerFactory.getLogger(SoftwareVersion.class);
    private static final String FALLBACK = "0.0.0";
    private static final String VERSION = load();

    private SoftwareVersion() {
    }

    /**
     * Returns the version number of the application.
     *
     * @return the version, for example {@code "2.0.2"}, or {@code "0.0.0"}
     *         if it cannot be determined
     */
    public static String get() {
        return VERSION;
    }

    /**
     * Reads the version from {@code version.properties} on the classpath.
     *
     * @return the version, or {@code "0.0.0"} if the file is missing or unreadable
     */
    private static String load() {
        try (InputStream is = SoftwareVersion.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (is == null) {
                LOG.warn("version.properties not found, using {}", FALLBACK);
                return FALLBACK;
            }
            Properties props = new Properties();
            props.load(is);
            return props.getProperty("app.version", FALLBACK);
        } catch (IOException e) {
            LOG.error("Could not read version.properties", e);
            return FALLBACK;
        }
    }
}