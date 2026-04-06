package view;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

interface Form {
    Label getLabel();

    Control getInputField();

    Label getInvalidLabel();

    String getInput();

    void setInputFieldText(String text);

    String getValidateText();
}
