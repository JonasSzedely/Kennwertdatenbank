package userinterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import kennwertdatenbank.Controller;

import java.util.HashMap;

public class AddProject extends Application {

    Controller controller;
    public AddProject(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void start(Stage addProjectStage) throws Exception {



        var outerPane = new VBox();
        outerPane.setAlignment(Pos.CENTER);


        var grid = new GridPane(10,10);
        grid.setPadding(new Insets(20,20,20,20));
        grid.setAlignment(Pos.CENTER);
        grid.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(300));

        String [] formsArray = {
                "projectNrForm;Projekt Nummer;10000;Keine gültige Zahl!",
                "addressForm;Adresse;Musterstrasse 5;Ungültige Adresse!",
                "plzForm;Postleitzahl;8001;Keine gültige Zahl!",
                "locationForm;Ort;Zürich;Keine gültige Zahl!",
                "ownerForm;Bauherr;Marcel Muster;Ungültiger Name!",
                "typeForm;Objekttyp;Miete/Verkauf;Bitte Typ auswählen!",
                "squareMeterFrom;HNF in m²;3456;Keine gültige Zahl!",
                "dataPathForm;BKP Dateipfad;C:\\Benutzer\\Name\\Downloads\\kv1.csv;Kein gültiger Pfad!"};

        HashMap<String, Form> forms = new HashMap<>();

        for (int i = 0; i < formsArray.length; i++){
            String[] parts = formsArray[i].split(";");
            String name = parts[0];
            if(name.equals("typeForm")){
                var dropdown = new DropdownForm(parts[1], parts[2], parts[3]);
                forms.put(name, dropdown);
                grid.add(dropdown.getLabel(), 0, i);
                grid.add(dropdown.getInputField(), 1, i);
                grid.add(dropdown.getInvalidLabel(), 2, i);

            } else {
                var form = new InputForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                grid.add(form.getLabel(), 0, i);
                grid.add(form.getInputField(), 1, i);
                grid.add(form.getInvalidLabel(), 2, i);
            }
        }

        var projectNrForm = (InputForm) forms.get("projectNrForm");
        var addressForm = (InputForm) forms.get("addressForm");
        var plzForm = (InputForm) forms.get("plzForm");
        var locationForm = (InputForm) forms.get("locationForm");
        var ownerForm = (InputForm) forms.get("ownerForm");
        var typeForm = (DropdownForm) forms.get("typeForm");
        var squareMeterFrom = (InputForm) forms.get("squareMeterFrom");
        var dataPathForm = (InputForm) forms.get("dataPathForm");

        //patern from claude.ai
        projectNrForm.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                var checker = new NumberValidator(projectNrForm, 10000, 99999);
                projectNrForm.getInvalidLabel().setVisible(!checker.isValid());
            }
        });

        addressForm.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                addressForm.getInvalidLabel().setVisible(addressForm.getTextField().getText().isEmpty());
            }
        });

        plzForm.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                var checker = new NumberValidator(plzForm, 1000, 9999);
                plzForm.getInvalidLabel().setVisible(!checker.isValid());
            }
        });

        locationForm.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                locationForm.getInvalidLabel().setVisible(locationForm.getTextField().getText().isEmpty());
            }
        });

        ownerForm.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                ownerForm.getInvalidLabel().setVisible(ownerForm.getTextField().getText().isEmpty());
            }
        });

        typeForm.getComboBox().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                typeForm.getInvalidLabel().setVisible(typeForm.getComboBox().getValue() == null);
            }
        });

        squareMeterFrom.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                var checker = new NumberValidator(squareMeterFrom, 1, Integer.MAX_VALUE);
                squareMeterFrom.getInvalidLabel().setVisible(!checker.isValid());
            }
        });

        dataPathForm.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                dataPathForm.getInvalidLabel().setVisible(dataPathForm.getTextField().getText().isEmpty());
            }
        });


        final var addButton = new Button("Projekt hinzufügen");
        addButton.setOnAction(event -> {

            int projectNr = 0;
            String address = "";
            int plz = 0;
            String location;
            String owner;
            String type;
            int squareMeter = 0;
            String dataPath;

            var checkerProjectNr = new NumberValidator(projectNrForm, 10000, 99999);
            boolean projctNrValid = checkerProjectNr.isValid();
            projectNrForm.getInvalidLabel().setVisible(!projctNrValid);

            boolean addressValid = !addressForm.getTextField().getText().isEmpty();
            addressForm.getInvalidLabel().setVisible(!addressValid);

            var checkerPLZ = new NumberValidator(plzForm, 1000, 9999);
            boolean plzValid = checkerPLZ.isValid();
            plzForm.getInvalidLabel().setVisible(!plzValid);

            boolean locationValid = !locationForm.getTextField().getText().isEmpty();
            locationForm.getInvalidLabel().setVisible(!locationValid);

            boolean ownerValid = !ownerForm.getTextField().getText().isEmpty();
            ownerForm.getInvalidLabel().setVisible(!ownerValid);

            boolean typeValid = true;
            if (typeForm.getComboBox().getValue() == null){
                typeForm.getInvalidLabel().setVisible(true);
                typeValid = false;
            }

            var checkerSquareMeter = new NumberValidator(squareMeterFrom, 1, Integer.MAX_VALUE);
            boolean squareMeterValid = checkerSquareMeter.isValid();
            squareMeterFrom.getInvalidLabel().setVisible(!squareMeterValid);

            boolean dataPathValid = !dataPathForm.getTextField().getText().isEmpty();
            dataPathForm.getInvalidLabel().setVisible(!dataPathValid);

            if (projctNrValid & addressValid & plzValid & locationValid & ownerValid & typeValid & squareMeterValid & dataPathValid){
                projectNr = Integer.valueOf(projectNrForm.getTextField().getText());
                address = addressForm.getTextField().getText();
                plz = Integer.valueOf(plzForm.getTextField().getText());
                location = locationForm.getTextField().getText();
                owner = ownerForm.getTextField().getText();
                type = typeForm.getValue();
                squareMeter = Integer.valueOf(squareMeterFrom.getTextField().getText());
                dataPath = dataPathForm.getTextField().getText();
                controller.addProject(projectNr, address, plz, location, owner, type, squareMeter, dataPath);
                controller.getProjects();
                addProjectStage.close();
            }
        });


        addButton.prefWidthProperty().bind(grid.widthProperty());
        grid.add(addButton,0,8);
        GridPane.setColumnSpan(addButton,2);

        Scene scene = new Scene(grid, 800,400);

        addProjectStage.setScene(scene);
        addProjectStage.show();


    }
}
