package kennwertdatenbank;

public class Calculation {
    String name;
    int calculation;
    public Calculation(String name, int calculation) {
        this.name = name;
        this.calculation = calculation;
    }

    public String getName(){
        return name;
    }

    public int getCalculation() {
        return calculation;
    }
}