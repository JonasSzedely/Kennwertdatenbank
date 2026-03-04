package kennwertdatenbank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    private void loadFromCsv(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            System.out.println("Reading File: " + filePath);
            String line = reader.readLine();

            while (line != null) {
                String[] components = line.split(";");
                int key = Integer.parseInt(components[0].replaceAll("[,.]", ""));
                int value = Integer.parseInt(components[1]);

                if (!map.containsKey(key)) {
                    map.put(key, value);
                } else if (!map.containsKey(key * 10) && key * 10 < 100_000) {
                    map.put(key * 10, value);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Failed to process file: " + e.getMessage());
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