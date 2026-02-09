package userinterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import kennwertdatenbank.Controller;
import kennwertdatenbank.Project;
import java.util.ArrayList;
import java.util.HashMap;


public class ProjectInputWindow extends Application {

    public enum Type {
        NEW,
        MODIFY,
        NEXT
    }
    private Controller controller;
    private boolean modify = false;
    private Project project;
    private Button addButton;
    private boolean addButtonUsed = false;
    private Type type;
    private HashMap<String, Form> forms;
    private ArrayList<FormListener> formListeners;
    private Stage stage;

    /**
     * Creates a new Window that allows to add a project to the Database.
     * It validates the input before it can be added, and flags wrong inputs in the UI.
     * @param controller pass the controller class object.
     */
    public ProjectInputWindow(Controller controller, Type type) {
        this.controller = controller;
        this.type = type;
    }

    /**
     * Creates a new Window that allows to edit a project.
     * It validates the input before it can be added, and flags wrong inputs in the UI.
     * @param controller pass the controller class object.
     * @param project the project to be edited.
     */
    public ProjectInputWindow(Controller controller, Project project, Type type){
        this.controller = controller;
        this.project = project;
        this.modify = true;
        this.type = type;
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("Projekt Formular");

        var outerPane = new VBox(10);
        outerPane.setPadding(new Insets(20));
        outerPane.setAlignment(Pos.CENTER);
        outerPane.setStyle("-fx-background-color: white");

        Label title = new Label();
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        switch(type){
            case NEW -> title.setText("Neues Projekt hinzufügen");
            case MODIFY -> title.setText("Projekt Nr. " + project.getProjectNr() + " Version " + project.getVersion() + " bearbeiten");
            case NEXT -> title.setText("Neue Version von Projekt Nr. " + project.getProjectNr() + " hinzufügen");
        }

        var gridPane = new GridPane(10,10);
        gridPane.setPadding(new Insets(20,20,20,20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(150), new ColumnConstraints(200), new ColumnConstraints(150), new ColumnConstraints(150), new ColumnConstraints(200), new ColumnConstraints(150));

        /**
         * Each element in the array represents an input field with a description.
         * There are 3 types: number, text, dropdown
         * Each element requires the following values (only type number requires the validation parameters)
         * LABEL ; NAME ; EXAMPLE TEXT ; WARNING ; TYPE ; VALIDATION-PARAMETER MIN ; VALIDATION-PARAMETER MAX
         */
        String[] formsArray = {
                "projectNr;Projekt Nummer;10000;Keine gültige Zahl!;number;10000;99999",
                "address;Adresse;Musterstrasse 5;Bitte Adresse eingeben!;text",
                "plz;Postleitzahl;8001;Keine gültige Zahl!;number;1000;9999",
                "location;Ort;Zürich;Bitte Ortschaft eingeben!;text",
                "owner;Bauherr;Marcel Muster;Ungültiger Name!;text",
                "propertyType;Gebäudenutzungen;Miete|Stockwerkeigentum|Gewerbe/Industrie|Wohnen/Gewerbe;Bitte auswählen!;dropdown",
                "constructionType;Art des Bauvorhaben;Neubau|Sanierung|Umbau|Anbau|Ausbau;Bitte auswählen!;dropdown",
                "documentPhase;Planungsstand;2|31|32|33|41|5;Bitte auswählen!;dropdown",
                "calculationPhase;Gerechnete Phasen ab;2|31|32|33|41|5;Bitte auswählen!;dropdown",
                "apartmentsNr;Anzahl Wohnungen;15;Keine gültige Zahl!;number;1;2147483647",
                "bathroomNr;Anzahl Nasszellen;33;Keine gültige Zahl!;number;1;2147483647",
                "hnf;HNF in m²;3456;Keine gültige Zahl!;number;1;2147483647",
                "gf;GF in m²;3456;Keine gültige Zahl!;number;1;2147483647",
                "volumeUnderground;Volumen unterirdisch in m³;3456;Keine gültige Zahl!;number;1;2147483647",
                "volumeAboveUnderground;Volumen überirdisch in m³;4567;Keine gültige Zahl!;number;1;2147483647",
                "facadearea;Fassadenfläche in m²;12345;Keine gültige Zahl!;number;1;2147483647",
                "windowarea;Fensterfläche in m²;6789;Keine gültige Zahl!;number;1;2147483647",
                "facadeType;Fassade;AWD-Standard|AWD-Hochwertig|Zweischallen-Mauerwerk|Hinterlüftet-Holz|Hinterlüftete-Stein|Hinterlüftete-Metall;Bitte auswählen!;dropdown",
                "windowType;Fenster;Kunststoff|Kunststoff-Metall|Metall|Holz|Holz-Metall;Bitte auswählen!;dropdown",
                "roofType;Dach;Flachdach|Steildach|Flach-Steildach-Kombi;Bitte auswählen!;dropdown",
                "heatingType;Heizung;Luft-Luft|Luft-Wasser|Erdsonde|Gas|Öl|Pellet|Fernerwärme|Unklar;Bitte auswählen!;dropdown",
                "coolingType;Kühlung;keine|FreeCooling|Unklar;Bitte auswählen!;dropdown",
                "ventilationTypeApartments;Lüftung Wohnungen;keine|Abluft|KWL zentral|KWL je Whg|Unklar;Bitte auswählen!;dropdown",
                "ventilationTypeUG;Lüftung UG;natürlich|Abluft|Zu- & Abluft|Unklar;Bitte auswählen!;dropdown",
                "coNO;CO/NO-Anlage;Ja|Nein|Unklar;Bitte auswählen!;dropdown",
                "special;Spezielles;Spezielle Projekt Eigenschaften;Ungültiger Eingabe!;special;65",
                "dataPath;BKP Dateipfad;C:\\Benutzer\\Name\\Downloads\\kv.csv;Kein gültiger Pfad!;text"
        };

        forms = new HashMap<>();
        formListeners = new ArrayList<>();
        int j = 0;
        int k = 0;

        //Converts formsArray into elements, creates listeners, and adds them to the map/array.
        for (int i = 0; i < formsArray.length; i++){
            String[] parts = formsArray[i].split(";");
            String name = parts[0];

            if (i == (formsArray.length-1)/2){
                k=3;
                j=0;
            } else if (name.equals("dataPath")){
                k=0;
            }
            if(parts[4].equals("dropdown")){
                forms.put(name, new DropdownForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((DropdownForm) forms.get(name), name));
            } else if (parts[4].equals("number")){
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), name, Integer.parseInt(parts[5]), Integer.parseInt(parts[6])));
            } else if (parts[4].equals("text")){
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), name));
            } else if (parts[4].equals("special")){
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), name, Integer.parseInt(parts[5])));
            }

            gridPane.add(forms.get(name).getLabel(), k, j);
            gridPane.add(forms.get(name).getInputField(), k+1, j);
            gridPane.add(forms.get(name).getInvalidLabel(), k+2, j);
            if (name.equals("dataPath")){
                GridPane.setColumnSpan(forms.get(name).getInputField(),4);
                GridPane.setColumnIndex(forms.get(name).getInvalidLabel(),5);
            }
            j++;
        }

        //Creates a button to add the new project to the DB.
        //The button checks whether the input in the form is valid and passes the data to the controller.
        addButton = new Button();

        switch(type){
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
                addButton.setText("Projekt anpassen");
                fillFields();
                forms.get("projectNr").getInputField().setDisable(true);
                forms.get("dataPath").setInputFieldText("Projekt Kosten können nicht überschrieben werden. Bitte neues Projekt anlegen.");
                forms.get("dataPath").getInputField().setDisable(true);

                addButton.setOnAction(event ->{
                    if (validate()){
                        String message = modifyProject();
                        confirmationWindow("Projekt angepasst", message);
                    }
                });
            }
            case NEXT -> {
                //Adding a new Project to the DB
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
        gridPane.add(addButton,0,(formsArray.length/2)+1);
        GridPane.setColumnSpan(addButton,5);

        outerPane.getChildren().addAll(title, gridPane);

        Scene scene = new Scene(outerPane);

        var cssResource = getClass().getResource("/style.css");
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

    public boolean getAddButtonStatus(){
        return addButtonUsed;
    }

    private void fillFields(){
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
        return controller.addProject(
                Integer.parseInt(forms.get("projectNr").getInput()),
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
                forms.get("dataPath").getInput()
        );
    }

    private String modifyProject() {
        return controller.modifyProject(
                project.getProjectNr(),
                project.getVersion(),
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
                forms.get("special").getInput()
        );
    }

    private boolean validate(){
        boolean inputIsValid = false;
        for (FormListener formListener : formListeners) {
            formListener.validate();
            inputIsValid = formListener.isValid();
            formListener.setInvalidLabel(inputIsValid);
        }
        return inputIsValid;
    }

    private void confirmationWindow(String title, String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Rückmeldung");
        alert.setContentText(message + "\nDrücken Sie OK um den Vorgang zu beenden.");
        alert.showAndWait();
        addButtonUsed = true;
        stage.close();
    }

}
