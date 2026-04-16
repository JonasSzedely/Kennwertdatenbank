package view;


import excel.CreateExcel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import model.Controller;

import java.io.IOException;
import java.util.Optional;

class TopPane {
    private final Controller controller;

    TopPane(Controller controller) {
        this.controller = controller;
    }

    /**
     * creates the top pane
     *
     * @return HBox
     */
    HBox get() {
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

        HBox filters = new Filters(controller).get();
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
                        //DBConfig.loadProperties();
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

    private void noDBConnection() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Keine Datenbankverbindung");
        error.setContentText("Die Funktion kann nur bei aktiver Datenbankverbindung verwendet werden.");
        error.show();
    }
}
