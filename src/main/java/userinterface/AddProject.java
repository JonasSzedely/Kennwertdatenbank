package userinterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import kennwertdatenbank.Controller;
import java.util.ArrayList;
import java.util.HashMap;


public class AddProject extends Application {

    Controller controller;

    /**
     * Creates a new Window that allows to add Projects to the Database.
     * It validates the input before it can be added, and flags wrong inputs in the UI.
     * It needs a Controller Class object.
     */
    public AddProject(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void start(Stage addProjectStage) throws Exception {
        var outerPane = new VBox();
        outerPane.setAlignment(Pos.CENTER);

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
                "special;Spezielles;Spezielle Projekt Eigenschaften;Ungültiger Eingabe!;text",
                "dataPath;BKP Dateipfad;C:\\Benutzer\\Name\\Downloads\\kv1.csv;Kein gültiger Pfad!;text"
        };


        HashMap<String, Form> forms = new HashMap<>();
        ArrayList<FormListener> formListeners = new ArrayList<>();
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
                var form = new DropdownForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name));
                gridPane.add(form.getLabel(), k+0, j);
                gridPane.add(form.getInputField(), k+1, j);
                gridPane.add(form.getInvalidLabel(), k+2, j);
            } else if (parts[4].equals("number")){
                var form = new InputForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name, Integer.parseInt(parts[5]), Integer.parseInt(parts[6])));
                gridPane.add(form.getLabel(), k+0, j);
                gridPane.add(form.getInputField(), k+1, j);
                gridPane.add(form.getInvalidLabel(), k+2, j);
            } else if (parts[4].equals("text")){
                var form = new InputForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name));
                gridPane.add(form.getLabel(), k+0, j);
                gridPane.add(form.getInputField(), k+1, j);
                gridPane.add(form.getInvalidLabel(), k+2, j);
                if (name.equals("dataPath")){
                    GridPane.setColumnSpan(form.getInputField(),4);
                    GridPane.setColumnIndex(form.getInvalidLabel(),5);
                }
            }
            j++;
        }


        //Creates a button to add the new project to the DB.
        //The button checks whether the input in the form is valid and passes the data to the controller.
        var addButton = new Button("Projekt hinzufügen");

        addButton.setOnAction(event -> {
            boolean inputIsValid = false;

            for (FormListener formListener : formListeners){
                inputIsValid = formListener.isValid();
                formListener.setInvalidLabel(inputIsValid);
            }

            if (inputIsValid){
                String message =
                controller.addProject(
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

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Projekt hinzugefügt");
                alert.setHeaderText("Rückmeldung");
                alert.setContentText(message + "\nDrücken Sie OK um den Vorgang zu beenden.");
                alert.showAndWait();
                addProjectStage.close();
            }


        });


        addButton.prefWidthProperty().bind(gridPane.widthProperty());
        gridPane.add(addButton,0,(formsArray.length/2)+1);
        GridPane.setColumnSpan(addButton,5);


        outerPane.getChildren().addAll(gridPane);

        Scene scene = new Scene(outerPane);

        addProjectStage.setScene(scene);
        addProjectStage.setMinWidth(1300);
        addProjectStage.setMinHeight(800);
        addProjectStage.showAndWait();
    }
}
