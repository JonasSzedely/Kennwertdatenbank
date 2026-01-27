package userinterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import kennwertdatenbank.Calculation;
import kennwertdatenbank.Controller;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kennwertdatenbank.Project;
import org.controlsfx.control.RangeSlider;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;


public class UI extends Application {
    private final Controller controller = new Controller();;
    private HBox projects;
    private final TreeMap<Integer, Project> treeMap = new TreeMap<>();
    private final ObservableMap<Integer, Project> data = FXCollections.observableMap(treeMap);
    ObservableList<Project> projectList = FXCollections.observableArrayList(data.values());
    FilteredList<Project> filteredProjects;
    private final Locale swissLocale = new Locale("de", "CH");
    //private Alert alert;

    @Override
    public void start(Stage primaryStage) {
        filteredProjects = new FilteredList<>(projectList, p -> true);
        filteredProjects.addListener((javafx.collections.ListChangeListener<Project>) c -> {
            displayProjects();
        });

        VBox outerPane = new VBox();
        outerPane.setPadding(new Insets(10));
        outerPane.setAlignment(Pos.CENTER);

        HBox topPane = topPane();
        HBox middlePane = middlePane();
        HBox bottomPane = bottomPane();

        outerPane.getChildren().addAll(topPane, middlePane, bottomPane);

        Scene scene = new Scene(outerPane, 1000,600);
        primaryStage.setScene(scene);

        //Height bind to scene height
        topPane.prefHeightProperty().bind(scene.heightProperty().multiply(0.1));
        bottomPane.prefHeightProperty().bind(scene.heightProperty().multiply(0.05));

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private HBox topPane(){
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

        HBox filters = filter();

        HBox.setHgrow(filters, Priority.ALWAYS);

        HBox topRight = new HBox();
        topRight.setAlignment(Pos.CENTER);
        topRight.setPadding(new Insets(20,20,20,20));

        Button addProjectButton = new Button("Neues Projekt");
        topRight.getChildren().add(addProjectButton);

        //Event-Handler for addProjectButton
        addProjectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                var addProject = new AddProject(controller);
                try {
                    Stage addStage = new Stage();
                    //addStage.initModality(Modality.APPLICATION_MODAL); // Macht es modal
                    addProject.start(addStage);
                    refreshProjectList();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        topPane.getChildren().addAll(topLeft, filters, topRight);

        return topPane;
    }

    private HBox middlePane(){
        HBox middlePane = new HBox();
        VBox.setVgrow(middlePane, Priority.ALWAYS);

        projects = new HBox();
        projects.setFillHeight(true);

        refreshProjectList();

        ScrollPane leftScroll = new ScrollPane();
        leftScroll.setFitToHeight(true);
        leftScroll.setMinWidth(150);
        //leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        ScrollPane rightScroll = new ScrollPane();
        rightScroll.setFitToHeight(true);

        leftScroll.vvalueProperty().bindBidirectional(rightScroll.vvalueProperty());

        VBox rowLabels = new VBox();
        rowLabels.setFillWidth(true);

        rowLabels.getChildren().addAll(
                projectLabel("Projekt-Nr."),
                projectLabel("Version"),
                projectLabel("Adresse"),
                projectLabel("PLZ"),
                projectLabel("Ort"),
                projectLabel("Bauherr"),
                projectLabel("Gebäudenutzungen"),
                projectLabel("Art des Bauvorhaben"),
                projectLabel("Planstand"),
                projectLabel("Gerechnete Phasen"),
                projectLabel("Anzahl Wohnungen"),
                projectLabel("Anzahl Nasszellen"),
                projectLabel("HNF in m²"),
                projectLabel("GF in m²"),
                projectLabel("SIA m³"),
                projectLabel("unten"),
                projectLabel("oben"),
                projectLabel("Fassaden-Typ"),
                projectLabel("Fenster-Typ"),
                projectLabel("Dach-Typ"),
                projectLabel("Heizungs-Typ"),
                projectLabel("Kühlungs-Typ"),
                projectLabel("Lüftungs-Typ Whg."),
                projectLabel("Lüftungs-Typ TG"),
                projectLabel("CO/NO-Anlage"),
                projectLabel("Sepzielles")//,
                //addCalculationButton
        );

        var projectsIterator = projectList.iterator();
        if(projectsIterator.hasNext()) {
            var project = projectsIterator.next();
            for (int i = 0; i < project.getCalculations().size(); i++) {
                rowLabels.getChildren().add(projectLabel(project.getCalculations().get(i).getName()));
            }
        }

        /*
        addCalculationButton.setMaxWidth(Double.MAX_VALUE);
        addCalculationButton.setPrefHeight(listLeft.getFixedCellSize()-5);
        addCalculationButton.setMinHeight(listLeft.getFixedCellSize()-5);

        addCalculationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                var addCallculation = new AddCalculation(controller);
                try {
                    Stage addStage = new Stage();
                    addStage.initModality(Modality.APPLICATION_MODAL); // Macht es modal
                    addCallculation.start(addStage);
                    refreshProjectList();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        */

        leftScroll.setContent(rowLabels);
        rightScroll.setContent(projects);


        Region spacer = new Region(); //spacer code and idea from claude.ai

        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox middleRightPane = new VBox(10);
        middleRightPane.setAlignment(Pos.TOP_RIGHT);
        middleRightPane.setMinWidth(250);
        middleRightPane.setStyle("-fx-background-color: lightblue;");

        middlePane.getChildren().addAll(leftScroll, rightScroll, spacer, middleRightPane);

        return middlePane;
    }

    private HBox bottomPane() {
        HBox bottomPane = new HBox();
        bottomPane.setMinHeight(20);
        bottomPane.setStyle("-fx-background-color: lightblue;");
        return bottomPane;
    }

    private void refreshProjectList() {
        data.clear();
        data.putAll(controller.getProjects());
        projectList.clear();
        projectList.addAll(data.values());

        displayProjects();
    }

    private void displayProjects() {
        projects.getChildren().clear();


        for(Project project : filteredProjects) {
            VBox projectBox = new VBox();
            projectBox.setMinWidth(150);
            projectBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 0 1;");

            ArrayList<String> calculations = new ArrayList<>();
            for (int i = 0; i < project.getCalculations().size(); i++) {
                int calculation = project.getCalculations().get(i).getCalculation();
                if (calculation != 0){
                    calculations.add(String.format(swissLocale, "%,d", project.getCalculations().get(i).getCalculation()));
                } else {
                    calculations.add("");
                }
            }

            projectBox.getChildren().addAll(
                    projectLabel(String.valueOf(project.getProjectNr())),
                    projectLabel(project.getVersion()),
                    projectLabel(project.getAddress()),
                    projectLabel(project.getPlz()),
                    projectLabel(project.getLocation()),
                    projectLabel(project.getOwner()),
                    projectLabel(project.getPropertyType()),
                    projectLabel(project.getConstructionType()),
                    projectLabel(project.getDocumentPhase()),
                    projectLabel(project.getCalculationPhase() + " - 5"),
                    projectLabel(project.getApartmentsNr()),
                    projectLabel(project.getBathroomNr()),
                    projectLabel(project.getHnf()),
                    projectLabel(project.getGf()),
                    projectLabel(project.getVolumeUnderground() + project.getVolumeAboveGround()),
                    projectLabel(project.getVolumeUnderground()),
                    projectLabel(project.getVolumeAboveGround()),
                    projectLabel(project.getFacadeType()),
                    projectLabel(project.getWindowType()),
                    projectLabel(project.getRoofType()),
                    projectLabel(project.getHeatingType()),
                    projectLabel(project.getCoolingType()),
                    projectLabel(project.getVentilationTypeApartments()),
                    projectLabel(project.getVentilationTypeUg()),
                    projectLabel(project.getCoNo()),
                    projectLabel(project.getSpecial())
                   // String.format(swissLocale, "%,d", project.getBathroomNr()),
                    //String.format(swissLocale, "%,d", project.getData().getTotalCost())
            );

            for (Calculation calc : project.getCalculations().values()){
                projectBox.getChildren().add(projectLabel(calc.getCalculation()));
            }

            projects.getChildren().add(projectBox);
        }
    }

    private HBox filter(){
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER);
        filters.setPadding(new Insets(20,0,20,0));

        TextField filterAddress = new TextField();
        filterAddress.setPromptText("Filter nach Adresse");

        TextField filterOwner = new TextField();
        filterOwner.setPromptText("Filter nach Bauherr");

        TextField filterPlz = new TextField();
        filterPlz.setPromptText("Filter nach PLZ");


        filterAddress.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                updateFilter(filteredProjects, filterAddress, filterOwner, filterPlz);
            }
        });
        filterAddress.setOnAction(e -> updateFilter(filteredProjects, filterAddress, filterOwner, filterPlz));

        filterOwner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                updateFilter(filteredProjects, filterAddress, filterOwner, filterPlz);
            }
        });
        filterOwner.setOnAction(e -> updateFilter(filteredProjects, filterAddress, filterOwner, filterPlz));

        filterPlz.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                updateFilter(filteredProjects, filterAddress, filterOwner, filterPlz);
            }
        });
        filterPlz.setOnAction(e -> updateFilter(filteredProjects, filterAddress, filterOwner, filterPlz));

        filters.getChildren().addAll(filterAddress, filterOwner, filterPlz);

        return filters;
    }

    private Label projectLabel(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(0,0,0,10));
        label.setMinHeight(30);
        label.setMaxHeight(30);
        label.setPrefHeight(30);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private Label projectLabel(int number) {
        Label label = new Label(String.format(swissLocale, "%,d",number));
        label.setPadding(new Insets(0,0,0,10));
        label.setMinHeight(30);
        label.setMaxHeight(30);
        label.setPrefHeight(30);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private void updateFilter(FilteredList<Project> filteredProjects,
                              TextField filterAddress,
                              TextField filterOwner,
                              TextField filterPlz) {

        filteredProjects.setPredicate(project -> {
            // Wenn alle Filter leer sind, zeige alles
            if (isEmpty(filterAddress) && isEmpty(filterOwner) && isEmpty(filterPlz)) {
                return true;
            }

            // Prüfe jeden Filter
            boolean matchAddress = isEmpty(filterAddress) ||
                    project.getAddress().toLowerCase().contains(filterAddress.getText().toLowerCase());

            boolean matchOwner = isEmpty(filterOwner) ||
                    project.getOwner().toLowerCase().contains(filterOwner.getText().toLowerCase());

            boolean matchPlz = isEmpty(filterPlz) ||
                    String.valueOf(project.getPlz()).contains(filterPlz.getText());

            // Alle Filter müssen matchen (AND-Verknüpfung)
            return matchAddress && matchOwner && matchPlz;
        });
    }
    private boolean isEmpty(TextField textField) {
        return textField.getText() == null || textField.getText().trim().isEmpty();
    }
}