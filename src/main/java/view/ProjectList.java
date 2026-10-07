package view;

import api.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import model.Project;
import model.ProjectAttributes;

import java.util.*;
import java.util.function.Predicate;

public final class ProjectList {
    private static final ObservableList<Project> projectList = FXCollections.observableArrayList();
    private static final FilteredList<Project> filteredProjects = new FilteredList<>(projectList);
    private static final SortedList<Project> sortedProjects = new SortedList<>(filteredProjects, Comparator.naturalOrder());
    private static final Map<Integer, Integer> maxVersions = new HashMap<>();
    private static DataService controller = null;

    public ProjectList(DataService controller) {
        ProjectList.controller = controller;
        refreshProjectList();
    }

    public static void refreshProjectList() {
        maxVersions.clear();
        Set<Project> projects = controller.getProjects();

        projects.forEach(p -> {
            int nr = p.get(ProjectAttributes.PROJECT_NR);
            int v  = p.get(ProjectAttributes.VERSION);
            maxVersions.merge(nr, v, Math::max);
        });

        projectList.setAll(projects);
    }

    public static boolean isLatestVersion(Project p) {
        return maxVersions.getOrDefault(p.<Integer>get(ProjectAttributes.PROJECT_NR), -1)
                == p.<Integer>get(ProjectAttributes.VERSION);
    }

    public static ObservableList<Project> getProjectList() {
        return projectList;
    }

    public static SortedList<Project> getSortedProjects() {
        return sortedProjects;
    }

    public static void setPredicate(Predicate<Project> predicate) {
        filteredProjects.setPredicate(predicate);
    }
}