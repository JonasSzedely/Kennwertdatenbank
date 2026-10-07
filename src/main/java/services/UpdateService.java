package services;

import javafx.application.HostServices;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import view.UpdateDialog;

import java.io.IOException;

/**
 * Runs the update check in the background and shows a dialog if an update is available.
 */
public final class UpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateService.class);

    private UpdateService() {
    }

    /**
     * Starts the update check on a virtual thread and returns immediately.
     * <p>
     * If a newer version is available, {@link UpdateDialog} is shown on the
     * JavaFX application thread. Errors are logged and not shown to the user.
     *
     * @param hostServices used to open the download link in the browser
     */
    public static void checkForUpdatesAsync(HostServices hostServices) {
        Thread.ofVirtual().name("update-check").start(() -> {
            try {
                UpdateChecker.findAvailableUpdate().ifPresent(update ->
                        Platform.runLater(() ->
                                UpdateDialog.show(update, SoftwareVersion.get(), hostServices)));
            } catch (IOException e) {
                LOG.warn("Update check failed, GitHub not reachable: {}", e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                LOG.error("Update check failed", e);
            }
        });
    }
}