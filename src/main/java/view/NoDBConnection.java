package view;

import javafx.scene.control.Alert;

public class NoDBConnection {
    public static void show() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Keine Datenbankverbindung");
        error.setContentText("Die Funktion kann nur bei aktiver Datenbankverbindung verwendet werden.");
        error.show();
    }
}
