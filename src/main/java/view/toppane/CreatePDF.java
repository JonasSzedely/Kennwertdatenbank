package view;

import javafx.application.Application;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Project;
import pdf.PDFCreator;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * PDF creation interface for generating project reports.
 * Provides a GUI for selecting output location and title,
 * then creates a PDF document with project data in table format.
 */
class CreatePDF extends Application {

    /**
     * Sorted list of projects to display in the PDF
     */
    private final SortedList<Project> sortedProjects;

    /**
     * Form elements mapping
     */
    private HashMap<String, Form> forms;

    /**
     * Form validation listeners
     */
    private ArrayList<FormListener> formListeners;

    /**
     * Flag indicating if the add button was used
     */
    private boolean addButtonUsed = false;


    /**
     * Maximum projects to export
     */
    private int MAX_PROJECTS_TO_EXPORT = 8;


    /**
     * Constructor initializing the PDF creator with project data.
     *
     * @param sortedProjects sorted list of projects to include in PDF
     */
    public CreatePDF(SortedList<Project> sortedProjects) {
        this.sortedProjects = sortedProjects;
    }

    /**
     * Starts the PDF creation GUI.
     *
     * @param stage primary stage for the GUI
     */
    @Override
    public void start(Stage stage) {
        StageFactory.setName(stage, "PDF Creator");
        StageFactory.setIcon(stage);

        VBox outerPane = new VBox(10);
        outerPane.setPadding(new Insets(20));
        outerPane.setAlignment(Pos.CENTER);
        outerPane.setStyle("-fx-background-color: white");

        Label title = new Label();
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        title.setText("Neues PDF erstellen");

        GridPane gridPane = new GridPane(10, 10);
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(150),
                new ColumnConstraints(200),
                new ColumnConstraints(200)
        );

        forms = new HashMap<>();
        formListeners = new ArrayList<>();

        // Form configuration: name;label;placeholder;errorMessage;type;maxLength(optional)
        String[] formsArray = {
                "title;Überschrift;Projektname eingeben;Bitte Überschrift eingeben;format;^[0-9]{5};5;45",
                "path;Speicherort (Ordner);C:\\Users\\Name\\Downloads;Bitte Ordner-Pfad eingeben!;text;260",
                "nOfProj; Anzahl Projekte (1-8);8;Bitte Ganzzahl eingeben (1-8);format;^[1-8]{1}$;1;1"
        };

        // Create form elements based on configuration
        int i;
        for (i = 0; i < formsArray.length; i++) {
            String[] parts = formsArray[i].split(";");
            String name = parts[0];

            if (parts[4].equals("format")) {
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), parts[5], Integer.parseInt(parts[6]), Integer.parseInt(parts[7])
                ));
            } else if (parts[4].equals("text")) {
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), Integer.parseInt(parts[5])));
            }

            gridPane.add(forms.get(name).getLabel(), 0, i);
            gridPane.add(forms.get(name).getInputField(), 1, i);
            gridPane.add(forms.get(name).getInvalidLabel(), 2, i);
        }

        String infoTextDefault = "Die Projektdatei wir nach der Projektnummer in der Überschrift benannt.\nEs werden die ersten 1-8 gefilterten Projekte als PDF exportiert.";
        Label infoText = new Label(infoTextDefault);
        forms.get("nOfProj").getInputField().focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (formListeners.get(2).isValid()) {
                infoText.setText("Die Projektdatei wir nach der Projektnummer in der Überschrift benannt.\nEs werden die ersten " + forms.get("nOfProj").getInput() + " Projekte als PDF exportiert.");
            } else {
                infoText.setText(infoTextDefault);
            }
        }));
        gridPane.add(infoText, 0, i);
        GridPane.setColumnSpan(infoText, 3);

        Button addButton = new Button("PDF erstellen");
        gridPane.add(addButton, 0, formsArray.length + 1);
        addButton.prefWidthProperty().bind(gridPane.widthProperty());
        GridPane.setColumnSpan(addButton, 2);

        addButton.setOnAction(event -> {
            if (validate()) {
                MAX_PROJECTS_TO_EXPORT = Integer.parseInt(forms.get("nOfProj").getInput());
                String pdfPath = forms.get("path").getInput().replaceAll("\"", "");
                PDFCreator creator = new PDFCreator();
                String message = creator.create(
                        sortedProjects,
                        forms.get("title").getInput(),
                        pdfPath
                );

                Hyperlink textLink = new Hyperlink("Ordner öffnen.");
                textLink.setOnAction(newEvent -> getHostServices().showDocument(pdfPath));

                Label label = new Label("Daten erfolgreich exportiert. ");

                HBox content = new HBox(label, textLink);
                content.setAlignment(Pos.CENTER_LEFT);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF erstellen");
                alert.setHeaderText(null);
                alert.getDialogPane().setContent(content);
                alert.showAndWait();

                addButtonUsed = true;
                stage.close();
            }
        });

        outerPane.getChildren().addAll(title, gridPane);
        Scene scene = new Scene(outerPane);

        // Apply CSS styling if available
        URL cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS file not found!");
        }

        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(300);
        stage.showAndWait();
    }

    /**
     * Validates all form inputs.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validate() {
        boolean inputIsValid = false;
        for (FormListener formListener : formListeners) {
            formListener.validate();
            inputIsValid = formListener.isValid();
            formListener.setInvalidLabel(!inputIsValid);
        }
        return inputIsValid;
    }

    /**
     * Returns whether the add button was used.
     *
     * @return true if PDF was created, false otherwise
     */
    public boolean getAddButtonStatus() {
        return addButtonUsed;
    }
}