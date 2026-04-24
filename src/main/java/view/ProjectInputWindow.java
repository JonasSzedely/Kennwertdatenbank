package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Controller;
import model.ProjectData;
import model.Project;
import model.ProjectValues;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;

class ProjectInputWindow extends Application {

    private final Controller controller;
    private final Type type;
    private Project project;
    private Button addButton;
    private boolean addButtonUsed = false;
    private EnumMap<ProjectValues, Form> forms;
    private ArrayList<FormListener> formListeners;
    private Form dataPathForm;
    private FormListener dataPathListener;
    private Stage stage;

    // VERSION is managed internally and never shown in the form
    private static final ProjectValues[] SKIPPED = {ProjectValues.VERSION};

    public ProjectInputWindow(Controller controller, Type type) {
        this.controller = controller;
        this.type = type;
    }

    public ProjectInputWindow(Controller controller, Project project, Type type) {
        this.controller = controller;
        this.project = project;
        this.type = type;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Projekt Formular");

        VBox outerPane = new VBox(10);
        outerPane.setPadding(new Insets(20));
        outerPane.setAlignment(Pos.CENTER);
        outerPane.setStyle("-fx-background-color: white");

        Label title = new Label();
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        switch (type) {
            case NEW -> title.setText("Neues Projekt hinzufügen");
            case MODIFY -> title.setText("Projekt Nr. " + project.get(ProjectValues.PROJECT_NR)
                    + " Version " + project.get(ProjectValues.VERSION) + " bearbeiten");
            case NEXT -> title.setText("Neue Version von Projekt Nr. "
                    + project.get(ProjectValues.PROJECT_NR) + " hinzufügen");
        }

        GridPane gridPane = new GridPane(10, 10);
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(150), new ColumnConstraints(200), new ColumnConstraints(150),
                new ColumnConstraints(150), new ColumnConstraints(200), new ColumnConstraints(150)
        );

        forms = new EnumMap<>(ProjectValues.class);
        formListeners = new ArrayList<>();

        // Build forms dynamically from enum
        for (ProjectValues field : ProjectValues.values()) {
            if (isSkipped(field)) continue;

            if (field.isDropdown()) {
                forms.put(field, new DropdownForm(field.getLabel(), field.getOptions(), "Bitte auswählen!"));
                formListeners.add(new FormListener((DropdownForm) forms.get(field)));
            } else if (field.getType() == Integer.class) {
                forms.put(field, new InputForm(field.getLabel(), "", "Keine gültige Zahl!"));
                formListeners.add(new FormListener((InputForm) forms.get(field), field.getMin(), field.getMax()));
            } else {
                forms.put(field, new InputForm(field.getLabel(), "", "Bitte Eingabe machen!"));
                formListeners.add(new FormListener((InputForm) forms.get(field), field.getMax()));
            }
        }

        // DataPath is a UI-only field (not in enum), placed full-width at the bottom
        dataPathForm = new InputForm("BKP Dateipfad", "C:\\Benutzer\\Name\\Downloads\\kv.csv", "Kein gültiger Pfad!");
        dataPathListener = new FormListener((InputForm) dataPathForm, 260);

        // Place forms in grid: left half then right half
        ProjectValues[] keys = forms.keySet().toArray(new ProjectValues[0]);
        int half = (keys.length + 1) / 2;

        for (int i = 0; i < keys.length; i++) {
            ProjectValues field = keys[i];
            Form form = forms.get(field);
            int col = (i < half) ? 0 : 3;
            int row = (i < half) ? i : i - half;

            gridPane.add(form.getLabel(), col, row);
            gridPane.add(form.getInputField(), col + 1, row);
            gridPane.add(form.getInvalidLabel(), col + 2, row);
        }

        // DataPath at the bottom, spanning both columns
        int dataRow = half + 1;
        gridPane.add(dataPathForm.getLabel(), 0, dataRow);
        gridPane.add(dataPathForm.getInputField(), 1, dataRow);
        GridPane.setColumnSpan(dataPathForm.getInputField(), 4);
        gridPane.add(dataPathForm.getInvalidLabel(), 5, dataRow);

        // Configure button and type-specific behaviour
        addButton = new Button();

        switch (type) {
            case NEW -> {
                addButton.setText("Projekt hinzufügen");
                addButton.setOnAction(event -> {
                    if (validate()) {
                        confirmationWindow("Projekt hinzugefügt", addProject());
                    }
                });
            }
            case MODIFY -> {
                addButton.setText("Projekt anpassen");
                fillFields();
                forms.get(ProjectValues.PROJECT_NR).getInputField().setDisable(true);
                dataPathForm.setInputFieldText("Projektkosten können nicht überschrieben werden. Bitte neue Version anlegen.");
                dataPathForm.getInputField().setDisable(true);
                addButton.setOnAction(event -> {
                    if (validate()) {
                        confirmationWindow("Projekt angepasst", modifyProject());
                    }
                });
            }
            case NEXT -> {
                addButton.setText("Neue Version hinzufügen");
                fillFields();
                forms.get(ProjectValues.PROJECT_NR).getInputField().setDisable(true);
                addButton.setOnAction(event -> {
                    if (validate()) {
                        confirmationWindow("Neue Version von Projekt Nr. "
                                + project.get(ProjectValues.PROJECT_NR) + " hinzugefügt", addProject());
                    }
                });
            }
        }

        addButton.prefWidthProperty().bind(gridPane.widthProperty());
        gridPane.add(addButton, 0, dataRow + 1);
        GridPane.setColumnSpan(addButton, 5);

        outerPane.getChildren().addAll(title, gridPane);
        Scene scene = new Scene(outerPane);

        URL cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS-Datei nicht gefunden!");
        }

        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(600);
        stage.showAndWait();
    }

    private void fillFields() {
        for (ProjectValues field : forms.keySet()) {
            Object value = project.get(field);
            if (value != null) {
                forms.get(field).setInputFieldText(String.valueOf(value));
            }
        }
    }

    private String addProject() {
        Project newProject = new Project();
        for (ProjectValues field : forms.keySet()) {
            setFieldOnProject(newProject, field, forms.get(field).getInput());
        }
        newProject.set(ProjectValues.VERSION, 1);
        ProjectData data = new ProjectData();
        data.set(dataPathForm.getInput().replaceAll("\"", "").trim());
        newProject.setData(data);
        return controller.addProject(newProject);
    }

    private String modifyProject() {
        for (ProjectValues field : forms.keySet()) {
            if (forms.get(field).getInputField().isDisabled()) continue;
            setFieldOnProject(project, field, forms.get(field).getInput());
        }
        return controller.modifyProject(project);
    }

    private void setFieldOnProject(Project target, ProjectValues field, String input) {
        if (field.getType() == Integer.class) {
            target.set(field, Integer.parseInt(input));
        } else {
            target.set(field, input);
        }
    }

    private boolean validate() {
        boolean allValid = true;
        for (FormListener listener : formListeners) {
            listener.validate();
            boolean valid = listener.isValid();
            listener.setInvalidLabel(!valid);
            if (!valid) allValid = false;
        }
        // DataPath is only required when not in MODIFY mode (cost data cannot be changed)
        if (type != Type.MODIFY) {
            dataPathListener.validate();
            boolean valid = dataPathListener.isValid();
            dataPathListener.setInvalidLabel(!valid);
            if (!valid) allValid = false;
        }
        return allValid;
    }

    private boolean isSkipped(ProjectValues field) {
        for (ProjectValues skipped : SKIPPED) {
            if (field == skipped) return true;
        }
        return false;
    }

    private void confirmationWindow(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Rückmeldung");
        alert.setContentText(message + "\nDrücken Sie OK um den Vorgang zu beenden.");
        alert.showAndWait();
        addButtonUsed = true;
        stage.close();
    }

    public boolean getAddButtonStatus() {
        return addButtonUsed;
    }

    enum Type {
        NEW,
        MODIFY,
        NEXT
    }
}