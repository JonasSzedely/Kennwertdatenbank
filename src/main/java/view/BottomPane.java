package view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

    BottomPane(){

    }
    /**
     * creates the bottom pane
     *
     * @return HBox
     */
    HBox get() {
        HBox bottomPane = new HBox();
        bottomPane.setMinHeight(30);
        bottomPane.setPadding(new Insets(0, 20, 0, 20));
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottom-pane");
        bottomPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 1 1;");

        Label version = new Label(SoftwareVersion.get());
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
