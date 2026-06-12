package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.AppLogger;
import services.KWDControllerService;
import services.UpdateService;
import view.bottompane.BottomPane;
import view.middlepane.MiddlePane;
import view.toppane.TopPane;

import java.net.URL;

public class UI extends Application {
    KWDControllerService service;

    @Override
    public void start(Stage primaryStage) {

        new SplashScreen().start(primaryStage);

        primaryStage.setWidth(1600);
        primaryStage.setHeight(900);
        primaryStage.setMaximized(true);
        primaryStage.show();

        try {
            service = new KWDControllerService();
            new ProjectList(service);
            new UICalculations();

            VBox outerPane = new VBox();
            outerPane.setPadding(new Insets(5));

            HBox topPane = new TopPane(service, service).get();
            HBox middlePane = new MiddlePane(service).get();
            HBox bottomPane = new BottomPane(service).get();

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

            if (!service.isDBAvailable()) {
                DatabaseWarning.show();
            }

        } catch (Exception e) {
            AppLogger.error(e.getMessage());
        }

        service.onDbAvailableChanged(o -> {
            if (!service.isDBAvailable()) {
                DatabaseWarning.show();
            }
        });

        service.onDbChanged(o -> {
            if (service.isDBAvailable()) {
                ProjectList.refreshProjectList();
            }
        });

        UpdateService.checkForUpdatesAsync(getHostServices());
    }
}
