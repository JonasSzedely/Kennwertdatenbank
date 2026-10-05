package view;

import javafx.application.HostServices;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import services.UpdateChecker;

/**
 * Dialog, der den Nutzer über eine neue Version informiert und
 * den Download im Browser öffnet.
 */
public class UpdateDialog {

    public static void show(UpdateChecker.UpdateInfo update, String current, HostServices hostServices) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update verfügbar");
        alert.setHeaderText("Version " + update.latestVersion() + " ist verfügbar");
        alert.setContentText("Installierte Version: " + current
                + "\nMöchtest du die neue Version herunterladen?");

        ButtonType download = new ButtonType("Herunterladen");
        alert.getButtonTypes().setAll(download, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(result -> {
            if (result == download) {
                hostServices.showDocument(update.downloadUrl());
            }
        });
    }
}
