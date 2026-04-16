package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.VBox;
import model.Controller;
import model.Project;

import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.function.Predicate;

class ProjectList {
    private static final TreeMap<Integer, Project> treeMap = new TreeMap<>();
    private static final ObservableMap<Integer, Project> data = FXCollections.observableMap(treeMap);
    private static final ObservableList<Project> projectList = FXCollections.observableArrayList(data.values());
    private static final FilteredList<Project> filteredProjects = new FilteredList<>(projectList);
    private static final SortedList<Project> sortedProjects = new SortedList<>(filteredProjects);
    private static final LinkedHashMap<Integer, VBox> cellCache = new LinkedHashMap<>();
    private static Controller controller = null;


    public ProjectList(Controller controller) {
        ProjectList.controller = controller;
        refreshProjectList();
    }


    /**
     * retrieves the projects from the backend (database) and updates the lists for the UI
     */
    public static void refreshProjectList() {
        data.clear();
        data.putAll(controller.getProjects());
        controller.calculate();
        projectList.setAll(data.values());
    }

    public static TreeMap<Integer, Project> getTreeMap() {
        return treeMap;
    }

    public static ObservableList<Project> getProjectList() {
        return projectList;
    }

    public static FilteredList<Project> getFilteredProjects() {
        return filteredProjects;
    }

    public static SortedList<Project> getSortedProjects() {
        return sortedProjects;
    }

    public static void setPredicate(Predicate<Project> predicate) {
        filteredProjects.setPredicate(predicate);
    }
}