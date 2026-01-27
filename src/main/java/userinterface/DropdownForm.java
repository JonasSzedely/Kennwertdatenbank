package userinterface;


import com.sun.javafx.scene.control.InputField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;


public class DropdownForm implements Form{
    private final String name;
    private final String validateText;
    private final Label label;
    private final ComboBox<String> comboBox;
    private final Label invalid;
    private final TextField input;

    public DropdownForm(String name, String optionsList, String validateText){
        this.name = name;
        this.validateText = validateText;
        this.label = new Label(this.name);
        this.comboBox = new ComboBox<>();
        this.input = new TextField();
        this.invalid = new Label(this.validateText);

        String[] parts = optionsList.split("\\|");

        //Code Abschnitt von claude.ai
        ObservableList<String> options = FXCollections.observableArrayList(parts);
        comboBox.setItems(options);
        comboBox.setPromptText("Bitte wählen");
        comboBox.setMaxWidth(Double.MAX_VALUE);

        invalid.setVisible(false);
        invalid.setTextFill(Color.RED);

        input.setDisable(true);
        input.setVisible(false);

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

    @Override
    public String getInput() {
        return comboBox.getSelectionModel().getSelectedItem();
    }

    @Override
    public Control getAdditionalTextField() {
        return input;
    }

    @Override
    public String getAdditionalInput() {
        return input.getText();
    }

    @Override
    public String getValidateText() {
        return validateText;
    }


}


