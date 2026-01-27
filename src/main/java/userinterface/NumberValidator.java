package userinterface;

public class NumberValidator {

    Form form;
    double minValue;
    double maxValue;
    String input;


    public NumberValidator(Form form, double minValue, double maxValue, String input){
        this.form = form;
        this. minValue = minValue;
        this. maxValue = maxValue;
        this.input = input;
    }

    public boolean isValid(){
        double num = 0;
        try {
            num = Double.valueOf(input);
        } catch (NumberFormatException e){
            System.out.println(e);
            return false;
        }
        if (num < minValue){
            return false;
        }else if (num > maxValue) {
            return false;
        } else {
            return true;
        }
    }
}
