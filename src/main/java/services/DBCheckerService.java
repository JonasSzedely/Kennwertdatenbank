package services;

import db.DBConnection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DBCheckerService {
    public static final String PROP_DB_AVAILABLE = "dbAvailable";
    public static final String PROP_DB_CHANGED = "dbChanged";

    private final DBConnection database;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private volatile int dbVersion = -1;
    private volatile boolean dbAvailable = false;
    private volatile boolean dbChanged = false;

    public DBCheckerService(DBConnection database) {
        this.database = database;
        this.dbVersion = validate();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "db-health-check");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::validate, 1, 5, TimeUnit.SECONDS);
    }

    private int validate() {
        String sql = "SELECT value FROM db_meta WHERE key = 'projects_version'";
        try (Connection conn = database.connect();
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {

            setDbAvailable(true);

            rs.next();
            int newVersion = rs.getInt("value");
            setDbChanged(newVersion != dbVersion);
            return newVersion;

        } catch (SQLException e) {
            setDbAvailable(false);
            return -1;
        }
    }

    // Setter feuern die Events
    private void setDbAvailable(boolean newValue) {
        boolean old = this.dbAvailable;
        this.dbAvailable = newValue;
        support.firePropertyChange(PROP_DB_AVAILABLE, old, newValue);
    }

    private void setDbChanged(boolean newValue) {
        boolean old = this.dbChanged;
        this.dbChanged = newValue;
        support.firePropertyChange(PROP_DB_CHANGED, old, newValue);
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener l) {
        support.addPropertyChangeListener(property, l);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener l) {
        support.removePropertyChangeListener(property, l);
    }

    public boolean isDbAvailable() {
        return dbAvailable;
    }

    public void refresh() {
        dbVersion = validate();
    }
}