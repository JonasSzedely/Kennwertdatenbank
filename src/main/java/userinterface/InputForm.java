package userinterface;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;


public class InputForm implements Form{
    private final String name;
    private final String example;
    private final String validateText;
    private final Label label;
    private final TextField input;
    private final Label invalid;

    public InputForm(String name, String example, String validateText){
        this.name = name;
        this. example = example;
        this.validateText = validateText;
        this.label = new Label(this.name);
        this.input = new TextField();
        this.invalid = new Label(this.validateText);
        input.setPromptText(this.example);
        invalid.setVisible(false);
        invalid.setTextFill(Color.RED);
    }

    @Override
    public void setInputFieldText(String text) {
        input.setText(text);
    }

    @Override
    public String getvalidateText() {
        return validateText;
    }


    @Override
    public Label getLabel(){
        return label;
    }

    @Override
    public Control getInputField(){
        return input;
    }

    @Override
    public Label getInvalidLabel(){
        return invalid;
    }

    @Override
    public String getInput(){
        return input.getText();
    }

}
