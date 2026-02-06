package userinterface;

import database.DBConfig;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import kennwertdatenbank.Calculation;
import kennwertdatenbank.Controller;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kennwertdatenbank.Project;
import org.controlsfx.control.ToggleSwitch;

import java.awt.event.MouseListener;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class UI extends Application {
    private Controller controller;
    private final TreeMap<Integer, Project> treeMap = new TreeMap<>();
    private final ObservableMap<Integer, Project> data = FXCollections.observableMap(treeMap);
    private ObservableList<Project> projectList = FXCollections.observableArrayList(data.values());
    private FilteredList<Project> filteredProjects = new FilteredList<>(projectList);
    private SortedList<Project> sortedProjects = new SortedList<>(filteredProjects);
    private final Locale swissLocale = new Locale("de", "CH");
    private RangeFilter sumFilter;
    private RangeFilter apartmentNrFilter;
    private ScrollPane rightScroll;
    private HBox projectsContainer;       // Enthält nur die SICHTBAREN Projekt-Boxen
    private VBox rowLabels;
    private final double CELL_WIDTH = 150;
    private final double CELL_GAP = 0;
    private final int SCROLLBAR_WIDTH = 15;
    private final LinkedHashMap<Integer, VBox> cellCache = new LinkedHashMap<>();
    private static final int MAX_CACHE_SIZE = 50;


    @Override
    public void start(Stage primaryStage) {
        controller = new Controller();

        VBox outerPane = new VBox();
        outerPane.setPadding(new Insets(5));
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

        if (!controller.isDatabaseAvailable()) {
            showDatabaseWarning();
        }
    }

    /**
     * opens a new warning window that the DB couldn't be connected
     */
    private void showDatabaseWarning() {
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("Datenbankverbindung");
        warning.setHeaderText("Keine Datenbankverbindung");
        warning.setContentText(
                "Die Anwendung konnte keine Verbindung zur Datenbank herstellen.\n" +
                        "Das Programm läuft im Offline-Modus mit eingeschränkter Funktionalität.\n\n" +
                        "Bitte prüfen Sie:\n" +
                        "- Ist die Datenbank gestartet?\n" +
                        "- Sind die Zugangsdaten in den Einstellungen korrekt?\n" +
                        "- Ist die Netzwerkverbindung verfügbar?"
        );
        warning.getDialogPane().setMinWidth(500);
        warning.getDialogPane().setPrefWidth(500);
        warning.show();
    }

    /**
     * creates the top pane
     * @return HBox
     */
    private HBox topPane(){
        HBox topPane = new HBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.setMinHeight(150);
        topPane.setStyle("-fx-background-color: #052048; -fx-border-color: lightgray; -fx-border-width: 1 1 0 1;");
        topPane.setId("top-pane");

        HBox topLeft = new HBox();
        topLeft.setAlignment(Pos.CENTER);
        topLeft.setPadding(new Insets(20,20,20,20));
        topLeft.setMinWidth(150);
        topPane.setMaxHeight(Double.MAX_VALUE);
        Label titel = new Label("Kennwert\n    Datenbank");
        titel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        titel.setAlignment(Pos.CENTER);
        topLeft.getChildren().add(titel);

        HBox filters = filters();
        HBox.setHgrow(filters, Priority.ALWAYS);

        HBox topRight = new HBox(10);
        topRight.setAlignment(Pos.CENTER);
        topRight.setMinWidth(250);

        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button addOptionsButton = new Button("Einstellungen");
        Button addProjectButton = new Button("Neues Projekt");

        buttonBox.getChildren().addAll(addOptionsButton, addProjectButton);

        //Event-Handler for addProjectButton
        addProjectButton.setOnAction(event -> {
            if (!controller.isDatabaseAvailable()) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Keine Datenbankverbindung");
                error.setContentText("Projekte können nur bei aktiver Datenbankverbindung hinzugefügt werden.");
                error.show();
                return;
            }
            var addProject = new ProjectInputWindow(controller);
            try {
                Stage newStage = new Stage();
                addProject.start(newStage);
                if(addProject.getAddButtonStatus()) {
                    refreshProjectList();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //Event-Handler for addOptionsButton
        addOptionsButton.setOnAction(event -> {
            TextInputDialog settingsPW = new TextInputDialog();
            settingsPW.setTitle("Einstellung");
            settingsPW.setHeaderText("Bitte Passwort für die Einstellungen eingeben");
            settingsPW.setContentText("Passwort:");

            TextField oldEditor = settingsPW.getEditor();
            GridPane content = (GridPane) settingsPW.getDialogPane().getContent();

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Passwort eingeben");

            content.getChildren().remove(oldEditor);
            content.add(passwordField, 1, 0);

            settingsPW.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return passwordField.getText();
                }
                return null;
            });
            Optional<String> result = settingsPW.showAndWait();

            if(!result.isPresent()){
                return;
            }

            if (result.get().equals("IMAG")){
                var options = new Settings(controller);
                try {
                    Stage newStage = new Stage();
                    options.start(newStage);
                    if(options.isSetButtonUsed()) {
                        DBConfig.loadProperties(); // Lade neue Properties
                        if (controller.initializeDatabase()) {
                            // Erfolgreich verbunden
                            refreshProjectList(); // Lädt Projekte neu

                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Datenbankverbindung");
                            success.setHeaderText("Verbindung erfolgreich");
                            success.setContentText("Die Datenbankverbindung wurde erfolgreich wiederhergestellt.");
                            success.show();
                        } else {
                            // Verbindung fehlgeschlagen
                            refreshProjectList(); // Aktualisiert UI trotzdem (zeigt leere Liste)
                            showDatabaseWarning();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            Alert wrongPW = new Alert(Alert.AlertType.CONFIRMATION);
            wrongPW.setTitle("Einstellungen");
            wrongPW.setHeaderText("Falsches Passwort");
            wrongPW.setContentText("Noch einmal versuchen?");
            Optional<ButtonType> clicked = wrongPW.showAndWait();
            if (clicked.get() == ButtonType.OK){
                addOptionsButton.fire();
            } else {
                wrongPW.close();
            }

        });

        topRight.getChildren().add(buttonBox);
        topPane.getChildren().addAll(topLeft, filters, topRight);

        return topPane;
    }

    /**
     * creates the middle pane
     * @return HBox
     */
    private HBox middlePane(){
        HBox middlePane = new HBox();
        VBox.setVgrow(middlePane, Priority.ALWAYS);
        middlePane.setId("middle-pane");

        refreshProjectList();

        // left side of the middle pane
        // scroll pane for row labels
        ScrollPane leftScroll = new ScrollPane();
        leftScroll.setFitToHeight(true);
        leftScroll.setMinWidth(150);
        leftScroll.setMaxWidth(150);
        leftScroll.getStyleClass().add("no-scrollbar");
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox rowLabels = displayRowLabels();
        rowLabels.setNodeOrientation(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT); //Lösung von claude.ai
        rowLabels.prefWidthProperty().bind(leftScroll.widthProperty());
        leftScroll.setContent(rowLabels);

        // middle of the middle pane
        // pane to know the space the projects have
        Pane totalWidthSpacer = new Pane();

        // container for the projects
        projectsContainer = new HBox(CELL_GAP);
        projectsContainer.setFillHeight(true);

        // this is used to overlay two panes to enable virtual scrolling
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.TOP_LEFT);
        stackPane.getChildren().addAll(totalWidthSpacer, projectsContainer);

        // scroll pane for projects
        rightScroll = new ScrollPane(stackPane);
        rightScroll.setFitToHeight(true);
        rightScroll.setFitToWidth(false);
        HBox.setHgrow(rightScroll, Priority.ALWAYS);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        //synchronize scrolling of left and right scrollPane (code from claude.ai)
        leftScroll.vvalueProperty().bindBidirectional(rightScroll.vvalueProperty());

        //update cells while scrolling right (code from claude.ai)
        rightScroll.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            updateVisibleCells();
        });

        //update cells while scrolling left (code from claude.ai)
        rightScroll.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateVisibleCells();
        });

        //update cells when projects get updated (code from claude.ai)
        filteredProjects.addListener((ListChangeListener<Project>) c -> {
            double totalWidth = filteredProjects.size() * (CELL_WIDTH + CELL_GAP);
            totalWidthSpacer.setPrefWidth(totalWidth);
            totalWidthSpacer.setMinWidth(totalWidth);
            cellCache.clear();
            updateVisibleCells();
        });

        //right side of the middle pane
        VBox middleRightPane = new VBox(10);
        middleRightPane.setAlignment(Pos.TOP_RIGHT);
        middleRightPane.setMinWidth(250);
        middleRightPane.getChildren().add(statistics());
        middleRightPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1 1 1 1;");

        middlePane.getChildren().addAll(leftScroll,rightScroll, middleRightPane);
        refreshProjectList();
        return middlePane;
    }

    /**
     * creates the bottom pane
     * @return HBox
     */
    private HBox bottomPane() {
        HBox bottomPane = new HBox();
        bottomPane.setMinHeight(30);
        bottomPane.setPadding(new Insets(0,20,0,0));
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottom-pane");
        bottomPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 1 1;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Label für Datum und Uhrzeit erstellen
        Label dateTimeLabel = new Label();
        dateTimeLabel.setAlignment(Pos.CENTER);

        // Formatter für die Anzeige
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        // Timeline für automatische Aktualisierung jede Sekunde
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    dateTimeLabel.setText(ZonedDateTime.now().format(formatter));
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Erste Anzeige sofort setzen
        dateTimeLabel.setText(ZonedDateTime.now().format(formatter));

        bottomPane.getChildren().addAll(spacer, dateTimeLabel);
        return bottomPane;
    }

    /**
     * creates the filters
     * @return HBox
     */
    private HBox filters(){
        HBox outerPane = new HBox();
        outerPane.setAlignment(Pos.CENTER);
        outerPane.setPadding(new Insets(20,0,20,0));

        GridPane filterBox = new GridPane(10,10);
        filterBox.getColumnConstraints().addAll(new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(100));

        //filter project number
        Label projectNrLabel = new Label("Projektnummer");
        TextField filterProjectNr = new TextField();
        filterProjectNr.setPromptText("Filter nach Projektnummer");
        filterBox.add(projectNrLabel, 0,0);
        filterBox.add(filterProjectNr, 1,0);

        //filter project Type
        Label projectTypeLabel = new Label("Projekt Art");
        ComboBox<String> projectTypeFilter = new ComboBox<>();
        ObservableList<String> options = FXCollections.observableArrayList("Neubau", "Sanierung", "Umbau", "Anbau", "Ausbau");
        projectTypeFilter.setItems(options);
        projectTypeFilter.setPromptText("Filter nach Art");
        filterBox.add(projectTypeLabel,0,2);
        filterBox.add(projectTypeFilter,1,2);

        //filtere versionen (all/newest)
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

        sumFilter = new RangeFilter(
                "Bausumme",
                "Reset",
                swissLocale,
                project -> project.getData().getTotalCost(),
                controller::getMinTotalCost,
                controller::getMaxTotalCost
        );

        filterBox.add(sumFilter.getTitelLabel(),2,0);
        filterBox.add(sumFilter.getResetButton(),3,0);
        filterBox.add(sumFilter.getSlider(),2,1);
        GridPane.setColumnSpan(sumFilter.getSlider(),2);
        filterBox.add(sumFilter.getMinTextField(),2,2);
        filterBox.add(sumFilter.getMaxTextField(),3,2);

        apartmentNrFilter = new RangeFilter(
                "Wohnungen",
                "Reset",
                swissLocale,
                Project::getApartmentsNr,
                controller::getMinApartments,
                controller::getMaxApartments
        );

        filterBox.add(apartmentNrFilter.getTitelLabel(), 4, 0);
        filterBox.add(apartmentNrFilter.getResetButton(), 5, 0);
        filterBox.add(apartmentNrFilter.getSlider(),4,1);
        GridPane.setColumnSpan(apartmentNrFilter.getSlider(),2);
        filterBox.add(apartmentNrFilter.getMinTextField(),4,2);
        filterBox.add(apartmentNrFilter.getMaxTextField(),5,2);

        //listener for filter
        sumFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, projectTypeFilter));
        apartmentNrFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, projectTypeFilter));
        filterProjectNr.setOnAction(e -> updateFilter(filterProjectNr, versionFilter, projectTypeFilter));
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
            updateFilter(filterProjectNr, versionFilter, projectTypeFilter);
        });

        projectTypeFilter.setOnAction(e -> updateFilter(filterProjectNr, versionFilter, projectTypeFilter));

        outerPane.getChildren().addAll(filterBox);
        return outerPane;
    }

    /**
     * used to update the projects
     * @param filterProjectNr
     * @param versionFilter
     * @param projectTypeFilter
     */
    private void updateFilter(TextField filterProjectNr,
                              ToggleSwitch versionFilter,
                              ComboBox<String> projectTypeFilter) {

        if (filteredProjects == null) return;

        //predicate methode von claude.ai
        filteredProjects.setPredicate(project -> {
            String selectedProjectType = projectTypeFilter.getSelectionModel().getSelectedItem();

            if (isEmpty(filterProjectNr) && selectedProjectType == null && !versionFilter.isSelected()) {
                return sumFilter.getPredicate().test(project) && apartmentNrFilter.getPredicate().test(project);
            }

            boolean matchProjectNrFilter = isEmpty(filterProjectNr) || String.valueOf(project.getProjectNr()).contains(filterProjectNr.getText().toLowerCase());

            boolean matchVersionFilter = true;
            if( versionFilter.isSelected() && data.containsKey((project.getProjectNr()*100) + (project.getVersion()+1))){
                matchVersionFilter = false;
            }

            boolean matchProjectType = selectedProjectType == null || String.valueOf(project.getConstructionType()).contains(selectedProjectType);
            boolean matchSumFilter = sumFilter.getPredicate().test(project);
            boolean matchApartmentFilter = apartmentNrFilter.getPredicate().test(project);
            return matchProjectNrFilter && matchVersionFilter && matchProjectType && matchSumFilter && matchApartmentFilter;
        });
    }

    private VBox displayRowLabels(){
        rowLabels = new VBox();

        projectList.addListener(new ListChangeListener<Project>() {
            @Override
            public void onChanged(Change<? extends Project> c) {
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
                        projectLabelSpez("Sepzielles"),
                        projectLabel("")//,
                );
                HBox noScroll = new HBox();
                noScroll.setMinHeight(15);

                for (int i = 0; i < projectList.getFirst().getCalculations().size(); i++) {
                    rowLabels.getChildren().add(projectLabel(projectList.getFirst().getCalculations().get(i).getName()));
                }
                rowLabels.getChildren().add(noScroll);
            }
        });

        return rowLabels;
    }

    //methode von claude.ai generiert
    private void updateVisibleCells() {
        if (rightScroll == null || projectsContainer == null) return;

        double scrollPaneWidth = rightScroll.getWidth() - (rightScroll.getVbarPolicy() != ScrollPane.ScrollBarPolicy.NEVER ? SCROLLBAR_WIDTH : 0); // Scrollbar-Breite abziehen
        double totalWidth = filteredProjects.size() * (CELL_WIDTH + CELL_GAP);

        // hvalue between 0 und 1 → calculate to Pixel
        double scrollOffset = rightScroll.getHvalue() * Math.max(0, totalWidth - scrollPaneWidth);

        // Welche Indizes sind sichtbar?
        int firstVisible = Math.max(0, (int) (scrollOffset / (CELL_WIDTH + CELL_GAP)));
        int lastVisible = Math.min(
                filteredProjects.size() - 1,
                (int) ((scrollOffset + scrollPaneWidth) / (CELL_WIDTH + CELL_GAP))
        );

        projectsContainer.getChildren().clear();

        //left spacer: moves the visible cells to the correct position
        if (firstVisible > 0) {
            Pane leftSpacer = new Pane();
            leftSpacer.setPrefWidth(firstVisible * (CELL_WIDTH + CELL_GAP));
            leftSpacer.setMinWidth(leftSpacer.getPrefWidth());
            projectsContainer.getChildren().add(leftSpacer);
        }

        //render only visible cells, fetch them from cache
        for (int i = firstVisible; i <= lastVisible; i++) {
            VBox cell = cellCache.get(i);
            if (cell == null) {
                cell = createProjectCell(filteredProjects.get(i));
                cellCache.put(i, cell);
                evictCacheIfNeeded();
            }
            projectsContainer.getChildren().add(cell);
        }
    }

    /**
     * creates the visual elements for a project
     * @param project the project to get rendered
     * @return project as VBox
     */
    private VBox createProjectCell(Project project) {
        VBox projectBox = new VBox();
        projectBox.setMinWidth(CELL_WIDTH);
        projectBox.setPrefWidth(CELL_WIDTH);
        projectBox.setMaxWidth(CELL_WIDTH);
        projectBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");

        //Modify-Project-Button, opens a new ProjectInputWindow
        Button modifyProjectButton = new Button("⟲");
        modifyProjectButton.setOnAction(event -> {
            ProjectInputWindow modify = new ProjectInputWindow(controller, project);
            try {
                Stage newStage = new Stage();
                modify.start(newStage);
                if (modify.getAddButtonStatus()) {
                    refreshProjectList();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //Delete-Project-Button, delete the project from the database
        Button deletProjectButton = new Button("\uD83D\uDDD1");
        deletProjectButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Projekt löschen");
            alert.setHeaderText("Bitte bestätigen:");
            alert.setContentText("Löschen von Projekt Nr. " + project.getProjectNr() + " Version " + project.getVersion() + "?");
            Optional<ButtonType> buttonType = alert.showAndWait();
            if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                String message = controller.deleteProject(project.getProjectNr(), project.getVersion());
                refreshProjectList();

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Projekt löschen");
                confirmAlert.setHeaderText("Rückmeldung");
                confirmAlert.setContentText(message);
                confirmAlert.showAndWait();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox projectHead = new HBox(10);
        projectHead.setPadding(new Insets(0, 10, 0, 0));
        projectHead.setMinHeight(30);
        projectHead.setMaxHeight(30);
        projectHead.setPrefHeight(30);
        projectHead.setAlignment(Pos.CENTER_LEFT);
        projectHead.getChildren().addAll(projectLabelNoBorder(String.valueOf(project.getProjectNr())), spacer, modifyProjectButton, deletProjectButton);
        projectHead.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        projectBox.getChildren().addAll(
                projectHead,
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
                projectLabel(project.getHnf()),
                projectLabel(project.getGf()),
                projectLabel(project.getVolumeUnderground() + project.getVolumeAboveGround()),
                projectLabel(project.getFacadeType()),
                projectLabel(project.getWindowType()),
                projectLabel(project.getRoofType()),
                projectLabel(project.getHeatingType()),
                projectLabel(project.getCoolingType()),
                projectLabel(project.getVentilationTypeApartments()),
                projectLabel(project.getVentilationTypeUg()),
                projectLabel(project.getCoNo()),
                projectLabelSpez(project.getSpecial()),
                projectLabel("")
        );

        for (Calculation calc : project.getCalculations().values()) {
            projectBox.getChildren().add(projectLabelRight(calc.getCalculation()));
        }

        return projectBox;
    }

    /**
     * Wenn der Cache zu groß wird, werden die ältesten Einträge entfernt
     */
    private void evictCacheIfNeeded() {
        while (cellCache.size() > MAX_CACHE_SIZE) {
            Iterator<Integer> it = cellCache.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    /**
     * retrieves the projects from the backend (database) and updates the lists for the UI
     */
    public void refreshProjectList() {
        data.clear();
        data.putAll(controller.getProjects());
        controller.calculate();
        projectList.setAll(data.values());
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
        Label averageRatioUG = new Label("⌀ Verhältnis UG/OG:");
        Label averageRatioUGValue = new Label(String.format("%.2f", controller.getAverageRatioUG()));
        Label averageWindowRatio = new Label("⌀ Anteil Fenster/Fassade:");
        Label averageWindowRatioValue = new Label(String.valueOf(controller.getAverageWindowRatio()) + " %");

        grid.add(statisticsTitel,0,0);
        grid.add(nrOfProjects,0,1);
        grid.add(nrOfProjectsValue, 1,1);
        grid.add(averageRatioUG,0,2);
        grid.add(averageRatioUGValue,1,2);
        grid.add(averageWindowRatio,0,3);
        grid.add(averageWindowRatioValue,1,3);

        projectList.addListener(new ListChangeListener<Project>() {
            @Override
            public void onChanged(Change<? extends Project> c) {
                nrOfProjectsValue.setText(String.valueOf(projectList.size()));
                averageRatioUGValue.setText(String.format("%.2f", controller.getAverageRatioUG()));
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
        return label;
    }

    private TextFlow projectLabelSpez(String text) {
        Text newText = new Text(text);
        TextFlow label = new TextFlow(newText);
        label.setPadding(new Insets(5,10,0,10));
        label.setMinHeight(60);
        label.setMaxHeight(60);
        label.setPrefHeight(60);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private Label projectLabelRight(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(0,0,0,10));
        label.setMinHeight(30);
        label.setMaxHeight(30);
        label.setPrefHeight(30);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
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

    private Label projectLabelNoBorder(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(0,0,0,10));
        label.setMinHeight(30);
        label.setMaxHeight(30);
        label.setPrefHeight(30);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}