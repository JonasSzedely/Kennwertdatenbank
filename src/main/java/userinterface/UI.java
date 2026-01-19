package userinterface;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import kennwertdatenbank.Controller;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kennwertdatenbank.Project;
import kennwertdatenbank.Projects;
import org.controlsfx.control.RangeSlider;

import java.lang.reflect.Array;


public class UI extends Application {
    private Controller controller;
    private HBox projects;
    private ObservableList<Project> projectList;

    @Override
    public void start(Stage primaryStage) throws Exception {

        controller = new Controller();
        projectList = FXCollections.observableArrayList();
        projectList.addAll(controller.getProjects());

        VBox outerPane = new VBox(10);
        outerPane.setPadding(new Insets(10));
        outerPane.setAlignment(Pos.CENTER);

        //topPane Components
        HBox topPane = new HBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.setMinHeight(50);
        topPane.setMaxHeight(100);
        topPane.setStyle("-fx-background-color: lightblue;");

        HBox topLeft = new HBox();
        topLeft.setAlignment(Pos.TOP_LEFT);
        topLeft.setPadding(new Insets(20,20,20,20));
        Label titel = new Label("Kennwertdatenbank");
        topLeft.getChildren().add(titel);


        HBox topMiddle = new HBox();
        topMiddle.setAlignment(Pos.CENTER);
        topMiddle.setPadding(new Insets(20,0,20,0));
        /*
        //Label filters = new Label("Filters");
        int min = controller.getMin();
        int max = controller.getMax();
        RangeSlider minMaxValueSlider = new RangeSlider(min, max, min, max);
        minMaxValueSlider.setShowTickLabels(true);
        minMaxValueSlider.setShowTickMarks(true);
        minMaxValueSlider.setSnapToTicks(true);
        minMaxValueSlider.setMajorTickUnit(50_000);
        minMaxValueSlider.setBlockIncrement(50_000);
        minMaxValueSlider.setMinWidth(500);

        topMiddle.getChildren().addAll(minMaxValueSlider);
         */

        HBox.setHgrow(topMiddle, Priority.ALWAYS);


        HBox right = new HBox();
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(20,20,20,20));
        Button addProject = new Button("Neues Projekt");
        right.getChildren().add(addProject);


        topPane.getChildren().addAll(topLeft, topMiddle, right);


        //middle Pane Components
        HBox middlePane = new HBox();
        VBox.setVgrow(middlePane, Priority.ALWAYS);


        HBox middleLeftPane = new HBox();
        middleLeftPane.setAlignment(Pos.TOP_LEFT);
        middleLeftPane.setMinWidth(150);
        middleLeftPane.setFillHeight(true);


        ListView<Object> listLeft = new ListView<>();
        listLeft.getItems().addAll(
                new Label("Projekt Nr."),
                new Label("Adresse"),
                new Label("PLZ"),
                new Label("Ort"),
                new Label("Bauherr"),
                new Label("Art"),
                new Label("HNF in m²"),
                new Label("BKP 1"));

        middleLeftPane.getChildren().addAll(listLeft);
        listLeft.prefHeightProperty().bind(middleLeftPane.maxHeightProperty());


        ScrollPane middleRightPane = new ScrollPane();
        middleRightPane.setFitToHeight(true);

        projects = new HBox(10);
        projects.setMinWidth(150);
        projects.prefHeightProperty().bind(middleRightPane.hmaxProperty());

        middleRightPane.setContent(projects);


        projectList.addListener((javafx.collections.ListChangeListener<Project>) change -> {
            displayProjects();
        });

        displayProjects();




        //bottom Pane Components
        HBox bottomPane = new HBox();
        bottomPane.setMinHeight(20);
        bottomPane.setStyle("-fx-background-color: lightblue;");

        middlePane.getChildren().addAll(middleLeftPane,middleRightPane);
        outerPane.getChildren().addAll(topPane, middlePane,bottomPane);

        Scene scene = new Scene(outerPane, 1000,600);
        primaryStage.setScene(scene);

        //Height bind to scene height
        topPane.prefHeightProperty().bind(scene.heightProperty().multiply(0.1));
        bottomPane.prefHeightProperty().bind(scene.heightProperty().multiply(0.05));

        //eventhandlers for buttons
        addProject.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                var addProject = new AddProject(controller);
                try {
                    addProject.start(new Stage());
                    refreshProjectList();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //Width bind to scene width
        //middleLeftPane.prefWidthProperty().bind(scene.widthProperty().multiply(0.1));

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void refreshProjectList() {
        projectList.clear();
        projectList.addAll(controller.getProjects());
    }

    private void displayProjects() {
        projects.getChildren().clear();

        for(Project project : projectList) {
            ListView<Object> list = new ListView<>();
            list.setMinWidth(50);
            list.setMaxWidth(150);
            list.prefHeightProperty().bind(projects.heightProperty());

            list.getItems().addAll(
                    project.getProjectNr(),
                    project.getAddress(),
                    project.getPlz(),
                    project.getLocation(),
                    project.getOwner(),
                    project.getType(),
                    project.getSquareMeter(),
                    project.getData().getBKP(1));

            projects.getChildren().add(list);
        }
    }


}
