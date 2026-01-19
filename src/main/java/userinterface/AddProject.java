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
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(300));

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
                "type;Objekttyp;Miete/Verkauf;Bitte Typ auswählen!;dropdown",
                "squareMeter;HNF in m²;3456;Keine gültige Zahl!;number;1;2147483647",
                "dataPath;BKP Dateipfad;C:\\Benutzer\\Name\\Downloads\\kv1.csv;Kein gültiger Pfad!;text"};

        HashMap<String, Form> forms = new HashMap<>();
        ArrayList<FormListener> formListeners = new ArrayList<>();

        //Converts formsArray into elements, creates listeners, and adds them to the map/array.
        for (int i = 0; i < formsArray.length; i++){
            String[] parts = formsArray[i].split(";");
            String name = parts[0];
            if(parts[4].equals("dropdown")){
                var form = new DropdownForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name));
                gridPane.add(form.getLabel(), 0, i);
                gridPane.add(form.getInputField(), 1, i);
                gridPane.add(form.getInvalidLabel(), 2, i);
            } else if (parts[4].equals("number")){
                var form = new InputForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name, Integer.parseInt(parts[5]), Integer.parseInt(parts[6])));
                gridPane.add(form.getLabel(), 0, i);
                gridPane.add(form.getInputField(), 1, i);
                gridPane.add(form.getInvalidLabel(), 2, i);
            } else if (parts[4].equals("text")){
                var form = new InputForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name));
                gridPane.add(form.getLabel(), 0, i);
                gridPane.add(form.getInputField(), 1, i);
                gridPane.add(form.getInvalidLabel(), 2, i);
            }
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
                controller.addProject(
                        Integer.parseInt(forms.get("projectNr").getInput()),
                        forms.get("address").getInput(),
                        Integer.parseInt(forms.get("plz").getInput()),
                        forms.get("location").getInput(),
                        forms.get("owner").getInput(),
                        forms.get("type").getInput(),
                        Integer.parseInt(forms.get("squareMeter").getInput()),
                        forms.get("dataPath").getInput()
                );

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Projekt hinzugefügt");
                alert.setHeaderText("Bestätigung");
                alert.setContentText(
                        "Das Projekt Nr. " + Integer.parseInt(forms.get("projectNr").getInput()) +
                        " wurde erfolgreich hinzugefügt.\nDrücken Sie OK um den Vorgang zu beenden."
                );
                alert.showAndWait();

                addProjectStage.close();
            }
        });


        addButton.prefWidthProperty().bind(gridPane.widthProperty());
        gridPane.add(addButton,0,8);
        GridPane.setColumnSpan(addButton,2);

        outerPane.getChildren().addAll(gridPane);

        Scene scene = new Scene(outerPane);

        addProjectStage.setScene(scene);
        addProjectStage.setMinWidth(650);
        addProjectStage.setMinHeight(400);
        addProjectStage.show();
    }
}
