package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SplashScreen {

    /**
     * creates a Splashscreen
     *
     * @param primaryStage Stage object
     */
    public void start(Stage primaryStage) {
        VBox outerPane = new VBox();

        outerPane.setAlignment(Pos.CENTER);
        outerPane.setPadding(new Insets(20));
        outerPane.setMinHeight(150);
        outerPane.setStyle("-fx-background-color: #052048;");
        Label titelLabel = new Label("Kennwert\n    Datenbank");
        titelLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        outerPane.getChildren().add(titelLabel);

        Scene scene = new Scene(outerPane);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double fontSize = newVal.doubleValue() / 10;
            titelLabel.setStyle(String.format("-fx-font-size: %.0fpx;-fx-font-weight: bold; -fx-text-fill: white;", fontSize));
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double fontSize = newVal.doubleValue() / 10;
            titelLabel.setStyle(String.format("-fx-font-size: %.0fpx;-fx-font-weight: bold; -fx-text-fill: white;", fontSize));
        });

        primaryStage.setScene(scene);
        StageFactory.setIcon(primaryStage);
        StageFactory.setName(primaryStage, "Kennwertdatenbank");
    }
}
