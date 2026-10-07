package services;

import api.DataService;
import api.DatabaseService;
import db.DBConnection;
import model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.util.TreeSet;

/**
 * Central entry point of the application for all data and database operations.
 * <p>
 * Implements {@link DataService} and {@link DatabaseService} and delegates each
 * operation to the corresponding service class. Holds the shared database
 * connection and the background {@link DBCheckerService}.
 * <p>
 * If the database is not reachable at startup, the application runs in offline
 * mode until the connection becomes available.
 */
public final class KWDControllerService implements DataService, DatabaseService {

    private static final Logger LOG = LoggerFactory.getLogger(KWDControllerService.class);

    private final DBConnection database = new DBConnection();
    private final DBCheckerService dbChecker = new DBCheckerService(database);

    /**
     * Creates the controller and prepares the database.
     * <p>
     * Creates missing tables and updates the schema if needed. If the database is
     * not reachable, the controller starts in offline mode.
     */
    public KWDControllerService() {
        if (!initializeDatabase()) {
            LOG.warn("Starting in offline mode, database not available");
        }
    }

    @Override
    public String addProject(Project project) {
        return AddProjectService.add(project, database);
    }

    @Override
    public String modifyProject(Project project) {
        return ModifyProjectService.modify(project, database);
    }

    @Override
    public String deleteProject(int projectNr, int version) {
        return DeleteProjectService.delete(projectNr, version, database);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Marks the loaded state as current, so that only later changes are reported
     * to listeners registered via {@link #onDbChanged(PropertyChangeListener)}.
     */
    @Override
    public TreeSet<Project> getProjects() {
        if (!isDBAvailable()) {
            return new TreeSet<>();
        }
        TreeSet<Project> projects = GetProjectsService.get(database);
        dbChecker.refresh();
        return projects;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Listeners are notified on a background thread.
     */
    @Override
    public void onDbAvailableChanged(PropertyChangeListener l) {
        dbChecker.addPropertyChangeListener(DBCheckerService.PROP_DB_AVAILABLE, l);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Listeners are notified on a background thread.
     */
    @Override
    public void onDbChanged(PropertyChangeListener l) {
        dbChecker.addPropertyChangeListener(DBCheckerService.PROP_DB_CHANGED, l);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the result of the last background check without opening a connection.
     */
    @Override
    public boolean isDBAvailable() {
        return dbChecker.isDbAvailable();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Also brings an existing schema up to date via {@link CheckAndRepairDBService}.
     */
    @Override
    public boolean initializeDatabase() {
        boolean success = InitializeDBService.initialize(database);
        if (success) {
            CheckAndRepairDBService.check(database);
            dbChecker.refresh();
        }
        return success;
    }

    @Override
    public boolean testDBConnection(String url, String username, String password) {
        return database.isConnectionAvailable(url, username, password);
    }

    @Override
    public String getDBUrl() {
        return database.getURL();
    }

    @Override
    public String getDBUsername() {
        return database.getUsername();
    }

    @Override
    public String getDBPassword() {
        return database.getPassword();
    }

    @Override
    public boolean setDBConfig(String url, String username, String password) {
        return database.setConfig(url, username, password);
    }
}