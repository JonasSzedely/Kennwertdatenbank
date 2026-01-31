package userinterface;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import kennwertdatenbank.Calculation;
import kennwertdatenbank.Controller;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kennwertdatenbank.Project;
import org.controlsfx.control.ToggleSwitch;

import java.util.Locale;
import java.util.TreeMap;


public class UI extends Application {
    private final Controller controller = new Controller();;
    private HBox projects;
    private final TreeMap<Integer, Project> treeMap = new TreeMap<>();
    private final ObservableMap<Integer, Project> data = FXCollections.observableMap(treeMap);
    private ObservableList<Project> projectList = FXCollections.observableArrayList(data.values());
    private FilteredList<Project> filteredProjects;
    private final Locale swissLocale = new Locale("de", "CH");
    private Alert alert;
    private RangeFilter sumFilter;
    private RangeFilter apartmentNrFilter;


    @Override
    public void start(Stage primaryStage) {
        filteredProjects = new FilteredList<>(projectList, p -> true);
        filteredProjects.addListener((javafx.collections.ListChangeListener<Project>) c -> {
            displayProjects();
        });
        controller.calculate();

        VBox outerPane = new VBox();
        outerPane.setPadding(new Insets(10));
        outerPane.setAlignment(Pos.CENTER);

        HBox topPane = topPane();
        HBox middlePane = middlePane();
        HBox bottomPane = bottomPane();

        outerPane.getChildren().addAll(topPane, middlePane, bottomPane);

        Scene scene = new Scene(outerPane, 1000,600);
        var cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS-Datei nicht gefunden!");
        }
        primaryStage.setScene(scene);

        //Height bind to scene height
        bottomPane.prefHeightProperty().bind(scene.heightProperty().multiply(0.05));

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private HBox topPane(){
        HBox topPane = new HBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.setMinHeight(150);
        topPane.setStyle("-fx-background-color: lightblue;");


        HBox topLeft = new HBox();
        topLeft.setAlignment(Pos.CENTER);
        topLeft.setPadding(new Insets(20,20,20,20));
        topLeft.setMinWidth(150);
        topPane.setMaxHeight(Double.MAX_VALUE);
        Label titel = new Label("Kennwert\n    datenbank");
        titel.setStyle(
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; "
        );

        titel.setAlignment(Pos.CENTER);
        topLeft.getChildren().add(titel);

        HBox filters = filters();
        HBox.setHgrow(filters, Priority.ALWAYS);

        HBox topRight = new HBox(10);
        topRight.setAlignment(Pos.CENTER);
        topRight.setMinWidth(250);

        Button addProjectButton = new Button("Neues Projekt");
        topRight.getChildren().add(addProjectButton);
        addProjectButton.setAlignment(Pos.CENTER);

        //Event-Handler for addProjectButton
        addProjectButton.setOnAction(actionEvent -> {
            var addProject = new AddProject(controller);
            try {
                Stage newStage = new Stage();
                addProject.start(newStage);

            } catch (Exception e) {
                throw new RuntimeException(e);
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
        leftScroll.setMaxWidth(150);
        leftScroll.getStyleClass().add("no-scrollbar");
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        ScrollPane rightScroll = new ScrollPane();
        rightScroll.setFitToHeight(true);
        rightScroll.setFitToWidth(true);
        HBox.setHgrow(rightScroll, Priority.ALWAYS);

        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        //synchronize scrolling of left and right scrollPane (code from claude.ai)
        leftScroll.vvalueProperty().bindBidirectional(rightScroll.vvalueProperty());

        VBox rowLabels = new VBox();
        rowLabels.prefWidthProperty().bind(leftScroll.widthProperty());



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
                //projectLabel("Anzahl Nasszellen"),
                projectLabel("HNF in m²"),
                projectLabel("GF in m²"),
                projectLabel("SIA m³"),
                //projectLabel("unten"),
                //projectLabel("oben"),
                projectLabel("Fassaden-Typ"),
                projectLabel("Fenster-Typ"),
                projectLabel("Dach-Typ"),
                projectLabel("Heizungs-Typ"),
                projectLabel("Kühlungs-Typ"),
                projectLabel("Lüftungs-Typ Whg."),
                projectLabel("Lüftungs-Typ TG"),
                projectLabel("CO/NO-Anlage"),
                projectLabel("Sepzielles"),
                projectLabel("")//,
        );

        var projectsIterator = projectList.iterator();
        if(projectsIterator.hasNext()) {
            var project = projectsIterator.next();
            for (int i = 0; i < project.getCalculations().size(); i++) {
                rowLabels.getChildren().add(projectLabel(project.getCalculations().get(i).getName()));
            }
        }
        HBox noScroll = new HBox();
        noScroll.setMinHeight(15);
        rowLabels.getChildren().add(noScroll);

        leftScroll.setContent(rowLabels);
        rightScroll.setContent(projects);
        leftScroll.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT); //Lösung von claude.ai
        rowLabels.setNodeOrientation(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT); //Lösung von claude.ai

        VBox middleRightPane = new VBox(10);
        middleRightPane.setAlignment(Pos.TOP_RIGHT);
        middleRightPane.setMinWidth(250);
        middleRightPane.setStyle("-fx-background-color: lightblue;");
        middleRightPane.getChildren().add(statistics());

        middlePane.getChildren().addAll(leftScroll,rightScroll, middleRightPane);

        return middlePane;
    }

    private HBox bottomPane() {
        HBox bottomPane = new HBox();
        bottomPane.setMinHeight(20);
        //bottomPane.setStyle("-fx-background-color: lightblue;");
        return bottomPane;
    }

    public void refreshProjectList() {
        data.clear();
        data.putAll(controller.getProjects());
        projectList.setAll(data.values());
        controller.calculate();
    }

    private void displayProjects() {
        projects.getChildren().clear();

        for(Project project : filteredProjects) {
            VBox projectBox = new VBox();
            projectBox.setMinWidth(150);
            projectBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 0 1;");

            /*
            ArrayList<String> calculations = new ArrayList<>();
            for (int i = 0; i < project.getCalculations().size(); i++) {
                int calculation = project.getCalculations().get(i).getCalculation();
                if (calculation != 0){
                    calculations.add(String.format(swissLocale, "%,d", project.getCalculations().get(i).getCalculation()));
                } else {
                    calculations.add("");
                }
            }

             */

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
                    //projectLabel(project.getBathroomNr()),
                    projectLabel(project.getHnf()),
                    projectLabel(project.getGf()),
                    projectLabel(project.getVolumeUnderground() + project.getVolumeAboveGround()),
                    //projectLabel(project.getVolumeUnderground()),
                    //projectLabel(project.getVolumeAboveGround()),
                    projectLabel(project.getFacadeType()),
                    projectLabel(project.getWindowType()),
                    projectLabel(project.getRoofType()),
                    projectLabel(project.getHeatingType()),
                    projectLabel(project.getCoolingType()),
                    projectLabel(project.getVentilationTypeApartments()),
                    projectLabel(project.getVentilationTypeUg()),
                    projectLabel(project.getCoNo()),
                    projectLabel(project.getSpecial()),
                    projectLabel("")
                   // String.format(swissLocale, "%,d", project.getBathroomNr()),
                    //String.format(swissLocale, "%,d", project.getData().getTotalCost())
            );

            for (Calculation calc : project.getCalculations().values()){
                projectBox.getChildren().add(projectLabel(calc.getCalculation()));
            }

            projects.getChildren().add(projectBox);
        }
    }

    private HBox filters(){
        HBox outerPane = new HBox();
        outerPane.setAlignment(Pos.CENTER);
        outerPane.setPadding(new Insets(20,0,20,0));

        GridPane filterBox = new GridPane(10,10);
        filterBox.getColumnConstraints().addAll(new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100));

        Label projectNrLabel = new Label("Projektnummer");
        TextField filterProjectNr = new TextField();
        filterProjectNr.setPromptText("Filter nach Projektnummer");
        filterBox.add(projectNrLabel, 0,0);
        filterBox.add(filterProjectNr, 1,0);

        Label versionLabel = new Label("Zeige Versionen");
        ToggleSwitch versionFilter = new ToggleSwitch();
        versionFilter.setAlignment(Pos.CENTER_RIGHT);
        versionFilter.setMinWidth(80);
        versionFilter.setPrefWidth(80);
        versionFilter.setMaxWidth(80);
        versionFilter.setSelected(false);
        versionFilter.setText("alle");
        filterBox.add(versionLabel, 0,1);
        filterBox.add(versionFilter, 1,1);

        Label plzLabel = new Label("Postleitzahl");
        TextField filterPlz = new TextField();
        filterPlz.setPromptText("Filter nach PLZ");
        filterBox.add(plzLabel,0,2);
        filterBox.add(filterPlz,1,2);


        sumFilter = new RangeFilter(
                "Bausumme",
                "Reset",
                swissLocale,
                project -> project.getData().getTotalCost(),
                controller::getMinTotalCost,
                controller::getMaxTotalCost);

        apartmentNrFilter = new RangeFilter(
                "Wohnungen",
                "Reset",
                swissLocale,
                Project::getApartmentsNr,
                controller::getMinApartments,
                controller::getMaxApartments
                );


        /*
        Pane sumFilterContainer = new VBox();
        sumFilterContainer.getChildren().addAll(sumFilter.getFilterBox(10,10));
        sumFilterContainer.setStyle(
                "-fx-border-color: #555555; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 0px; " +
                        "-fx-padding: 10px;"
        );
        filterBox.add(sumFilterContainer, 2, 0, 2, 3);

         */


        filterBox.add(sumFilter.getTitelLabel(),2,0);
        filterBox.add(sumFilter.getResetButton(),3,0);
        filterBox.add(sumFilter.getSlider(),2,1);
        GridPane.setColumnSpan(sumFilter.getSlider(),2);
        filterBox.add(sumFilter.getMinTextField(),2,2);
        filterBox.add(sumFilter.getMaxTextField(),3,2);

        filterBox.add(apartmentNrFilter.getTitelLabel(), 4, 0);
        filterBox.add(apartmentNrFilter.getResetButton(), 5, 0);
        filterBox.add(apartmentNrFilter.getSlider(),4,1);
        GridPane.setColumnSpan(apartmentNrFilter.getSlider(),2);
        filterBox.add(apartmentNrFilter.getMinTextField(),4,2);
        filterBox.add(apartmentNrFilter.getMaxTextField(),5,2);

        sumFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, filterPlz));

        apartmentNrFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr,versionFilter,filterPlz));

        filterProjectNr.setOnAction(e -> updateFilter(filterProjectNr, versionFilter, filterPlz));

        projectList.addListener(new ListChangeListener<Project>() {
            @Override
            public void onChanged(Change<? extends Project> c) {
                sumFilter.setRange();
                apartmentNrFilter.setRange();
            }
        });

        versionFilter.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                versionFilter.setText("neuste");
            } else {
                versionFilter.setText("alle");
            }
            updateFilter(filterProjectNr, versionFilter, filterPlz);
        });

        filterPlz.setOnAction(e -> updateFilter(filterProjectNr, versionFilter, filterPlz));


        outerPane.getChildren().addAll(filterBox);
        return outerPane;
    }

    private void updateFilter(TextField filterProjectNr,
                              ToggleSwitch versionFilter,
                              TextField filterPlz) {

        if (filteredProjects == null) return;  // Null-Check

        filteredProjects.setPredicate(project -> {

            if (isEmpty(filterProjectNr) && isEmpty(filterPlz) && !versionFilter.isSelected()) {
                return sumFilter.getPredicate().test(project) && apartmentNrFilter.getPredicate().test(project);
            }

            boolean matchProjectNrFilter = isEmpty(filterProjectNr) || String.valueOf(project.getProjectNr()).contains(filterProjectNr.getText().toLowerCase());

            boolean matchVersionFilter = true;
            if (versionFilter.isSelected()) {
                int version = project.getVersion();
                while(data.containsKey((project.getProjectNr()*100) + version+1)){
                    version++;
                }
                if (project.getVersion() != version){
                    matchVersionFilter = false;
                }
            }

            boolean matchPlz = isEmpty(filterPlz) || String.valueOf(project.getPlz()).contains(filterPlz.getText());

            boolean matchSumFilter = sumFilter.getPredicate().test(project);

            boolean matchApartmentFilter = apartmentNrFilter.getPredicate().test(project);

            return matchProjectNrFilter && matchVersionFilter && matchPlz && matchSumFilter && matchApartmentFilter;
        });
    }

    private HBox statistics(){
        HBox outerPane = new HBox(10);
        outerPane.setPadding(new Insets(10));
        GridPane grid = new GridPane(10, 10);
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        Label statisticsTitel = new Label("Statistik");
        statisticsTitel.setStyle(
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; "
        );
        Label nrOfProjects = new Label("Anzahl Projekte:");
        Label nrOfProjectsValue = new Label(String.valueOf(projectList.size()));
        Label avarageRatioUG = new Label("⌀ Verhältnis UG/OG:");
        Label avarageRatioUGValue = new Label(String.format("%.2f", controller.getAvarageRatioUG()));


        grid.add(statisticsTitel,0,0);
        grid.add(nrOfProjects,0,1);
        grid.add(nrOfProjectsValue, 1,1);
        grid.add(avarageRatioUG,0,2);
        grid.add(avarageRatioUGValue,1,2);

        projectList.addListener(new ListChangeListener<Project>() {
            @Override
            public void onChanged(Change<? extends Project> c) {
                nrOfProjectsValue.setText(String.valueOf(projectList.size()));
                avarageRatioUGValue.setText(String.format("%.2f", controller.getAvarageRatioUG()));
            }
        });

        outerPane.getChildren().add(grid);

        return outerPane;
    }


    private boolean isEmpty(TextField textField) {
        return textField.getText() == null || textField.getText().trim().isEmpty();
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
        if(number == 0){
            label.setText("");
        }
        return label;
    }







}