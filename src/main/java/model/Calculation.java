package model;

public class Calculation {
    String name;
    String calculation;
    public Calculation(String name, String calculation) {
        this.name = name;
        this.calculation = calculation;
    }

    public static final Calculation SEPARATOR = new Calculation("", "");

    public String getName(){
        return name;
    }

    public String getCalculation() {
        return calculation;
    }
}