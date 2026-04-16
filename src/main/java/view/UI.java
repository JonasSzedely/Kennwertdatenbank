package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Controller;

import java.net.URL;


public class UI extends Application {
    @Override
    public void start(Stage primaryStage) {

        splash(primaryStage);

        primaryStage.setWidth(1600);
        primaryStage.setHeight(900);
        primaryStage.setMaximized(true);
        primaryStage.show();

        Controller controller = new Controller();
        new ProjectList(controller);

        VBox outerPane = new VBox();
        outerPane.setPadding(new Insets(5));

        HBox topPane = new TopPane(controller).get();
        HBox middlePane = new MiddlePane(controller).get();
        HBox bottomPane = new BottomPane().get();

        outerPane.getChildren().addAll(topPane, middlePane, bottomPane);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(outerPane, screenBounds.getWidth(), screenBounds.getHeight());

        URL cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS-Datei nicht gefunden!");
        }

        primaryStage.setScene(scene);
        StageFactory.setIcon(primaryStage);
        StageFactory.setName(primaryStage, "Kennwertdatenbank");
        primaryStage.setMaximized(true);

        if (!controller.isDatabaseAvailable()) {
            DatabaseWarning.show();
        }
    }

    /**
     * creates a Splashscreen
     *
     * @param primaryStage Stage object
     */
    private void splash(Stage primaryStage) {
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
