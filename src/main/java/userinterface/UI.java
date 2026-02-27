package userinterface;
import database.DBConfig;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
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

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class UI extends Application {
    private Controller controller;
    private final TreeMap<Integer, Project> treeMap = new TreeMap<>();
    private final ObservableMap<Integer, Project> data = FXCollections.observableMap(treeMap);
    private final ObservableList<Project> projectList = FXCollections.observableArrayList(data.values());
    private final FilteredList<Project> filteredProjects = new FilteredList<>(projectList);
    private final SortedList<Project> sortedProjects = new SortedList<>(filteredProjects);
    private final LinkedHashMap<Integer, VBox> cellCache = new LinkedHashMap<>();
    private final Locale swissLocale = Locale.of("de", "CH");
    private RangeFilter sumFilter;
    private RangeFilter apartmentNrFilter;
    private RangeFilter volumeFilter;
    private ScrollPane rightScroll;
    private HBox projectsContainer;
    private VBox rowLabels;
    private final double CELL_WIDTH = 150;
    private final double CELL_HEIGHT = 30;
    private final double CELL_GAP = 0;
    private static final int MAX_CACHE_SIZE = 50;
    private final int TOOL_TIP_TIME = 200;
    private final double BUTTON_SIZE = CELL_HEIGHT * 0.66;
    private final int SCROLLBAR_WIDTH = 15;

    @Override
    public void start(Stage primaryStage) {

        StageFactory.setIcon(primaryStage);
        controller = new Controller();

        VBox outerPane = new VBox();
        outerPane.setPadding(new Insets(5));
        outerPane.setAlignment(Pos.CENTER);

        HBox topPane = topPane();
        HBox middlePane = middlePane();
        HBox bottomPane = bottomPane();

        outerPane.getChildren().addAll(topPane, middlePane, bottomPane);

        Scene scene = new Scene(outerPane, 1000,600);

        URL cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS-Datei nicht gefunden!");
        }
        primaryStage.setScene(scene);

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
                        "Das Programm läuft im Offline-Modus mit eingeschränkter Funktionalität.\n" +
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
        topPane.setPadding(new Insets(20));
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

        GridPane buttonBox = new GridPane(20,20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getColumnConstraints().add(new ColumnConstraints(100));

        Button pdfButton = new Button("PDF erstellen");
        pdfButton.setMaxWidth(Double.MAX_VALUE);
        Button exportButton = new Button("Exportieren");
        exportButton.setMaxWidth(Double.MAX_VALUE);
        Button optionsButton = new Button("Einstellungen");
        optionsButton.setMaxWidth(Double.MAX_VALUE);
        Button addProjectButton = new Button("Neues Projekt");
        addProjectButton.setMaxWidth(Double.MAX_VALUE);

        buttonBox.add(pdfButton,0,0);
        buttonBox.add(exportButton,1,0);
        buttonBox.add(addProjectButton,0,1);
        buttonBox.add(optionsButton,1,1);

        //Event-Handler for pdfButton
        pdfButton.setOnAction(event ->{
            if (!controller.isDatabaseAvailable()) {
                noDBConnection();
                return;
            }
            CreatePDF newPDF = new CreatePDF(sortedProjects);
            try {
                Stage newStage = StageFactory.createStage("PDF erstellen");
                newPDF.start(newStage);
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Fehler beim Öffnen des PDF-Fensters");
                error.setContentText("Fehler: " + e.getMessage());
                error.show();
                throw new RuntimeException(e);
            }
        });

        //Event-Handler for exportButton
        exportButton.setOnAction(event -> {
            if (!controller.isDatabaseAvailable()) {
                noDBConnection();
                return;
            }
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Daten exportieren");
            dialog.setHeaderText("Daten exportieren");
            dialog.setContentText("Zielordner für Expor eingeben:");
            dialog.getEditor().setPromptText("C:\\Users\\Name\\Downloads");
            Platform.runLater(() -> dialog.getDialogPane().requestFocus()); //von claude.ai (needed because focus is set after the dialog is rendered
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String path = dialog.getEditor().getText().replaceAll("\"", "");
                System.out.println(path);
                CreateExcel newExcel = new CreateExcel(projectList, path);
                try {
                    newExcel.export();

                    Hyperlink textLink = new Hyperlink("Ordner öffnen.");
                    textLink.setOnAction(newEvent -> getHostServices().showDocument(path));

                    Label label = new Label("Daten erfolgreich exportiert. ");

                    HBox content = new HBox(label, textLink);
                    content.setAlignment(Pos.CENTER_LEFT);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Daten exportieren");
                    alert.setHeaderText(null);
                    alert.getDialogPane().setContent(content);
                    alert.showAndWait();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //Event-Handler for addProjectButton
        addProjectButton.setOnAction(event -> {
            if (!controller.isDatabaseAvailable()) {
                noDBConnection();
                return;
            }
            ProjectInputWindow addProject = new ProjectInputWindow(controller, ProjectInputWindow.Type.NEW);
            try {
                Stage newStage = StageFactory.createStage("Neues Projekt");
                addProject.start(newStage);
                if(addProject.getAddButtonStatus()) {
                    refreshProjectList();
                }
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Fehler beim Öffnen des Projekt-Fensters");
                error.setContentText("Fehler: " + e.getMessage());
                error.show();
                throw new RuntimeException(e);
            }
        });

        //Event-Handler for optionsButton
        optionsButton.setOnAction(event -> {
            TextInputDialog settingsPW = new TextInputDialog();
            settingsPW.setTitle("Einstellung");
            settingsPW.setHeaderText("Bitte Passwort eingeben");
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

            if(result.isEmpty()){
                return;
            }

            if (result.get().equals("IMAG")){
                Settings options = new Settings(controller);
                try {
                    Stage newStage = StageFactory.createStage("Einstellungen");
                    options.start(newStage);
                    if(options.isSetButtonUsed()) {
                        DBConfig.loadProperties();
                        if (controller.initializeDatabase()) {
                            refreshProjectList();
                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Datenbankverbindung");
                            success.setHeaderText("Verbindung erfolgreich");
                            success.setContentText("Die Datenbankverbindung wurde erfolgreich wiederhergestellt.");
                            success.show();
                        } else {
                            refreshProjectList();
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
            if (clicked.isPresent() && clicked.get() == ButtonType.OK){
                optionsButton.fire();
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
        updateSorting();

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
        rightScroll.hvalueProperty().addListener(observable -> updateVisibleCells());

        //update cells while scrolling left (code from claude.ai)
        rightScroll.widthProperty().addListener(observable -> updateVisibleCells());

        //update cells when projects get updated (code from claude.ai)
        sortedProjects.addListener((ListChangeListener<Project>) change -> {
            double totalWidth = sortedProjects.size() * (CELL_WIDTH + CELL_GAP);
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
                new KeyFrame(Duration.seconds(1), event -> dateTimeLabel.setText(ZonedDateTime.now().format(formatter)))
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
        filterBox.getColumnConstraints().addAll(new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(2),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(2),new ColumnConstraints(100),new ColumnConstraints(100),new ColumnConstraints(2),new ColumnConstraints(100),new ColumnConstraints(100));

        //filter project number
        //Label projectNrLabel = new Label("Projektnummer");
        TextField filterProjectNr = new TextField();
        filterProjectNr.setPromptText("Projektnummer");
        //filterBox.add(projectNrLabel, 0,0);
        filterBox.add(filterProjectNr, 0,0);
        filterProjectNr.setTooltip(new Tooltip("Nach Projektnummer filtern"));
        filterProjectNr.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        //filter project type
        //Label projectTypeLabel = new Label("Gebäudenutzung");
        ComboBox<String> projectTypeFilter = new ComboBox<>();
        ObservableList<String> options = FXCollections.observableArrayList("Alle Gebäudenutzer", "Miete", "Stockwerkeigentum");
        projectTypeFilter.setItems(options);
        projectTypeFilter.setPromptText("Gebäudenutzer");
        //filterBox.add(projectTypeLabel,0,2);
        filterBox.add(projectTypeFilter,0,1);
        projectTypeFilter.setTooltip(new Tooltip("Nach Gebäudenutzer filtern"));
        projectTypeFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        //filter construction Type
        //Label constructionTypeLabel = new Label("Bauvorhaben Art");
        ComboBox<String> constructionTypeFilter = new ComboBox<>();
        ObservableList<String> optionsConstructionType = FXCollections.observableArrayList("Alle Bauvorhaben", "Neubau", "Sanierung", "Umbau", "Anbau", "Ausbau");
        constructionTypeFilter.setItems(optionsConstructionType);
        constructionTypeFilter.setPromptText("Bauvorhaben");
        //filterBox.add(constructionTypeLabel,0,2);
        filterBox.add(constructionTypeFilter,0,2);
        constructionTypeFilter.setTooltip(new Tooltip("Nach Bauvorhaben Art filtern"));
        constructionTypeFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        Separator verticalLine1 = new Separator(Orientation.VERTICAL);
        filterBox.add(verticalLine1, 2, 0);
        GridPane.setRowSpan(verticalLine1, 3);
        GridPane.setFillHeight(verticalLine1, true);

        //filter versions (all/newest)
        //Label versionLabel = new Label("Zeige Versionen");
        ToggleSwitch versionFilter = new ToggleSwitch();
        versionFilter.setAlignment(Pos.CENTER_RIGHT);
        versionFilter.setMinWidth(99);
        versionFilter.setPrefWidth(99);
        versionFilter.setMaxWidth(99);
        versionFilter.setSelected(false);
        versionFilter.setText("vollständig");
        //filterBox.add(versionLabel, 0,1);
        filterBox.add(versionFilter, 1,0);
        versionFilter.setTooltip(new Tooltip("Nach Versionen filtern"));
        versionFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        sumFilter = new RangeFilter(
                "Bausumme",
                "Reset",
                swissLocale,
                project -> project.getData().getTotalCost(),
                controller::getMinTotalCost,
                controller::getMaxTotalCost
        );

        filterBox.add(sumFilter.getTitelLabel(),3,0);
        filterBox.add(sumFilter.getResetButton(),4,0);
        filterBox.add(sumFilter.getSlider(),3,1);
        GridPane.setColumnSpan(sumFilter.getSlider(),2);
        filterBox.add(sumFilter.getMinTextField(),3,2);
        filterBox.add(sumFilter.getMaxTextField(),4,2);

        Separator verticalLine2 = new Separator(Orientation.VERTICAL);
        filterBox.add(verticalLine2, 5, 0);
        GridPane.setRowSpan(verticalLine2, 3);
        GridPane.setFillHeight(verticalLine2, true);

        apartmentNrFilter = new RangeFilter(
                "Wohnungen",
                "Reset",
                swissLocale,
                Project::getApartmentsNr,
                controller::getMinApartments,
                controller::getMaxApartments
        );

        filterBox.add(apartmentNrFilter.getTitelLabel(), 6, 0);
        filterBox.add(apartmentNrFilter.getResetButton(), 7, 0);
        filterBox.add(apartmentNrFilter.getSlider(),6,1);
        GridPane.setColumnSpan(apartmentNrFilter.getSlider(),2);
        filterBox.add(apartmentNrFilter.getMinTextField(),6,2);
        filterBox.add(apartmentNrFilter.getMaxTextField(),7,2);

        Separator verticalLine3 = new Separator(Orientation.VERTICAL);
        filterBox.add(verticalLine3, 8, 0);
        GridPane.setRowSpan(verticalLine3, 3);
        GridPane.setFillHeight(verticalLine3, true);

        volumeFilter = new RangeFilter(
                "Volumen",
                "Reset",
                swissLocale,
                Project::getVolume,
                controller::getMinVolume,
                controller::getMaxVolume
        );

        filterBox.add(volumeFilter.getTitelLabel(), 9, 0);
        filterBox.add(volumeFilter.getResetButton(), 10, 0);
        filterBox.add(volumeFilter.getSlider(),9,1);
        GridPane.setColumnSpan(volumeFilter.getSlider(),2);
        filterBox.add(volumeFilter.getMinTextField(),9,2);
        filterBox.add(volumeFilter.getMaxTextField(),10,2);

        //listener for filter
        sumFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter));
        apartmentNrFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter,projectTypeFilter));
        volumeFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter,projectTypeFilter));

        filterProjectNr.setOnAction(event -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter,projectTypeFilter));

        projectList.addListener((ListChangeListener<Project>) change ->  {
                sumFilter.setRange();
                apartmentNrFilter.setRange();
                volumeFilter.setRange();
        });

        versionFilter.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                versionFilter.setText("neuste");
            } else {
                versionFilter.setText("vollständig");
            }
            updateFilter(filterProjectNr, versionFilter, constructionTypeFilter,projectTypeFilter);
        });

        constructionTypeFilter.setOnAction(event -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter,projectTypeFilter));

        projectTypeFilter.setOnAction(event -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter,projectTypeFilter));

        outerPane.getChildren().addAll(filterBox);
        return outerPane;
    }

    /**
     * used to update the projects
     * @param filterProjectNr input filter for project number
     * @param versionFilter input filter for version
     * @param constructionTypeFilter input filter for construction type
     * @param projectTypeFilter input filter for project type
     */
    private void updateFilter(TextField filterProjectNr,
                              ToggleSwitch versionFilter,
                              ComboBox<String> constructionTypeFilter,
                              ComboBox<String> projectTypeFilter) {

        //predicate methode von claude.ai
        filteredProjects.setPredicate(project -> {
            String selectedConstructionType = constructionTypeFilter.getSelectionModel().getSelectedItem();
            String selectedProjectType = projectTypeFilter.getSelectionModel().getSelectedItem();

            boolean matchSumFilter = sumFilter.getPredicate().test(project);
            boolean matchApartmentFilter = apartmentNrFilter.getPredicate().test(project);
            boolean matchVolumeFilter = volumeFilter.getPredicate().test(project);

            if (isEmpty(filterProjectNr) 
                    && (selectedConstructionType == null || selectedConstructionType.equals("Alle Bauvorhaben"))
                    && (selectedProjectType == null || selectedProjectType.equals("Alle Gebäudenutzer"))
                    && !versionFilter.isSelected())
            {
                return matchSumFilter && matchApartmentFilter && matchVolumeFilter;
            }

            boolean matchProjectNrFilter = isEmpty(filterProjectNr) || String.valueOf(project.getProjectNr()).contains(filterProjectNr.getText().toLowerCase());
            boolean matchConstructionType = selectedConstructionType == null || selectedConstructionType.equals("Alle Bauvorhaben") || String.valueOf(project.getConstructionType()).contains(selectedConstructionType);
            boolean matchProjectType = selectedProjectType == null || selectedProjectType.equals("Alle Gebäudenutzer") || String.valueOf(project.getPropertyType()).contains(selectedProjectType);


            int baseKey = project.getProjectNr() * 100;
            int currentKey = baseKey + project.getVersion();
            boolean matchVersionFilter = !versionFilter.isSelected() || treeMap.subMap(currentKey + 1, baseKey + 100).isEmpty();

            return matchProjectNrFilter && matchVersionFilter && matchVolumeFilter && matchConstructionType && matchProjectType && matchSumFilter && matchApartmentFilter;
        });
    }

    private VBox displayRowLabels(){
        rowLabels = new VBox();

        projectList.addListener((ListChangeListener<Project>) change -> {
                rowLabels.getChildren().clear();
                rowLabels.getChildren().addAll(
                        rowLabel("Projekt-Nr."),
                        rowLabel("Version"),
                        rowLabel("Adresse"),
                        rowLabel("PLZ"),
                        rowLabel("Ort"),
                        rowLabel("Bauherr"),
                        rowLabel("Gebäudenutzungen"),
                        rowLabel("Art des Bauvorhaben"),
                        rowLabel("Planstand"),
                        rowLabel("Gerechnete Phasen"),
                        rowLabel("Anzahl Wohnungen"),
                        //rowLabel()("Anzahl Nasszellen"),
                        rowLabel("HNF in m²"),
                        rowLabel("GF in m²"),
                        rowLabel("SIA m³"),
                        //rowLabel()("unten"),
                        //rowLabel()("oben"),
                        rowLabel("Fassaden-Typ"),
                        rowLabel("Fenster-Typ"),
                        rowLabel("Dach-Typ"),
                        rowLabel("Heizungs-Typ"),
                        rowLabel("Kühlungs-Typ"),
                        rowLabel("Lüftungs-Typ Whg."),
                        rowLabel("Lüftungs-Typ TG"),
                        rowLabel("CO/NO-Anlage"),
                        rowLabelSpez("Sepzielles"),
                        rowLabel("")//,
                );
                HBox noScroll = new HBox();
                noScroll.setMinHeight(15);

                for (int i = 0; i < projectList.getFirst().getCalculations().size(); i++) {
                    rowLabels.getChildren().add(rowLabel(projectList.getFirst().getCalculations().get(i).getName()));
                }
                rowLabels.getChildren().add(noScroll);
        });

        return rowLabels;
    }

    //methode von claude.ai generiert
    private void updateVisibleCells() {
        if (rightScroll == null || projectsContainer == null) return;

        double scrollPaneWidth = rightScroll.getWidth() - (rightScroll.getVbarPolicy() != ScrollPane.ScrollBarPolicy.NEVER ? SCROLLBAR_WIDTH : 0); // Scrollbar-Breite abziehen
        double totalWidth = sortedProjects.size() * (CELL_WIDTH + CELL_GAP);

        // hvalue between 0 und 1 → calculate to Pixel
        double scrollOffset = rightScroll.getHvalue() * Math.max(0, totalWidth - scrollPaneWidth);

        // Welche Indizes sind sichtbar?
        int firstVisible = Math.max(0, (int) (scrollOffset / (CELL_WIDTH + CELL_GAP)));
        int lastVisible = Math.min(
                sortedProjects.size() - 1,
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
                cell = createProjectCell(sortedProjects.get(i));
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
            ProjectInputWindow modify = new ProjectInputWindow(controller, project, ProjectInputWindow.Type.MODIFY);
            try {
                Stage newStage = StageFactory.createStage("Projekt bearbeiten");
                modify.start(newStage);
                if (modify.getAddButtonStatus()) {
                    refreshProjectList();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        modifyProjectButton.setTooltip(new Tooltip("Projekt bearbeiten"));
        modifyProjectButton.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        modifyProjectButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        modifyProjectButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        modifyProjectButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        modifyProjectButton.setPadding(new Insets(0));

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
        deletProjectButton.setTooltip(new Tooltip("Projekt löschen"));
        deletProjectButton.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        deletProjectButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        deletProjectButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        deletProjectButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        deletProjectButton.setPadding(new Insets(0));

        Region spacerHead = new Region();
        HBox.setHgrow(spacerHead, Priority.ALWAYS);

        HBox projectHead = new HBox(10);
        projectHead.setPadding(new Insets(0, 10, 0, 0));
        projectHead.setMinHeight(CELL_HEIGHT);
        projectHead.setMaxHeight(CELL_HEIGHT);
        projectHead.setPrefHeight(CELL_HEIGHT);
        projectHead.setAlignment(Pos.CENTER_LEFT);
        projectHead.getChildren().addAll(projectLabelNoBorder(String.valueOf(project.getProjectNr())), spacerHead, modifyProjectButton, deletProjectButton);
        projectHead.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        //adding next version of Project
        Button nextVersionButton = new Button("+");
        nextVersionButton.setPadding(new Insets(0));
        nextVersionButton.setOnAction(event -> {
            ProjectInputWindow nextVersion = new ProjectInputWindow(controller, project, ProjectInputWindow.Type.NEXT);
            try {
                Stage newStage = StageFactory.createStage("Neue Version");
                nextVersion.start(newStage);
                if (nextVersion.getAddButtonStatus()) {
                    refreshProjectList();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        nextVersionButton.setTooltip(new Tooltip("Neue Version erstellen"));
        nextVersionButton.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        nextVersionButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        nextVersionButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        nextVersionButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

        //focus checkbox, sets the project to be focused and sorted first
        CheckBox focus = new CheckBox();
        focus.setTooltip(new Tooltip("Projekt als erstes anzeigen"));
        focus.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        focus.selectedProperty().bindBidirectional(project.pinnedProperty()); //code from claude.ai
        focus.setPadding(new Insets(0));

        HBox focusContainer = new HBox(focus);
        focusContainer.setAlignment(Pos.CENTER);
        focusContainer.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        focusContainer.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        focusContainer.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

        // Update sorting when the pinned property changes (from claude.ai)
        project.pinnedProperty().addListener(observable -> {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> updateSorting());
            pause.play();
        });

        Region spacerVersion = new Region();
        HBox.setHgrow(spacerVersion, Priority.ALWAYS);

        HBox projectVersion = new HBox(10);
        projectVersion.setPadding(new Insets(0, 10, 0, 0));
        projectVersion.setMinHeight(CELL_HEIGHT);
        projectVersion.setMaxHeight(CELL_HEIGHT);
        projectVersion.setPrefHeight(CELL_HEIGHT);
        projectVersion.setAlignment(Pos.CENTER_LEFT);
        projectVersion.getChildren().addAll(projectLabelNoBorder(String.valueOf(project.getVersion())), spacerVersion, nextVersionButton, focusContainer);
        projectVersion.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        projectBox.getChildren().addAll(
                projectHead,
                projectVersion,
                projectLabel(project.getAddress()),
                projectLabel(String.valueOf(project.getPlz())),
                projectLabel(project.getLocation()),
                projectLabel(project.getOwner()),
                projectLabel(project.getPropertyType()),
                projectLabel(project.getConstructionType()),
                projectLabel(project.getDocumentPhase()),
                projectLabel(project.getCalculationPhase() + " - 5"),
                projectLabel(project.getApartmentsNr()),
                projectLabel(project.getHnf()),
                projectLabel(project.getGf()),
                projectLabel(project.getVolume()),
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
            projectBox.getChildren().add(projectLabel(calc.getCalculation()));
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

    /**
     * sorts the project list (from claude.ai)
     */
    private void updateSorting() {
        sortedProjects.setComparator(
                Comparator.comparing(Project::isPinned).reversed() // true first
                        .thenComparing(Project::getProjectNr) // project number
                        .thenComparing(Project::getVersion) // version
        );
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

        projectList.addListener((ListChangeListener<Project>) change ->  {
                nrOfProjectsValue.setText(String.valueOf(projectList.size()));
                averageRatioUGValue.setText(String.format("%.2f", controller.getAverageRatioUG()));
        });

        outerPane.getChildren().add(grid);
        return outerPane;
    }

    private boolean isEmpty(TextField textField) {
        return textField.getText() == null || textField.getText().trim().isEmpty();
    }

    private Label rowLabel(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(0,0,0,10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }
    private TextFlow rowLabelSpez(String text) {
        Text newText = new Text(text);
        TextFlow label = new TextFlow(newText);
        label.setPadding(new Insets(5,10,0,10));
        label.setMinHeight(CELL_HEIGHT*2);
        label.setMaxHeight(CELL_HEIGHT*2);
        label.setPrefHeight(CELL_HEIGHT*2);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private Label projectLabel(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(5,10,0,10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private TextFlow projectLabelSpez(String text) {
        Text newText = new Text(text);
        TextFlow label = new TextFlow(newText);
        label.setPadding(new Insets(5,10,0,10));
        label.setMinHeight(CELL_HEIGHT*2);
        label.setMaxHeight(CELL_HEIGHT*2);
        label.setPrefHeight(CELL_HEIGHT*2);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.RIGHT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private Label projectLabel(int number) {
        Label label = new Label(String.format(swissLocale, "%,d",number));
        label.setPadding(new Insets(5,10,0,10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        if(number == 0){
            label.setText("");
        }
        return label;
    }

    private Label projectLabelNoBorder(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(5,10,0,10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }

    private void noDBConnection(){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Keine Datenbankverbindung");
        error.setContentText("Die Funktion kann nur bei aktiver Datenbankverbindung verwendet werden.");
        error.show();
    }
}