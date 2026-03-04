package userinterface;

import database.DBConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Duration;
import kennwertdatenbank.Controller;
import kennwertdatenbank.Project;
import org.controlsfx.control.ToggleSwitch;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

public class TopPane {
    private final Controller controller;
    private final int TOOL_TIP_TIME = 200;
    private RangeFilter sumFilter;
    private RangeFilter apartmentNrFilter;
    private RangeFilter volumeFilter;
    private final Locale swissLocale = Locale.of("de", "CH");


    public TopPane(Controller controller){
        this.controller = controller;
    }

    /**
     * creates the top pane
     * @return HBox
     */
    public HBox get() {
        HBox topPane = new HBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.setPadding(new Insets(20));
        topPane.setMinHeight(150);
        topPane.setStyle("-fx-background-color: #052048; -fx-border-color: lightgray; -fx-border-width: 1 1 0 1;");
        topPane.setId("top-pane");

        HBox topLeft = new HBox();
        topLeft.setAlignment(Pos.CENTER);
        topLeft.setPadding(new Insets(20, 20, 20, 20));
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

        GridPane buttonBox = new GridPane(20, 20);
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

        buttonBox.add(pdfButton, 0, 0);
        buttonBox.add(exportButton, 1, 0);
        buttonBox.add(addProjectButton, 0, 1);
        buttonBox.add(optionsButton, 1, 1);

        //Event-Handler for pdfButton
        pdfButton.setOnAction(event -> {
            if (!controller.isDatabaseAvailable()) {
                noDBConnection();
                return;
            }
            CreatePDF newPDF = new CreatePDF(ProjectList.getSortedProjects());
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
                CreateExcel newExcel = new CreateExcel(ProjectList.getProjectList(), path);
                try {
                    newExcel.export();

                    Label label = new Label("Daten erfolgreich exportiert.");

                    HBox content = new HBox(label);
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
                if (addProject.getAddButtonStatus()) {
                    ProjectList.refreshProjectList();
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

            if (result.isEmpty()) {
                return;
            }

            if (result.get().equals("IMAG")) {
                Settings options = new Settings(controller);
                try {
                    Stage newStage = StageFactory.createStage("Einstellungen");
                    options.start(newStage);
                    if (options.isSetButtonUsed()) {
                        DBConfig.loadProperties();
                        if (controller.initializeDatabase()) {
                            ProjectList.refreshProjectList();
                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Datenbankverbindung");
                            success.setHeaderText("Verbindung erfolgreich");
                            success.setContentText("Die Datenbankverbindung wurde erfolgreich wiederhergestellt.");
                            success.show();
                        } else {
                            ProjectList.refreshProjectList();
                            DatabaseWarning.show();
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
            if (clicked.isPresent() && clicked.get() == ButtonType.OK) {
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
        TextField filterProjectNr = new TextField();
        filterProjectNr.setPromptText("Projektnummer");
        filterBox.add(filterProjectNr, 0,0);
        filterProjectNr.setTooltip(new Tooltip("Nach Projektnummer filtern"));
        filterProjectNr.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        //filter project type
        ComboBox<String> projectTypeFilter = new ComboBox<>();
        ObservableList<String> options = FXCollections.observableArrayList("Alle Gebäudenutzer", "Miete", "Stockwerkeigentum");
        projectTypeFilter.setItems(options);
        projectTypeFilter.setPromptText("Gebäudenutzer");
        filterBox.add(projectTypeFilter,0,1);
        projectTypeFilter.setTooltip(new Tooltip("Nach Gebäudenutzer filtern"));
        projectTypeFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        //filter construction Type
        ComboBox<String> constructionTypeFilter = new ComboBox<>();
        ObservableList<String> optionsConstructionType = FXCollections.observableArrayList("Alle Bauvorhaben", "Neubau", "Sanierung", "Umbau", "Anbau", "Ausbau");
        constructionTypeFilter.setItems(optionsConstructionType);
        constructionTypeFilter.setPromptText("Bauvorhaben");
        filterBox.add(constructionTypeFilter,0,2);
        constructionTypeFilter.setTooltip(new Tooltip("Nach Bauvorhaben Art filtern"));
        constructionTypeFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        Separator verticalLine1 = new Separator(Orientation.VERTICAL);
        filterBox.add(verticalLine1, 2, 0);
        GridPane.setRowSpan(verticalLine1, 3);
        GridPane.setFillHeight(verticalLine1, true);

        //filter versions (all/newest)
        ToggleSwitch versionFilter = new ToggleSwitch();
        versionFilter.setAlignment(Pos.CENTER_RIGHT);
        versionFilter.setMinWidth(99);
        versionFilter.setPrefWidth(99);
        versionFilter.setMaxWidth(99);
        versionFilter.setSelected(false);
        versionFilter.setText("vollständig");
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

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change ->  {
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
        ProjectList.setPredicate(project -> {
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
            boolean matchVersionFilter = !versionFilter.isSelected() || ProjectList.getTreeMap().subMap(currentKey + 1, baseKey + 100).isEmpty();

            return matchProjectNrFilter && matchVersionFilter && matchVolumeFilter && matchConstructionType && matchProjectType && matchSumFilter && matchApartmentFilter;
        });
    }

    private void noDBConnection(){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Keine Datenbankverbindung");
        error.setContentText("Die Funktion kann nur bei aktiver Datenbankverbindung verwendet werden.");
        error.show();
    }

    private boolean isEmpty(TextField textField) {
        return textField.getText() == null || textField.getText().trim().isEmpty();
    }
}