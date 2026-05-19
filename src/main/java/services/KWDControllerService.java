package services;

import api.DataService;
import api.DatabaseService;
import db.DBConnection;
import model.Project;

import java.beans.PropertyChangeListener;
import java.util.TreeSet;

public final class KWDControllerService implements DataService, DatabaseService {
    private final DBConnection database = new DBConnection();
    private final DBCheckerService dbChecker = new DBCheckerService(database);

    public KWDControllerService() {
        if (!initializeDatabase()) {
            System.err.println("Controller läuft im Offline-Modus");
        } else {
            CheckAndRepairDBService.check(database);
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

    @Override
    public TreeSet<Project> getProjects() {
        if (!isDBAvailable()) {
            return new TreeSet<Project>();
        }
        TreeSet<Project> set = GetProjectsService.get(database);
        dbChecker.refresh();
        return set;
    }

    @Override
    public void onDbAvailableChanged(PropertyChangeListener l) {
        dbChecker.addPropertyChangeListener(DBCheckerService.PROP_DB_AVAILABLE, l);
    }

    @Override
    public void onDbChanged(PropertyChangeListener l) {
        dbChecker.addPropertyChangeListener(DBCheckerService.PROP_DB_CHANGED, l);
    }

    @Override
    public boolean isDBAvailable() {
        return dbChecker.isDbAvailable();
    }

    @Override
    public boolean initializeDatabase() {
        boolean success =  InitializeDBService.initialize(database);
        if(success){
            dbChecker.refresh();
        }
        return  success;
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
