package api;

import model.Project;

import java.beans.PropertyChangeListener;
import java.util.TreeSet;

public interface DataService {
    String addProject(Project project);

    String modifyProject(Project project);

    String deleteProject(int projectNr, int version);

    TreeSet<Project> getProjects();

    boolean isDBAvailable();

    void onDbAvailableChanged(PropertyChangeListener l);

    void onDbChanged(PropertyChangeListener l);
}
