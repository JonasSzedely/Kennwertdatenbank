package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Checks via the GitHub API whether a newer release of the application is available.
 */
public final class UpdateChecker {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateChecker.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String API_URL =
            "https://api.github.com/repos/JonasSzedely/Kennwertdatenbank/releases/latest";

    private UpdateChecker() {
    }

    /**
     * Information about an available release.
     *
     * @param latestVersion the version of the release, for example {@code "2.1.0"}
     * @param downloadUrl   the download link of the {@code .exe} installer, or {@code null} if none exists
     * @param releaseUrl    the link to the release page on GitHub
     */
    public record UpdateInfo(String latestVersion, String downloadUrl, String releaseUrl) {
    }

    /**
     * Returns the latest release if it is newer than the running version and
     * provides an {@code .exe} installer.
     * <p>
     * No update is reported if GitHub is not reachable, returns an error or
     * a version number cannot be compared.
     *
     * @return the available update, or empty if there is none
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public static Optional<UpdateInfo> findAvailableUpdate() throws IOException, InterruptedException {
        Optional<UpdateInfo> latest = fetchLatestRelease();
        if (latest.isEmpty()) {
            return Optional.empty();
        }

        UpdateInfo info = latest.get();
        String current = SoftwareVersion.get();

        if (info.downloadUrl() != null && isNewer(info.latestVersion(), current)) {
            LOG.info("Update available: {} (installed: {})", info.latestVersion(), current);
            return Optional.of(info);
        }
        return Optional.empty();
    }

    /**
     * Fetches the latest release from the GitHub API.
     *
     * @return the release information, or empty if GitHub returns an error
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    private static Optional<UpdateInfo> fetchLatestRelease() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(TIMEOUT)
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            LOG.warn("Update check failed, GitHub returned status {}", response.statusCode());
            return Optional.empty();
        }

        JsonNode json = OBJECT_MAPPER.readTree(response.body());
        String latest = json.path("tag_name").asText().replaceFirst("^v", "");

        String exeUrl = null;
        for (JsonNode asset : json.path("assets")) {
            if (asset.path("name").asText().endsWith(".exe")) {
                exeUrl = asset.path("browser_download_url").asText(null);
                break;
            }
        }
        return Optional.of(new UpdateInfo(latest, exeUrl, json.path("html_url").asText(null)));
    }

    /**
     * Compares two version numbers part by part, for example {@code "1.1.22"} and {@code "1.2.0"}.
     * <p>
     * Missing parts count as zero, so {@code "2.1"} equals {@code "2.1.0"}.
     *
     * @param latest  the version of the release
     * @param current the version of the running application
     * @return {@code true} if {@code latest} is newer than {@code current},
     *         {@code false} if not or if a version cannot be parsed
     */
    static boolean isNewer(String latest, String current) {
        int[] l = parse(latest);
        int[] c = parse(current);
        if (l == null || c == null) {
            LOG.debug("Cannot compare versions '{}' and '{}'", latest, current);
            return false;
        }

        for (int i = 0; i < Math.max(l.length, c.length); i++) {
            int lv = i < l.length ? l[i] : 0;
            int cv = i < c.length ? c[i] : 0;
            if (lv != cv) {
                return lv > cv;
            }
        }
        return false;
    }

    /**
     * Splits a version number into its numeric parts.
     *
     * @param version the version, for example {@code "2.0.2"}
     * @return the numeric parts, or {@code null} if a part is not a number
     */
    private static int[] parse(String version) {
        String[] parts = version.split("\\.");
        int[] numbers = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                numbers[i] = Integer.parseInt(parts[i]);
            }
            return numbers;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}