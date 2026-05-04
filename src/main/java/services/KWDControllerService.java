package model;

import api.KWDController;
import db.DBService;
import services.*;

import java.util.TreeMap;

public final class KWDControllerService implements KWDController {
    private final DBService database = new DBService();

    public KWDControllerService() {
        if (!initializeDatabase()) {
            System.err.println("Controller läuft im Offline-Modus");
        } else {
            checkAndRepairDB();
        }
    }

    @Override
    public boolean initializeDatabase() {
        return InitializeDBService.initialize();
    }

    @Override
    public boolean isDatabaseAvailable() {
        return database.isConnectionAvailable();
    }

    @Override
    public boolean testDBConnection(String url, String username, String password) {
        return database.isConnectionAvailable(url, username, password);
    }

    @Override
    public String addProject(Project project) {
        return AddProjectService.add(project);
    }

    @Override
    public String modifyProject(Project project) {
        return ModifyProjectService.modify(project);
    }

    @Override
    public String deleteProject(int projectNr, int version) {
        return DeleteProjectService.delete(projectNr,version);
    }

    @Override
    public TreeMap<Integer, Project> getProjects() {
        if (!isDatabaseAvailable()) {
            return new TreeMap<Integer, Project>();
        }
        return GetProjectsService.get();
    }

    public String getDBUrl() {
        return database.getURL();
    }

    public String getDBUsername() {
        return database.getUsername();
    }

    public String getDBPassword() {
        return database.getPassword();
    }

    public boolean setDBConfig(String url, String username, String password) {
        return database.setConfig(url, username, password);
    }

    private void checkAndRepairDB() {
        CheckAndRepairDBService.check();
    }
}
