package kennwertdatenbank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * Projekt BKPs einlesen
 * 1. Im Messerli BauAD mit der Vorlage "Datenbank" exportieren
 * 2. Excel als >> CSV (durch Trennzeichen getrennt) (*.csv) << abspeichern.
 * 3. Dateipfad der neuen .csv Datei kopieren und im Programm einfügen.
 * code used from: https://www.w3schools.com/java/java_bufferedreader.asp
 */

public class ProjectData {
    private TreeMap<Integer, Integer> map = new TreeMap<>(new BKPComparator());

    public ProjectData (String filePath) {
        loadFromCsv(filePath);
    }

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
                int key = Integer.parseInt(components[0].replace(",", ""));
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

    public TreeMap<Integer,Integer> getData(){
        return map;
    }


    public int getTotalCost() {
        int total = 0;
        for (int i = 0; i < 10; i++){
            if(map.containsKey(i)){
                total += map.get(i);
            }
        }
        return total;
    }

    public int getBKP(int bkp){
        if(map.containsKey(bkp)){
            return map.get(bkp);
        }
        return 0;
    }
}