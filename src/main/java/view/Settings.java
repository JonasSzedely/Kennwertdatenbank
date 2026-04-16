package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Controller;
import model.DBConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

class Settings {
    private final Controller controller;
    private boolean setButtonUsed = false;

    public Settings(Controller controller) {
        this.controller = controller;
    }

    public void start(Stage stage) {
        VBox outerPane = new VBox();
        outerPane.setAlignment(Pos.CENTER);
        String path = DBConfig.getPropertiesPath();

        Label dbURLLabel = new Label("Datenbank URL");
        TextField dbURLInput = new TextField();
        dbURLInput.setText(getValue(path, "db.url"));

        Label dbUsernameLabel = new Label("Benutzername");
        TextField dbUsernameInput = new TextField();
        dbUsernameInput.setText(getValue(path, "db.username"));

        Label dbPasswordLabel = new Label("Passwort");
        PasswordField dbPasswordInput = new PasswordField();
        String dbPassword = getValue(path, "db.password");
        dbPasswordInput.setText(dbPassword);
        AtomicReference<String> newPassword = new AtomicReference<>(dbPassword);

        dbPasswordInput.textProperty().addListener((observable, oldValue, newValue) -> newPassword.set(newValue));

        Button setSettingsButton = new Button("Einstellungen speichern");
        setSettingsButton.setOnAction(event -> {
            boolean dbPath = setValue(path, "db.url", dbURLInput.getText());
            boolean dbUser = setValue(path, "db.username", dbUsernameInput.getText());
            boolean dbPass = setValue(path, "db.password", String.valueOf(newPassword));
            Alert settingsSetConfirmation = new Alert(Alert.AlertType.INFORMATION);
            settingsSetConfirmation.setTitle("Einstellungen");
            settingsSetConfirmation.setHeaderText(null);
            if (dbPath && dbUser && dbPass) {
                settingsSetConfirmation.setContentText("Erfolgreich angepasst.");
            } else {
                settingsSetConfirmation.setContentText("Einstellungen konnten nicht angepasst werden.");
            }
            settingsSetConfirmation.showAndWait();
            setButtonUsed = controller.initializeDatabase();
            stage.close();
        });

        Button tryDBConnectionButton = new Button("DB-Verbindung testen");
        tryDBConnectionButton.setOnAction(event -> {
            boolean dbPath = setValue(path, "db.url", dbURLInput.getText());
            boolean dbUser = setValue(path, "db.username", dbUsernameInput.getText());
            boolean dbPass = setValue(path, "db.password", String.valueOf(newPassword));
            if (dbPath && dbUser && dbPass && controller.testDBConnection()) {
                tryDBConnectionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                tryDBConnectionButton.setText("Verbindung erfolgreich");
            } else {
                tryDBConnectionButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                tryDBConnectionButton.setText("Verbindung nicht möglich");
            }
        });

        GridPane gridPane = new GridPane(10, 10);
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(dbURLLabel, 0, 0);
        gridPane.add(dbURLInput, 1, 0);
        gridPane.add(dbUsernameLabel, 0, 1);
        gridPane.add(dbUsernameInput, 1, 1);
        gridPane.add(dbPasswordLabel, 0, 2);
        gridPane.add(dbPasswordInput, 1, 2);

        tryDBConnectionButton.prefWidthProperty().bind(gridPane.widthProperty());
        gridPane.add(tryDBConnectionButton, 0, 3);
        GridPane.setColumnSpan(tryDBConnectionButton, 2);

        setSettingsButton.prefWidthProperty().bind(gridPane.widthProperty());
        gridPane.add(setSettingsButton, 0, 4);
        GridPane.setColumnSpan(setSettingsButton, 2);

        gridPane.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(200));

        outerPane.getChildren().addAll(gridPane);

        Scene scene = new Scene(outerPane);

        stage.setScene(scene);
        stage.setMinWidth(350);
        stage.setMinHeight(150);
        stage.showAndWait();
    }

    // Source - https://stackoverflow.com/a/62851268
    public String getValue(String path, String key) {
        String value = null;
        try {
            Properties prop = new Properties();
            File file = new File(path);
            if (file.exists()) {
                prop.load(new FileInputStream(file));
                value = prop.getProperty(key);
            }
        } catch (Exception e) {
            return ("Einstellungen konnten nicht geladen werden.");
        }
        return value;
    }

    // Source - https://stackoverflow.com/a/62851268
    public boolean setValue(String path, String key, String value) {
        Properties props = new Properties();
        File f = new File(path);
        try {
            final FileInputStream configStream = new FileInputStream(f);
            props.load(configStream);
            configStream.close();
            props.setProperty(key, value);
            final FileOutputStream output = new FileOutputStream(f);
            props.store(output, "");
            output.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean isSetButtonUsed() {
        return setButtonUsed;
    }
}