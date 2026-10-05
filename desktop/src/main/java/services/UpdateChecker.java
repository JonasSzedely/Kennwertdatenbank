package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Prüft über die GitHub-API, ob eine neuere Version der Anwendung
 * als Release verfügbar ist.
 */
public class UpdateChecker {

    private static final String API_URL =
            "https://api.github.com/repos/JonasSzedely/Kennwertdatenbank/releases/latest";

    public record UpdateInfo(String latestVersion, String downloadUrl, String releaseUrl) {}

    /**
     * Liefert UpdateInfo nur, wenn eine neuere Version mit .exe-Asset existiert.
     * Einziger Einstiegspunkt für den Update-Check.
     */
    public static Optional<UpdateInfo> findAvailableUpdate() throws Exception {
        UpdateInfo info = fetchLatestRelease();
        String current = SoftwareVersion.get();

        if (info.downloadUrl() != null && isNewer(info.latestVersion(), current)) {
            return Optional.of(info);
        }
        return Optional.empty();
    }

    /** Holt das neueste Release von der GitHub-API. */
    private static UpdateInfo fetchLatestRelease() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = new ObjectMapper().readTree(response.body());

        String latest = json.get("tag_name").asText().replaceFirst("^v", "");
        String exeUrl = null;
        for (JsonNode asset : json.get("assets")) {
            if (asset.get("name").asText().endsWith(".exe")) {
                exeUrl = asset.get("browser_download_url").asText();
                break;
            }
        }
        return new UpdateInfo(latest, exeUrl, json.get("html_url").asText());
    }

    /** Vergleicht z.B. "1.1.22" mit "1.2.0" (true, wenn latest neuer als current ist). */
    static boolean isNewer(String latest, String current) {
        String[] l = latest.split("\\."), c = current.split("\\.");
        for (int i = 0; i < Math.max(l.length, c.length); i++) {
            int lv = i < l.length ? Integer.parseInt(l[i]) : 0;
            int cv = i < c.length ? Integer.parseInt(c[i]) : 0;
            if (lv != cv) return lv > cv;
        }
        return false;
    }
}