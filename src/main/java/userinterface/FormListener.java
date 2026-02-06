package userinterface;

public class FormListener {
    String name;
    Form form;
    boolean isValid = false;
    private int min = 0;
    private int max = 0;
    String type;

    public FormListener(InputForm form, String name, int min, int max){
        this.name = name;
        this.form = form;
        this.min = min;
        this.max = max;
        this.type ="number";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                validate();
            }
        });
    }

    public FormListener(DropdownForm dropdown, String name){
        this.name = name;
        this.form = dropdown;
        this.type ="dropdwon";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                validate();
            }
        });
    }

    public FormListener(InputForm form, String name){
        this.name = name;
        this.form = form;
        this.max = Integer.MAX_VALUE;
        this.type ="text";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                validate();
            }
        });
    }

    public FormListener(InputForm form, String name, int max){
        this.name = name;
        this.form = form;
        this.max = max;
        this.type ="text";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                validate();
            }
        });
    }

    public void validate() {
        if (type.equals("number")) {
            var checker = new NumberValidator(form, min, max, form.getInput());
            isValid = checker.isValid();
        } else if (type.equals("dropdown")) {
            isValid = !(form.getInput() == null);
        } else {
            isValid = !(form.getInput().isEmpty()) && form.getInput().length() <= max;
            if(form.getInput().length() > max){
                form.getInvalidLabel().setText("Maximal " + max + " Zeichen erlaubt!");
            } else {
                form.getInvalidLabel().setText(form.getvalidateText());
            }

        }
        form.getInvalidLabel().setVisible(!isValid);
    }


    public boolean isValid() {
        return isValid;
    }

    public void setInvalidLabel(boolean valid){
        form.getInvalidLabel().setVisible(!valid);
    }

    public String getType(){
        return type;
    }
}
