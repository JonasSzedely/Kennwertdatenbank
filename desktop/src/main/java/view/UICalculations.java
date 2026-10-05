package view;

import javafx.collections.ListChangeListener;
import model.Project;
import model.ProjectValues;

public final class UICalculations {
    private static int minTotalCost;
    private static int maxTotalCost;
    private static int minApartments;
    private static int maxApartments;
    private static double averageRatioUG;
    private static int averageWindowRatio;
    private static int minVolume;
    private static int maxVolume;

    public UICalculations(){
        reset();
        calculate();
        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            reset();
            calculate();
        });
    }

    private void calculate(){
        if (ProjectList.getProjectList().isEmpty()) {
            System.out.println("Keine Projekte zum Berechnen vorhanden");
            return;
        }

        for (Project project : ProjectList.getProjectList()) {
            int cost = project.getData().getTotalCost();
            int apartments = project.get(ProjectValues.APARTMENTS_NR);
            int volumeUG = project.get(ProjectValues.VOLUME_UNDERGROUND);
            int volumeOG = project.get(ProjectValues.VOLUME_ABOVE_GROUND);
            int volume = volumeUG + volumeOG;
            int windowArea = project.get(ProjectValues.WINDOW_AREA);
            int facadeArea = project.get(ProjectValues.FACADE_AREA);

            averageRatioUG += (double) volumeUG / volumeOG;
            minTotalCost = Math.min(cost, minTotalCost);
            maxTotalCost = Math.max(cost, maxTotalCost);
            minApartments = Math.min(apartments, minApartments);
            maxApartments = Math.max(apartments, maxApartments);
            averageWindowRatio += (int) (((double) windowArea / (double) facadeArea) * 100);
            minVolume = Math.min(volume, minVolume);
            maxVolume = Math.max(volume, maxVolume);
        }
        int size = ProjectList.getProjectList().size();
        averageRatioUG /= size;
        averageWindowRatio /= size;
    }

    public static int getMinVolume() {
        return minVolume;
    }

    public static int getMaxVolume() {
        return maxVolume;
    }

    public static int getMinTotalCost() {
        return minTotalCost;
    }

    public static int getMaxTotalCost() {
        return maxTotalCost;
    }

    public static int getMinApartments() {
        return minApartments;
    }

    public static int getMaxApartments() {
        return maxApartments;
    }

    public static double getAverageRatioUG() {
        return averageRatioUG;
    }

    public static int getAverageWindowRatio() {
        return averageWindowRatio;
    }

    private void reset(){
        minTotalCost = Integer.MAX_VALUE;
        maxTotalCost = 0;
        minApartments = Integer.MAX_VALUE;
        maxApartments = 0;
        averageRatioUG = 0;
        averageWindowRatio = 0;
        minVolume = Integer.MAX_VALUE;
        maxVolume = 0;
    }

}
