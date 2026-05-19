package view.middlepane;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.Project;
import view.ProjectList;
import view.UICalculations;


class Statistics {
    private double averageRatioUG;
    private int averageWindowRatio;

    Statistics() {
        getCalculations();
    }

    HBox get() {
        HBox outerPane = new HBox(10);
        outerPane.setPadding(new Insets(10));
        GridPane grid = new GridPane(10, 10);
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        Label statisticsTitel = new Label("Statistik");
        statisticsTitel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label nrOfProjects = new Label("Anzahl Projekte:");
        Label nrOfProjectsValue = new Label(String.valueOf(ProjectList.getProjectList().size()));
        Label averageRatioUGLabel = new Label("⌀ Verhältnis UG/OG:");
        Label averageRatioUGValue = new Label(String.format("%.2f", averageRatioUG));
        Label averageWindowRatioLabel = new Label("⌀ Anteil Fenster/Fassade:");
        Label averageWindowRatioValue = new Label(averageWindowRatio + " %");

        grid.add(statisticsTitel, 0, 0);
        grid.add(nrOfProjects, 0, 1);
        grid.add(nrOfProjectsValue, 1, 1);
        grid.add(averageRatioUGLabel, 0, 2);
        grid.add(averageRatioUGValue, 1, 2);
        grid.add(averageWindowRatioLabel, 0, 3);
        grid.add(averageWindowRatioValue, 1, 3);

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            getCalculations();
            nrOfProjectsValue.setText(String.valueOf(ProjectList.getProjectList().size()));
            averageRatioUGValue.setText(String.format("%.2f", averageRatioUG));
        });

        outerPane.getChildren().add(grid);
        return outerPane;
    }

    private void getCalculations() {
        averageRatioUG = UICalculations.getAverageRatioUG();
        averageWindowRatio = UICalculations.getAverageWindowRatio();
    }


}
