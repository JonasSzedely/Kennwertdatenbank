package userinterface;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import kennwertdatenbank.Controller;

import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class UI extends Application {
    private Controller controller;

    @Override
    public void start(Stage primaryStage) {

        StageFactory.setIcon(primaryStage);
        controller = new Controller();
        new ProjectList(controller);

        VBox outerPane = new VBox();
        outerPane.setPadding(new Insets(5));
        outerPane.setAlignment(Pos.CENTER);

        HBox topPane = new TopPane(controller).get();
        HBox middlePane = new MiddlePane(controller).get();
        HBox bottomPane = bottomPane();

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

    /**
     * creates the bottom pane
     *
     * @return HBox
     */
    private HBox bottomPane() {
        HBox bottomPane = new HBox();
        bottomPane.setMinHeight(30);
        bottomPane.setPadding(new Insets(0, 20, 0, 20));
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottom-pane");
        bottomPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 1 1;");

        Label version = new Label("Version: 1.1.1");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateTimeLabel = new Label();
        dateTimeLabel.setAlignment(Pos.CENTER);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        //check every second
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> dateTimeLabel.setText(ZonedDateTime.now().format(formatter)))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        dateTimeLabel.setText(ZonedDateTime.now().format(formatter));

        bottomPane.getChildren().addAll(version, spacer, dateTimeLabel);
        return bottomPane;
    }

}