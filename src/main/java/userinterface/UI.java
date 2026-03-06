package userinterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kennwertdatenbank.Controller;

import java.net.URL;


public class UI extends Application {
    @Override
    public void start(Stage primaryStage) {
        StageFactory.setIcon(primaryStage);
        Controller controller = new Controller();
        new ProjectList(controller);

        VBox outerPane = new VBox();
        outerPane.setPadding(new Insets(5));
        outerPane.setAlignment(Pos.CENTER);

        HBox topPane = new TopPane(controller).get();
        HBox middlePane = new MiddlePane(controller).get();
        HBox bottomPane = new BottomPane().get();

        outerPane.getChildren().addAll(topPane, middlePane, bottomPane);

        Scene scene = new Scene(outerPane, 1000, 600);

        URL cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS-Datei nicht gefunden!");
        }
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);
        primaryStage.show();

        if (!controller.isDatabaseAvailable()) {
            showDatabaseWarning();
        }
    }

    /**
     * opens a new warning window that the DB couldn't be connected
     */
    private void showDatabaseWarning() {
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