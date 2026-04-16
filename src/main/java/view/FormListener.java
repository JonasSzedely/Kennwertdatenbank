package view;

import java.util.regex.Pattern;

import static view.FormListener.Type.*;


class FormListener {

    private final Form form;
    private final Type type;
    private final String format;
    boolean isValid = false;
    private int min = 0;
    private int max = 0;


    /**
     * creates a form listener for numbers
     *
     * @param form needs an object of InputForm
     * @param min  min number of characters
     * @param max  max number of characters
     */
    public FormListener(InputForm form, int min, int max) {
        this.form = form;
        this.min = min;
        this.max = max;
        this.format = "";
        this.type = NUMBER;
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
     */
    public FormListener(DropdownForm dropdown) {
        this.form = dropdown;
        this.format = "";
        this.type = DROPDOWN;
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
     * @param max  max number of characters
     */
    public FormListener(InputForm form, int max) {
        this.form = form;
        this.max = max;
        this.format = "";
        this.type = TEXT_MAX;
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
     * @param format the format as regex (Allowed characters needs to be in []) example: ^[0-9]{5}
     * @param min    min number of characters
     * @param max    max number of characters
     */
    public FormListener(InputForm form, String format, int min, int max) {
        this.form = form;
        this.format = format;
        this.min = min;
        this.max = max;
        this.type = FORMAT;
        this.form.getInputField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) {
                validate();
            }
        });
    }

    /**
     * Test if input is valid, if not the InvalidLabel will be made visible and isValid set to false.
     */
    public void validate() {
        switch (type) {
            case NUMBER -> {
                int number;
                try {
                    number = Integer.parseInt(form.getInput());
                    isValid = number >= min && number <= max;
                } catch (NumberFormatException e) {
                    isValid = false;
                }
            }
            case DROPDOWN -> {
                isValid = form.getInput() != null;
            }
            case TEXT_MAX -> {
                isValid = !(form.getInput().isEmpty()) && form.getInput().length() <= max;
                if (form.getInput().length() > max) {
                    form.getInvalidLabel().setText("Maximal " + max + " Zeichen erlaubt!");
                } else {
                    form.getInvalidLabel().setText(form.getValidateText());
                }
            }
            case FORMAT -> {
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
     * Check if input is valid.
     *
     * @return boolean
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Change visibility of invalidLabel.
     *
     * @param visible boolean
     */
    public void setInvalidLabel(boolean visible) {
        form.getInvalidLabel().setVisible(visible);
    }

    enum Type {
        NUMBER,
        DROPDOWN,
        TEXT_MAX,
        FORMAT
    }
}