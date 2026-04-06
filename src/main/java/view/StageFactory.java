package view;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

class StageFactory {
    private static final String LOGO_PATH = "/software_logo.png";

    public static Stage createStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        setIcon(stage);
        return stage;
    }

    public static void setIcon(Stage stage) {
        try {
            stage.getIcons().clear();
            stage.getIcons().add(
                    new Image(Objects.requireNonNull(
                            StageFactory.class.getResourceAsStream(LOGO_PATH)
                    ))
            );
        } catch (Exception e) {
            System.out.println("Hinweis: Logo nicht gefunden, verwende Standard-Icon");
        }
    }

    public static void setName(Stage stage, String name){
        stage.titleProperty().set(name);
    }
}