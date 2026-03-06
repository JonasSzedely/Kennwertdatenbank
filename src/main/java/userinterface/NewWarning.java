package userinterface;

import javafx.scene.control.Alert;

public class NewWarning{
    public static void show(String error){
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("Fehler");
        warning.setContentText(error);
        warning.getDialogPane().setMinWidth(500);
        warning.getDialogPane().setPrefWidth(500);
        warning.show();
    }
}
