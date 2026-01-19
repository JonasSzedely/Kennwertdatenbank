package userinterface;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;


public class DropdownForm implements Form{
    private final String name;
    private final String validateText;
    private final Label label;
    private final ComboBox<String> comboBox;
    private final Label invalid;

    public DropdownForm(String name, String optionsList, String validateText){
        this.name = name;
        this.validateText = validateText;
        this.label = new Label(this.name);
        this.comboBox = new ComboBox();
        String[] parts = optionsList.split("/");

        //Code Abschnitt von claude.ai
        ObservableList<String> options = FXCollections.observableArrayList(parts);
        comboBox.setItems(options);
        comboBox.setPromptText("Bitte wählen");
        comboBox.setMaxWidth(Double.MAX_VALUE);

        this.invalid = new Label(this.validateText);
        invalid.setVisible(false);
        invalid.setTextFill(Color.RED);

    }



    @Override
    public Label getLabel(){
        return label;
    }

    @Override
    public Control getInputField(){
        return comboBox;
    }

    @Override
    public Label getInvalidLabel(){
        return invalid;
    }

    public ComboBox getComboBox() {
        return comboBox;
    }

    public String getValue() {
        return comboBox.getSelectionModel().getSelectedItem();
    }


}


