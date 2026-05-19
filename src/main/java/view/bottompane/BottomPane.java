package view.bottompane;

import api.DataService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class BottomPane {

    DataService dataService;
    public BottomPane(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * creates the bottom pane
     *
     * @return HBox
     */
    public HBox get() {
        HBox bottomPane = new HBox();
        bottomPane.setMinHeight(30);
        bottomPane.setPadding(new Insets(0, 20, 0, 20));
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottom-pane");
        bottomPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 1 1;");

        Label version = new Label("Version: " + SoftwareVersion.get());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dbStatus = new Label();

        if(dataService.isDBAvailable()){
            dbStatus.textProperty().set("● Datenbank verbunden");
            dbStatus.styleProperty().set("-fx-text-fill: #00c853;");
        } else {
            dbStatus.textProperty().set("● Keine Datenbankverbindung");
            dbStatus.styleProperty().set("-fx-text-fill: #ff1744;");
        }

        dataService.onDbAvailableChanged( e -> {
            if(dataService.isDBAvailable()){
                dbStatus.textProperty().set("● Datenbank verbunden");
                dbStatus.styleProperty().set("-fx-text-fill: #00c853;");
            } else {
                dbStatus.textProperty().set("● Keine Datenbankverbindung");
                dbStatus.styleProperty().set("-fx-text-fill: #ff1744;");
            }
        });

        Region spacer2 = new Region();
        spacer2.setMaxWidth(20);
        spacer2.setPrefWidth(20);

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

        bottomPane.getChildren().addAll(version, spacer, dbStatus, spacer2, dateTimeLabel);
        return bottomPane;
    }
}
