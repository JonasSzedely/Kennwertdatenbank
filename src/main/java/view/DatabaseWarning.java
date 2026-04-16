package view;

import javafx.scene.control.Alert;

class DatabaseWarning {
    public static void show() {
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("Datenbankverbindung");
        warning.setHeaderText("Keine Datenbankverbindung");
        warning.setContentText(
                "Die Anwendung konnte keine Verbindung zur Datenbank herstellen.\n" +
                        "Das Programm läuft im Offline-Modus mit eingeschränkter Funktionalität.\n" +
                        "Bitte prüfen Sie:\n" +
                        "- Ist die Datenbank gestartet?\n" +
                        "- Sind die Zugangsdaten in den Einstellungen korrekt?\n" +
                        "- Ist die Netzwerkverbindung verfügbar?"
        );
        warning.getDialogPane().setMinWidth(500);
        warning.getDialogPane().setPrefWidth(500);
        warning.show();
    }
}