package userinterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kennwertdatenbank.Controller;

import java.util.ArrayList;
import java.util.HashMap;

public class AddCalculation extends Application {

    private final Controller controller;
    /**
     * Creates a new Window that allows to add Calculations to the UI.
     */
    public AddCalculation(Controller controller) {
        this.controller = controller;

    }

    @Override
    public void start(Stage addCalculationStage) throws Exception {
        var outerPane = new VBox();
        outerPane.setAlignment(Pos.CENTER);

        var gridPane = new GridPane(10,10);
        gridPane.setPadding(new Insets(20,20,20,20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(300));




        String[] formsArray = {
                "name;Bezeichnung;CHF pro m²;Bitte Bezeichnung eingeben!;text;",
                "number1;Erster Operand;BKP|HNF|Bausumme|Anzahl Wohnungen;Bitte Typ auswählen!;dropdown;BKP;1.00;999.99",
                "operator;Operator;+|-|/|x;Bitte Typ auswählen!;dropdown;BKP;0;0",
                "number2;Zweiter Operand;BKP|HNF|Bausumme|Anzahl Wohnungen;Bitte Typ auswählen!;dropdown;BKP;1.00;999.99"
        };


        HashMap<String, Form> forms = new HashMap<>();
        ArrayList<FormListener> formListeners = new ArrayList<>();

        //Converts formsArray into elements, creates listeners, and adds them to the map/array.
        for (int i = 0; i < formsArray.length; i++){
            String[] parts = formsArray[i].split(";");
            String name = parts[0];
            if(parts[4].equals("dropdown")){
                var form = new DropdownForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name, parts[5], Double.parseDouble(parts[6]), Double.parseDouble(parts[7])));
                gridPane.add(form.getLabel(), 0, i);
                gridPane.add(form.getInputField(), 1, i);
                gridPane.add(form.getAdditionalTextField(), 2, i);
                gridPane.add(form.getInvalidLabel(), 3, i);
            } else if (parts[4].equals("text")){
                var form = new InputForm(parts[1], parts[2], parts[3]);
                forms.put(name, form);
                formListeners.add(new FormListener(form, name));
                gridPane.add(form.getLabel(), 0, i);
                gridPane.add(form.getInputField(), 1, i);
                gridPane.add(form.getInvalidLabel(), 3, i);
            }
        }

        Button addComponent = new Button("+");




        outerPane.getChildren().addAll(gridPane);

        Scene scene = new Scene(outerPane);

        addCalculationStage.setScene(scene);
        addCalculationStage.setMinWidth(650);
        addCalculationStage.setMinHeight(450);
        addCalculationStage.showAndWait();


    }
}
