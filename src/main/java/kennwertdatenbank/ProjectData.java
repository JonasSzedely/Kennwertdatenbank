package kennwertdatenbank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

/**
 * Projekt BKPs einlesen
 * 1. Im Messerli BauAD mit der Vorlage "Datenbank" exportieren
 * 2. Excel als >> CSV (durch Trennzeichen getrennt) (*.csv) << abspeichern.
 * 3. Dateipfad der neuen .csv Datei kopieren und im Programm einfügen.
 * code used from: https://www.w3schools.com/java/java_bufferedreader.asp
 */

public class ProjectData {
    private final TreeMap<Integer, Double> map = new TreeMap<>(new BKPComparator());

    public ProjectData (String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));

            System.out.println("Reading File: " + path);
            String line = reader.readLine();

            while (line != null){
                String[] components = line.split(";");
                //ToDo: import auf korrekten Inhalt überprüfen
                int key = Integer.parseInt((components[0].replace(",", ""))) ;
                double value = Double.parseDouble(components[1].replace(",", "."));
                if (!map.containsKey(key)){
                    map.put(key, value);
                } else if(!map.containsKey(key*10) && key*10 < 100_000){
                    map.put(key*10, value);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Failed to process file: " + e.getMessage());
            System.err.println("Please check that the file exists and you have permission to access it.");
        }

        //map.forEach((key, value) -> System.out.println("BKP: " + key + " Fr.: " + value)); //for testing

        //System.out.println("Max Value: " + getMaxValue()); //for testing
    }

    public TreeMap<Integer,Double> getData(){
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

    public double getBKP(int bkp){
        if(map.containsKey(bkp)){
            return map.get(bkp);
        }
        return -1;
    }
}