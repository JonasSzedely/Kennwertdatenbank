package services;

import javafx.application.HostServices;
import javafx.application.Platform;
import model.AppLogger;
import view.UpdateDialog;

/**
 * Startet den Update-Check im Hintergrund und zeigt bei
 * verfügbarem Update den Dialog auf dem FX-Thread an.
 */
public class UpdateService {

    public static void checkForUpdatesAsync(HostServices hostServices) {
        Thread.ofVirtual().start(() -> {
            try {
                UpdateChecker.findAvailableUpdate().ifPresent(update ->
                        Platform.runLater(() ->
                                UpdateDialog.show(update, SoftwareVersion.get(), hostServices))
                );
            } catch (Exception e) {
                // Kein Internet / Rate-Limit soll den App-Start nicht stören
                AppLogger.error("Update-Check fehlgeschlagen: " + e.getMessage());
            }
        });
    }
}