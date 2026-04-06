package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;



public class ProjectData {
    private TreeMap<Integer, Integer> map = new TreeMap<>(new BKPComparator());

    /**
     * Imports Project BKP numbers from a CSV
     * Formatting: BKP-number;valueInCHF
     */
    public ProjectData (String filePath) {
        loadFromCsv(filePath);
    }

    /**
     * Imports Project BKP numbers from a TreeMap<Integer, Integer>
     * Formatting: BKP-number;valueInCHF
     */
    public ProjectData(TreeMap<Integer,Integer> map){
        this.map = map;
    }

    private void loadFromCsv(String path) {
        String filePath = path.replaceAll("\"", "");
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))){
            System.out.println("Reading File: " + filePath);
            String line = reader.readLine();

            while(line != null && !line.startsWith("\"1\"") && !line.startsWith("1")){
                line = reader.readLine();
            }

            while (line != null) {
                if(!line.isEmpty() && !line.startsWith("\"\"")) {
                    String[] components = line.split(";");
                    String rawKey;
                    String rawValue;

                    if (components.length < 2) {
                        line = reader.readLine();
                        continue;
                    } else {
                        rawKey = components[0].replaceAll("[\",.']", "");
                        rawValue = components[1].replaceAll("[\",.']", "");
                    }

                    if (rawKey.isBlank()) {
                        line = reader.readLine();
                        continue;
                    } else if (rawValue.isBlank()) {
                        rawValue = "0";
                    }

                    try {
                        int key = Integer.parseInt(rawKey);
                        int value = Integer.parseInt(rawValue);

                        if (!map.containsKey(key)) {
                            map.put(key, value);
                        } else if (!map.containsKey(key * 10) && key * 10 < 100_000) {
                            map.put(key * 10, value);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Zeile übersprungen: " + line);
                    }
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            System.out.println("Fehler beim ladender csv Datei: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Project BKP numbers as a TreeMap<Integer, Integer>
     *     Formatting: BKP-number;valueInCHF
     */
    public TreeMap<Integer,Integer> getData(){
        return map;
    }

    /**
     *
     * @return returns the total cost in CHF for the project, only counts single-digit BKP figures
     */
    public int getTotalCost() {
        int total = 0;
        for (int i = 0; i < 10; i++){
            if(map.containsKey(i)){
                total += map.get(i);
            }
        }
        return total;
    }

    /**
     * calculates the cost for all BKP figures between two BKP numbers
     * @param bkpStart first BKP (included)
     * @param bkpEnd last BKP (included)
     * @return total cost in CHF
     */
    public int getRange(int bkpStart, int bkpEnd){
        int sum = 0;
        for(int val : map.subMap(bkpStart, true, bkpEnd, true).values()){
            sum += val;
        }
        return sum;
    }

    /**
     * returns the cost for a specific BKP number
     * @param bkp the BKP number
     * @return cost in CHF
     */
    public int getBKP(int bkp){
        if(map.containsKey(bkp)){
            return map.get(bkp);
        }
        return 0;
    }
}