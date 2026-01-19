package userinterface;

public class NumberValidator {

    InputForm form;
    int minValue;
    int maxValue;


    public NumberValidator(InputForm form, int minValue, int maxValue){
        this.form = form;
        this. minValue = minValue;
        this. maxValue = maxValue;
    }

    public boolean isValid(){
        int num = 0;
        try {
            num = Integer.valueOf(form.getInput());
        } catch (NumberFormatException e){
            return false;
        }
        if (num < minValue){
            return false;
        }
        if (num > maxValue) {
            return false;
        }
        return true;
    }
}
