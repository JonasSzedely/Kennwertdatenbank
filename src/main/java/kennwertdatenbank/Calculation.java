package kennwertdatenbank;

public class Calculation {
    String name;
    String calculation;
    public Calculation(String name, String calculation) {
        this.name = name;
        this.calculation = calculation;
    }

    public String getName(){
        return name;
    }

    public String getCalculation() {
        return calculation;
    }
}