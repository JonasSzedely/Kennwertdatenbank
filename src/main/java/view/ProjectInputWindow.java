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
import model.Project;
import model.ProjectData;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


class ProjectInputWindow extends Application {

    private final Controller controller;
    private final Type type;
    private Project project;
    private Button addButton;
    private boolean addButtonUsed = false;
    private LinkedHashMap<String, Form> forms;
    private ArrayList<FormListener> formListeners;
    private Stage stage;
    /**
     * Creates a new Window that allows to add a project to the Database.
     * It validates the input before it can be added, and flags wrong inputs in the UI.
     *
     * @param controller pass the controller class object.
     */
    public ProjectInputWindow(Controller controller, Type type) {
        this.controller = controller;
        this.type = type;
    }

    /**
     * Creates a new Window that allows to edit a project.
     * It validates the input before it can be added, and flags wrong inputs in the UI.
     *
     * @param controller pass the controller class object.
     * @param project    the project to be edited.
     */
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
            case MODIFY ->
                    title.setText("Projekt Nr. " + project.getProjectNr() + " Version " + project.getVersion() + " bearbeiten");
            case NEXT -> title.setText("Neue Version von Projekt Nr. " + project.getProjectNr() + " hinzufügen");
        }

        GridPane gridPane = new GridPane(10, 10);
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(150), new ColumnConstraints(200), new ColumnConstraints(150), new ColumnConstraints(150), new ColumnConstraints(200), new ColumnConstraints(150));
        forms = new LinkedHashMap<>();
        formListeners = new ArrayList<>();
        /*
        Each element in the array represents an input field with a description.
        */
        addInputNumber("projectNr", "Projekt Nummer", "10000", "Keine gültige Zahl!", 10000, 99999);
        addInputText("address", "Adresse", "Musterstrasse 5", "Bitte Adresse eingeben!", 22);
        addInputNumber("plz", "Postleitzahl", "8001", "Keine gültige Zahl!", 1000, 9999);
        addInputText("location", "Ort", "Zürich", "Bitte Ortschaft eingeben!", 22);
        addInputText("owner", "Bauherr", "Marcel Muster", "Bitte Name eingeben!", 22);
        addDropdown("propertyType", "Gebäudenutzungen", "Miete|Stockwerkeigentum|Gewerbe/Industrie|Wohnen+Gewerbe", "Bitte auswählen!");
        addDropdown("constructionType", "Art des Bauvorhaben", "Neubau|Sanierung|Umbau|Anbau|Ausbau", "Bitte auswählen!");
        addDropdown("documentPhase", "Planungsstand", "2|31|32|33|41|5", "Bitte auswählen!");
        addDropdown("calculationPhase", "Gerechnete Phasen ab", "2|31|32|33|41|5", "Bitte auswählen!");
        addInputNumber("apartmentsNr", "Anzahl Wohnungen", "15", "Keine gültige Zahl!", 1, 2147483647);
        addInputNumber("bathroomNr", "Anzahl Nasszellen", "33", "Keine gültige Zahl!", 1, 2147483647);
        addInputNumber("hnf", "HNF inkl. Reduit in m²", "3456", "Keine gültige Zahl!", 1, 2147483647);
        addInputNumber("gf", "GF in m²", "3456", "Keine gültige Zahl!", 1, 2147483647);
        addInputNumber("parcelSize", "Grundstücksfläche in m2", "3456", "Keine gültige Zahl!", 1,2147483647);
        addInputNumber("landscapedArea", "Umgebungsfläche in m2", "3456", "Keine gültige Zahl!", 1,2147483647);
        addInputNumber("volumeUnderground", "Volumen unterirdisch m³", "3456", "Keine gültige Zahl!", 1, 2147483647);
        addInputNumber("volumeAboveUnderground", "Volumen überirdisch m³", "4567", "Keine gültige Zahl!", 1, 2147483647);
        addInputNumber("facadearea", "Fassadenfläche in m²", "12345", "Keine gültige Zahl!", 1, 2147483647);
        addInputNumber("windowarea", "Fensterfläche in m²", "6789", "Keine gültige Zahl!", 1, 2147483647);
        addDropdown("facadeType", "Fassade", "AWD-Standard|AWD-Hochwertig|Zweischallen-Mauerwerk|Hinterlüftet-Holz|Hinterlüftete-Stein|Hinterlüftete-Metall", "Bitte auswählen!");
        addDropdown("windowType", "Fenster", "Kunststoff|Kunststoff-Metall|Metall|Holz|Holz-Metall", "Bitte auswählen!");
        addDropdown("roofType", "Dach", "Flachdach|Steildach|Flach-Steildach-Kombi", "Bitte auswählen!");
        addDropdown("heatingType", "Heizung", "Luft-Luft|Luft-Wasser|Erdsonde|Gas|Öl|Pellet|Fernerwärme|Unklar", "Bitte auswählen!");
        addDropdown("coolingType", "Kühlung", "keine|FreeCooling|Unklar", "Bitte auswählen!");
        addDropdown("ventilationTypeApartments", "Lüftung Wohnungen", "keine|Abluft|KWL zentral|KWL je Whg|Unklar", "Bitte auswählen!");
        addDropdown("ventilationTypeUG", "Lüftung UG", "natürlich|Abluft|Zu- & Abluft|Unklar", "Bitte auswählen!");
        addDropdown("coNO", "CO/NO-Anlage", "Ja|Nein|Unklar", "Bitte auswählen!");
        addInputText("special", "Spezielles", "Spezielle Projekt Eigenschaften", "Ungültiger Eingabe!", 65);
        addInputText("dataPath", "BKP Dateipfad", "C:\\Benutzer\\Name\\Downloads\\kv.csv", "Kein gültiger Pfad!", 260);

        //adding the forms to the GridPane
        List<String> keys = new ArrayList<>(forms.keySet());
        String midKey = keys.get((keys.size() - 1) / 2);
        int j = 0, k = 0;
        for (String name : keys) {
            if (name.equals(midKey)) {
                k = 3;
                j = 0;
            } else if (name.equals("dataPath")) {
                k = 0;
            }

            gridPane.add(forms.get(name).getLabel(), k, j);
            gridPane.add(forms.get(name).getInputField(), k + 1, j);
            gridPane.add(forms.get(name).getInvalidLabel(), k + 2, j);

            if (name.equals("dataPath")) {
                GridPane.setColumnSpan(forms.get(name).getInputField(), 4);
                GridPane.setColumnIndex(forms.get(name).getInvalidLabel(), 5);
            }
            j++;
        }

        //Creates a button to add the new project to the DB.
        //The button checks whether the input in the form is valid and passes the data to the controller.
        addButton = new Button();

        switch (type) {
            case NEW -> {
                //Adding a new Project to the DB
                addButton.setText("Projekt hinzufügen");
                addButton.setOnAction(event -> {
                    if (validate()) {
                        String message = addProject();
                        confirmationWindow("Projekt hinzugefügt", message);
                    }
                });
            }
            case MODIFY -> {
                //Modifying a Project in the DB
                addButton.setText("Projekt anpassen");
                fillFields();
                forms.get("projectNr").getInputField().setDisable(true);
                forms.get("dataPath").setInputFieldText("Projekt Kosten können nicht überschrieben werden. Bitte neues Projekt anlegen.");
                forms.get("dataPath").getInputField().setDisable(true);

                addButton.setOnAction(event -> {
                    if (validate()) {
                        String message = modifyProject();
                        confirmationWindow("Projekt angepasst", message);
                    }
                });
            }
            case NEXT -> {
                //Adding a new version of the Project to the DB
                addButton.setText("Neue Version hinzufügen");
                fillFields();
                forms.get("projectNr").getInputField().setDisable(true);

                addButton.setOnAction(event -> {
                    if (validate()) {
                        String message = addProject();
                        confirmationWindow("Neue Version von Projekt Nr. \" + project.getProjectNr() + \" hinzugefügt", message);
                    }
                });
            }
        }

        addButton.prefWidthProperty().bind(gridPane.widthProperty());
        gridPane.add(addButton, 0, (forms.size() / 2) + 1);
        GridPane.setColumnSpan(addButton, 5);

        outerPane.getChildren().addAll(title, gridPane);
        Scene scene = new Scene(outerPane);

        //adding CSS to the Scene
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

    private void addInputNumber(String name, String label, String placeholder, String error, int min, int max) {
        forms.put(name, new InputForm(label, placeholder, error));
        formListeners.add(new FormListener((InputForm) forms.get(name), min, max));
    }

    private void addInputText(String name, String label, String placeholder, String error, int maxLength) {
        forms.put(name, new InputForm(label, placeholder, error));
        formListeners.add(new FormListener((InputForm) forms.get(name), maxLength));
    }

    private void addDropdown(String name, String label, String options, String error) {
        forms.put(name, new DropdownForm(label, options, error));
        formListeners.add(new FormListener((DropdownForm) forms.get(name)));
    }

    public boolean getAddButtonStatus() {
        return addButtonUsed;
    }

    private void fillFields() {
        forms.get("projectNr").setInputFieldText(String.valueOf(project.getProjectNr()));
        forms.get("address").setInputFieldText(project.getAddress());
        forms.get("plz").setInputFieldText(String.valueOf(project.getPlz()));
        forms.get("location").setInputFieldText(project.getLocation());
        forms.get("owner").setInputFieldText(project.getOwner());
        forms.get("propertyType").setInputFieldText(project.getPropertyType());
        forms.get("constructionType").setInputFieldText(project.getConstructionType());
        forms.get("documentPhase").setInputFieldText(String.valueOf(project.getDocumentPhase()));
        forms.get("calculationPhase").setInputFieldText(String.valueOf(project.getCalculationPhase()));
        forms.get("apartmentsNr").setInputFieldText(String.valueOf(project.getApartmentsNr()));
        forms.get("bathroomNr").setInputFieldText(String.valueOf(project.getBathroomNr()));
        forms.get("hnf").setInputFieldText(String.valueOf(project.getHnf()));
        forms.get("gf").setInputFieldText(String.valueOf(project.getGf()));
        forms.get("parcelSize").setInputFieldText(String.valueOf(project.getParcelSize()));
        forms.get("landscapedArea").setInputFieldText(String.valueOf(project.getLandscapedArea()));
        forms.get("volumeUnderground").setInputFieldText(String.valueOf(project.getVolumeUnderground()));
        forms.get("volumeAboveUnderground").setInputFieldText(String.valueOf(project.getVolumeAboveGround()));
        forms.get("facadearea").setInputFieldText(String.valueOf(project.getFacadeArea()));
        forms.get("windowarea").setInputFieldText(String.valueOf(project.getWindowArea()));
        forms.get("facadeType").setInputFieldText(project.getFacadeType());
        forms.get("windowType").setInputFieldText(project.getWindowType());
        forms.get("roofType").setInputFieldText(project.getRoofType());
        forms.get("heatingType").setInputFieldText(project.getHeatingType());
        forms.get("coolingType").setInputFieldText(project.getCoolingType());
        forms.get("ventilationTypeApartments").setInputFieldText(project.getVentilationTypeApartments());
        forms.get("ventilationTypeUG").setInputFieldText(project.getVentilationTypeUg());
        forms.get("coNO").setInputFieldText(project.getCoNo());
        forms.get("special").setInputFieldText(project.getSpecial());
    }

    private String addProject() {
        Project project = new Project(
                Integer.parseInt(forms.get("projectNr").getInput()),
                1,
                forms.get("address").getInput(),
                Integer.parseInt(forms.get("plz").getInput()),
                forms.get("location").getInput(),
                forms.get("owner").getInput(),
                forms.get("propertyType").getInput(),
                forms.get("constructionType").getInput(),
                Integer.parseInt(forms.get("documentPhase").getInput()),
                Integer.parseInt(forms.get("calculationPhase").getInput()),
                Integer.parseInt(forms.get("apartmentsNr").getInput()),
                Integer.parseInt(forms.get("bathroomNr").getInput()),
                Integer.parseInt(forms.get("hnf").getInput()),
                Integer.parseInt(forms.get("gf").getInput()),
                Integer.parseInt(forms.get("parcelSize").getInput()),
                Integer.parseInt(forms.get("landscapedArea").getInput()),
                Integer.parseInt(forms.get("volumeUnderground").getInput()),
                Integer.parseInt(forms.get("volumeAboveUnderground").getInput()),
                Integer.parseInt(forms.get("facadearea").getInput()),
                Integer.parseInt(forms.get("windowarea").getInput()),
                forms.get("facadeType").getInput(),
                forms.get("windowType").getInput(),
                forms.get("roofType").getInput(),
                forms.get("heatingType").getInput(),
                forms.get("coolingType").getInput(),
                forms.get("ventilationTypeApartments").getInput(),
                forms.get("ventilationTypeUG").getInput(),
                forms.get("coNO").getInput(),
                forms.get("special").getInput(),
                new ProjectData(forms.get("dataPath").getInput().replaceAll("\"", "").trim())
        );
        return controller.addProject(project);
    }

    private String modifyProject() {
        project.setAddress(forms.get("address").getInput());
        project.setPlz(Integer.parseInt(forms.get("plz").getInput()));
        project.setLocation(forms.get("location").getInput());
        project.setOwner(forms.get("owner").getInput());
        project.setPropertyType(forms.get("propertyType").getInput());
        project.setConstructionType(forms.get("constructionType").getInput());
        project.setDocumentPhase(Integer.parseInt(forms.get("documentPhase").getInput()));
        project.setCalculationPhase(Integer.parseInt(forms.get("calculationPhase").getInput()));
        project.setApartmentsNr(Integer.parseInt(forms.get("apartmentsNr").getInput()));
        project.setBathroomNr(Integer.parseInt(forms.get("bathroomNr").getInput()));
        project.setHnf(Integer.parseInt(forms.get("hnf").getInput()));
        project.setGf(Integer.parseInt(forms.get("gf").getInput()));
        project.setParcelSize(Integer.parseInt(forms.get("parcelSize").getInput()));
        project.setLandscapedArea(Integer.parseInt(forms.get("landscapedArea").getInput()));
        project.setVolumeUnderground(Integer.parseInt(forms.get("volumeUnderground").getInput()));
        project.setVolumeAboveGround(Integer.parseInt(forms.get("volumeAboveUnderground").getInput()));
        project.setFacadeArea(Integer.parseInt(forms.get("facadearea").getInput()));
        project.setWindowArea(Integer.parseInt(forms.get("windowarea").getInput()));
        project.setFacadeType(forms.get("facadeType").getInput());
        project.setWindowType(forms.get("windowType").getInput());
        project.setRoofType(forms.get("roofType").getInput());
        project.setHeatingType(forms.get("heatingType").getInput());
        project.setCoolingType(forms.get("coolingType").getInput());
        project.setVentilationTypeApartments(forms.get("ventilationTypeApartments").getInput());
        project.setVentilationTypeUg(forms.get("ventilationTypeUG").getInput());
        project.setCoNo(forms.get("coNO").getInput());
        project.setSpecial(forms.get("special").getInput());

        return controller.modifyProjects(project);
    }

    private boolean validate() {
        boolean inputIsValid = false;
        for (FormListener formListener : formListeners) {
            formListener.validate();
            inputIsValid = formListener.isValid();
            formListener.setInvalidLabel(!inputIsValid);
        }
        return inputIsValid;
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

    enum Type {
        NEW,
        MODIFY,
        NEXT
    }
}