package api;

import model.Project;

import java.beans.PropertyChangeListener;
import java.util.TreeSet;

/**
 * Provides access to the project data stored in the cost benchmark database.
 * <p>
 * Write operations return a status message intended for display to the user.
 */
public interface DataService {

    /**
     * Adds a project to the database.
     * <p>
     * The version number is assigned automatically based on the project number.
     *
     * @param project the project to add
     * @return a status message describing the result of the operation
     */
    String addProject(Project project);

    /**
     * Updates an existing project version in the database.
     * <p>
     * The project is identified by its project number and version. Both remain unchanged.
     *
     * @param project the project containing the updated values
     * @return a status message describing the result of the operation
     */
    String modifyProject(Project project);

    /**
     * Removes a project version from the database.
     *
     * @param projectNr the project number
     * @param version   the version to remove
     * @return a status message describing the result of the operation
     */
    String deleteProject(int projectNr, int version);

    /**
     * Returns all projects stored in the database.
     *
     * @return a sorted set of projects, empty if the database is not available
     */
    TreeSet<Project> getProjects();

    /**
     * Returns whether the database is currently reachable.
     *
     * @return {@code true} if the database is available, {@code false} otherwise
     */
    boolean isDBAvailable();

    /**
     * Registers a listener that is notified when the database availability changes.
     *
     * @param l the listener to register
     */
    void onDbAvailableChanged(PropertyChangeListener l);

    /**
     * Registers a listener that is notified when the project data in the database changes.
     *
     * @param l the listener to register
     */
    void onDbChanged(PropertyChangeListener l);
}