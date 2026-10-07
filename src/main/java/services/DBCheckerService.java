package services;

import db.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors the database in the background.
 * <p>
 * Checks every {@value #CHECK_INTERVAL_SECONDS} seconds whether the database is
 * reachable and whether the project data was changed. Changes are detected via the
 * {@code projects_version} counter in {@code db_meta}, which a database trigger
 * increments on every change to the {@code projects} table.
 * <p>
 * Listeners are notified on a background thread. UI updates must therefore be
 * wrapped in {@code Platform.runLater}.
 */
public class DBCheckerService {

    private static final Logger LOG = LoggerFactory.getLogger(DBCheckerService.class);

    /** Property name fired when the database availability changes. */
    public static final String PROP_DB_AVAILABLE = "dbAvailable";

    /**
     * Property name fired when the project data in the database differs from the
     * last loaded state, or matches it again after {@link #refresh()}.
     */
    public static final String PROP_DB_CHANGED = "dbChanged";

    private static final int CHECK_INTERVAL_SECONDS = 5;
    private static final String VERSION_SQL = "SELECT value FROM db_meta WHERE key = 'projects_version'";

    private final DBConnection database;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private volatile int dbVersion;
    private volatile boolean dbAvailable = false;
    private volatile boolean dbChanged = false;

    /**
     * Creates the checker, runs a first check and starts the periodic background check.
     *
     * @param database the database connection to monitor
     */
    public DBCheckerService(DBConnection database) {
        this.database = database;
        this.dbVersion = validate();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "db-health-check");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::scheduledCheck, 1, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Runs a check from the scheduler.
     * <p>
     * Catches all runtime exceptions, since an uncaught exception would stop
     * the scheduler permanently.
     */
    private void scheduledCheck() {
        try {
            validate();
        } catch (RuntimeException e) {
            LOG.error("Database check failed unexpectedly", e);
        }
    }

    /**
     * Reads the current data version from the database and updates the state.
     *
     * @return the current data version, or {@code -1} if the database is not reachable
     */
    private synchronized int validate() {
        try (Connection conn = database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(VERSION_SQL)) {

            setDbAvailable(true);

            if (!rs.next()) {
                LOG.warn("Entry 'projects_version' missing in db_meta");
                return -1;
            }
            int newVersion = rs.getInt("value");
            setDbChanged(newVersion != dbVersion);
            return newVersion;

        } catch (SQLException e) {
            setDbAvailable(false);
            return -1;
        }
    }

    /**
     * Updates the availability and notifies listeners if it changed.
     *
     * @param newValue the new availability
     */
    private void setDbAvailable(boolean newValue) {
        boolean old = this.dbAvailable;
        this.dbAvailable = newValue;
        if (old != newValue) {
            if (newValue) {
                LOG.info("Database connection available");
            } else {
                LOG.warn("Database connection lost");
            }
        }
        support.firePropertyChange(PROP_DB_AVAILABLE, old, newValue);
    }

    /**
     * Updates the change state and notifies listeners if it changed.
     *
     * @param newValue {@code true} if the data differs from the last loaded state
     */
    private void setDbChanged(boolean newValue) {
        boolean old = this.dbChanged;
        this.dbChanged = newValue;
        support.firePropertyChange(PROP_DB_CHANGED, old, newValue);
    }

    /**
     * Registers a listener for a property.
     *
     * @param property the property name, {@link #PROP_DB_AVAILABLE} or {@link #PROP_DB_CHANGED}
     * @param l        the listener to register
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener l) {
        support.addPropertyChangeListener(property, l);
    }

    /**
     * Removes a listener for a property.
     *
     * @param property the property name
     * @param l        the listener to remove
     */
    public void removePropertyChangeListener(String property, PropertyChangeListener l) {
        support.removePropertyChangeListener(property, l);
    }

    /**
     * Returns whether the database was reachable at the last check.
     *
     * @return {@code true} if the database is available, {@code false} otherwise
     */
    public boolean isDbAvailable() {
        return dbAvailable;
    }

    /**
     * Marks the current database state as loaded.
     * <p>
     * Must be called after the projects were read, so that only later changes
     * are reported via {@link #PROP_DB_CHANGED}.
     */
    public synchronized void refresh() {
        dbVersion = validate();
    }
}