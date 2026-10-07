package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Holds the costs of a project per BKP code.
 * <p>
 * Costs are stored in CHF and ordered hierarchically by {@link BKPComparator}.
 * Each code contains the total of its sub-codes, so for example BKP 25 already
 * includes BKP 250 and BKP 2501.
 */
public class ProjectData {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectData.class);

    private TreeMap<Integer, Integer> map = new TreeMap<>(new BKPComparator());

    /**
     * Creates empty cost data.
     */
    public ProjectData() {
    }

    /**
     * Imports costs from a CSV file and adds them to the existing data.
     * <p>
     * Expected format per line: {@code BKP code;amount in CHF}, separated by semicolons.
     * Lines before the first entry starting with {@code 1} are skipped.
     * Quotes, dots, commas and apostrophes are removed from both columns,
     * so {@code 250.1} becomes {@code 2501}. If a code occurs twice, the second
     * value is stored under the code multiplied by 10, as long as that code is free
     * and below 100'000.
     *
     * @param filePath the path to the CSV file, surrounding quotes are ignored
     * @throws UncheckedIOException if the file cannot be read
     */
    public void set(String filePath) {
        loadFromCsv(filePath);
    }

    /**
     * Replaces the cost data with the given map.
     * <p>
     * The map is used directly, not copied. It should be ordered by {@link BKPComparator}.
     *
     * @param map the costs in CHF per BKP code
     */
    public void set(TreeMap<Integer, Integer> map) {
        this.map = map;
    }

    /**
     * Reads the CSV file and adds all valid entries to the map.
     *
     * @param path the path to the CSV file
     * @throws UncheckedIOException if the file cannot be read
     */
    private void loadFromCsv(String path) {
        String filePath = path.replace("\"", "");
        LOG.info("Reading cost file: {}", filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            reader.lines()
                    .dropWhile(line -> !line.startsWith("1") && !line.startsWith("\"1\""))
                    .filter(line -> !line.isEmpty() && !line.startsWith("\"\""))
                    .map(this::parseLine)
                    .flatMap(Optional::stream)
                    .forEach(this::put);
        } catch (IOException | UncheckedIOException e) {
            LOG.error("Error reading cost file: {}", filePath, e);
            throw e instanceof UncheckedIOException u ? u : new UncheckedIOException((IOException) e);
        }
    }

    /**
     * Parses a single CSV line into a BKP code and its amount.
     *
     * @param line the CSV line
     * @return the parsed entry, or empty if the line has no valid code
     */
    private Optional<BkpEntry> parseLine(String line) {
        String[] components = line.split(";");
        if (components.length < 2) {
            return Optional.empty();
        }

        String rawKey = clean(components[0]);
        String rawValue = clean(components[1]);
        if (rawKey.isBlank()) {
            return Optional.empty();
        }

        try {
            int code = Integer.parseInt(rawKey);
            int amount = rawValue.isBlank() ? 0 : Integer.parseInt(rawValue);
            return Optional.of(new BkpEntry(code, amount));
        } catch (NumberFormatException e) {
            LOG.warn("Skipped invalid line: {}", line);
            return Optional.empty();
        }
    }

    /**
     * Removes quotes, dots, commas and apostrophes.
     *
     * @param value the raw CSV value
     * @return the cleaned value
     */
    private static String clean(String value) {
        return value.replaceAll("[\",.']", "");
    }

    /**
     * Stores an entry. If the code already exists, the entry is stored under
     * the code multiplied by 10, as long as that code is free and below 100'000.
     *
     * @param entry the entry to store
     */
    private void put(BkpEntry entry) {
        int code = entry.code();
        if (!map.containsKey(code)) {
            map.put(code, entry.amount());
        } else if (!map.containsKey(code * 10) && code * 10 < 100_000) {
            map.put(code * 10, entry.amount());
        }
    }

    /**
     * A single cost entry read from the CSV file.
     *
     * @param code   the BKP code
     * @param amount the cost in CHF
     */
    private record BkpEntry(int code, int amount) {
    }

    /**
     * Returns the costs per BKP code.
     * <p>
     * The returned map is the internal map, changes affect this object.
     *
     * @return the costs in CHF per BKP code
     */
    public TreeMap<Integer, Integer> getData() {
        return map;
    }

    /**
     * Returns the total cost of the project.
     * <p>
     * Only the single-digit BKP codes 0 to 9 are summed, since they already
     * contain all sub-codes.
     *
     * @return the total cost in CHF
     */
    public int getTotalCost() {
        int total = 0;
        for (int i = 0; i < 10; i++) {
            total += getBKP(i);
        }
        return total;
    }

    /**
     * Returns the cost of a single BKP code.
     *
     * @param bkp the BKP code
     * @return the cost in CHF, or {@code 0} if the code does not exist
     */
    public int getBKP(int bkp) {
        return map.getOrDefault(bkp, 0);
    }
}