package userinterface;

import java.util.regex.Pattern;

public class FormListener {

    private final String name;
    private final Form form;
    private final String type;
    private final String format;
    boolean isValid = false;
    private int min = 0;
    private int max = 0;

    /**
     * creates a form listener for numbers
     *
     * @param form needs an object of InputForm
     * @param name the name of the listener
     * @param min  min number of characters
     * @param max  max number of characters
     */
    public FormListener(InputForm form, String name, int min, int max) {
        this.name = name;
        this.form = form;
        this.min = min;
        this.max = max;
        this.format = "";
        this.type = "number";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) {
                validate();
            }
        });
    }

    /**
     * creates a form listener for dropdown form
     *
     * @param dropdown needs an object of DropdownForm
     * @param name     the name of the listener
     */
    public FormListener(DropdownForm dropdown, String name) {
        this.name = name;
        this.form = dropdown;
        this.format = "";
        this.type = "dropdown";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) {
                validate();
            }
        });
    }

    /**
     * creates a form listener for text with maximum length
     *
     * @param form needs an object of InputForm
     * @param name the name of the listener
     * @param max  max number of characters
     */
    public FormListener(InputForm form, String name, int max) {
        this.name = name;
        this.form = form;
        this.max = max;
        this.format = "";
        this.type = "textMax";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) {
                validate();
            }
        });
    }

    /**
     * creates a form listener with formated Text
     *
     * @param form   needs an object of InputForm
     * @param name   the name of the listener
     * @param format the format as regex (Allowed characters needs to be in []) example: ^[0-9]{5}
     * @param min    min number of characters
     * @param max    max number of characters
     */
    public FormListener(InputForm form, String name, String format, int min, int max) {
        this.name = name;
        this.form = form;
        this.format = format;
        this.min = min;
        this.max = max;
        this.type = "format";
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) {
                validate();
            }
        });
    }

    public void validate() {
        switch (type) {
            case "number" -> {
                int number = 0;
                try {
                    number = Integer.parseInt(form.getInput());
                    isValid = number >= min && number <= max;
                } catch (NumberFormatException e) {
                    isValid = false;
                }
            }
            case "dropdown" -> {
                isValid = form.getInput() != null;
            }
            case "textMax" -> {
                isValid = !(form.getInput().isEmpty()) && form.getInput().length() <= max;
                if (form.getInput().length() > max) {
                    form.getInvalidLabel().setText("Maximal " + max + " Zeichen erlaubt!");
                } else {
                    form.getInvalidLabel().setText(form.getValidateText());
                }
            }
            case "format" -> {
                String characters = format.substring(format.indexOf("[") + 1, format.indexOf("]"));
                isValid = !(form.getInput().isEmpty()
                        || form.getInput().length() > max
                        || form.getInput().length() < min
                        || !Pattern.compile(format).matcher(form.getInput()).find()); //Pattern matcher from claude.ai
                if (form.getInput().length() > max) {
                    form.getInvalidLabel().setText("Maximal " + max + " Zeichen (" + characters + ")");
                } else if (!Pattern.compile(format).matcher(form.getInput()).find()) {
                    form.getInvalidLabel().setText("Muss mit " + min + " Zeichen (" + characters + ") starten");
                } else {
                    form.getInvalidLabel().setText(form.getValidateText());
                }
            }
        }
        form.getInvalidLabel().setVisible(!isValid);
    }

    /**
     * check if input is valid
     *
     * @return returns boolean ture if valid, false if not valid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * change if the invalidLabel is visible
     *
     * @param visible need a boolean true = label visible, false = label not visible
     */
    public void setInvalidLabel(boolean visible) {
        form.getInvalidLabel().setVisible(visible);
    }
}
