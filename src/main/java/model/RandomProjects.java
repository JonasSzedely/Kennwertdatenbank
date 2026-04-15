package model;

import java.util.Random;

public class RandomProjects {
    /**
     * Used to create random projects, it will use project cost from kv1-10 in resources
     * @param howMany set how many projects should be created.
     */
    public static void addRandomProjects(int howMany, Controller controller) {
        Random rand = new Random(123);
        int j = 1;
        for (int i = 0; i<howMany; i++){
            int num = 10001 + i;

            String pt = "Miete";
            if(i%2==0){
                pt = "Stockwerkeigentum";
            }

            String fassade = "AWD";
            if(i%2==0){
                fassade = "Hinterlüftet";
            }

            String window = "Kunststoff";
            if(i%2==0){
                window = "Holz";
            }

            String dach = "Flachdach";
            if(i%2==0){
                dach = "Steildach";
            }

            String heizung = "Erdsonde";
            if(i%2==0){
                heizung = "Pellet";
            }

            String kühlung = "FreeCooling";
            if(i%2==0){
                kühlung = "keine";
            }

            String lüftung = "KWL";
            if(i%2==0){
                lüftung = "keine";
            }

            String lüftungUG = "mechanisch";
            if(i%2==0){
                lüftungUG = "natürlich";
            }

            String cono = "Ja";
            if(i%2==0){
                cono = "Nein";
            }

            int s = rand.nextInt(9999)+1001;
            int w = rand.nextInt(50)+1;

            if (j == 10){
                j = 1;
            } else {
                j++;
            }

            System.out.println(controller.addProject(new Project(num,1,"Projektstrasse " + num, 8001 + i, "Zürich", "Besitzer"+i,
                    pt, "Neubau",31, 41, w, (int)(w*1.8), s, (int)(s*1.2), (int)((s*2.8)*0.4),
                    (int)((s*2.8)*0.6), (int) (s*0.8), (int)(s*0.3),fassade, window, dach,
                    heizung, kühlung, lüftung,lüftungUG, cono,
                    "nichts spezielles",new ProjectData("C:\\Users\\Jonas\\Nextcloud\\Jonas\\07_Programmieren\\Java\\Kennwertdatenbank\\src\\main\\resources\\kv" + j + ".csv"))));
        }
    }
}
