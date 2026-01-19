package userinterface;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

public interface Form {
    Label getLabel();
    Control getInputField();
    Label getInvalidLabel();
}
