package userinterface;

public class FormListener {
    String name;
    Form form;
    boolean isValid = false;

    public FormListener(InputForm form, String name, int min, int max){
        this.name = name;
        this.form = form;
        //listener patern from claude.ai
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                var checker = new NumberValidator(form, min, max, form.getInput());
                isValid = checker.isValid();
                form.getInvalidLabel().setVisible(!isValid);
            }
        });
    }

    public FormListener(DropdownForm dropdown, String name){
        this.name = name;
        this.form = dropdown;
        //listener patern from claude.ai
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                isValid = !(form.getInput() == null);
                form.getInvalidLabel().setVisible(!isValid);
            }
        });
    }

    public FormListener(InputForm form, String name){
        this.name = name;
        this.form = form;
        //listener patern from claude.ai
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused){
                isValid = !(form.getInput().isEmpty());
                form.getInvalidLabel().setVisible(!isValid);
            }
        });
    }


    public boolean isValid() {
        return isValid;
    }

    public void setInvalidLabel(boolean valid){
        form.getInvalidLabel().setVisible(!valid);
    }
}
