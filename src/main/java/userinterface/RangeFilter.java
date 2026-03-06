package userinterface;

import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;
import kennwertdatenbank.Project;
import org.controlsfx.control.RangeSlider;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RangeFilter {
    private final Label titel;
    private final Button reset;
    private final RangeSlider slider;
    private final TextField minTextField;
    private final TextField maxTextField;
    private final TextFormatter<Number> minFormatter;
    private final TextFormatter<Number> maxFormatter;
    private final Supplier<Integer> minSupplier;
    private final Supplier<Integer> maxSupplier;
    private final Function<Project, Integer> valueExtractor;
    private FilterChangeListener changeListener;

    /**
     * Creates a new RangeFilter with UI elements
     *
     * @param titel          the text that will be displayed in the titel Label
     * @param resetText      the text that will be displayed in the reset Button
     * @param locale         the number format that will be used
     * @param valueExtractor
     * @param minSupplier
     * @param maxSupplier
     */
    public RangeFilter(String titel, String resetText, Locale locale, Function<Project, Integer> valueExtractor, Supplier<Integer> minSupplier, Supplier<Integer> maxSupplier) {
        this.titel = new Label(titel);
        this.reset = new Button(resetText);
        this.slider = new RangeSlider();
        this.minTextField = new TextField();
        this.maxTextField = new TextField();
        this.valueExtractor = valueExtractor;
        this.minSupplier = minSupplier;
        this.maxSupplier = maxSupplier;
        NumberFormat integerFormat = NumberFormat.getIntegerInstance(locale); //formatter to format numbers in swiss style (#'###)
        integerFormat.setMaximumFractionDigits(0); //set the formatter to display only integers
        this.minFormatter = new TextFormatter<>(new NumberStringConverter(integerFormat), minSupplier.get());
        this.maxFormatter = new TextFormatter<>(new NumberStringConverter(integerFormat), maxSupplier.get());
        this.minTextField.setTextFormatter(minFormatter);
        this.maxTextField.setTextFormatter(maxFormatter);

        maxTextField.setTooltip(new Tooltip("Maximum"));
        maxTextField.getTooltip().setShowDelay(Duration.millis(500));
        minTextField.setTooltip(new Tooltip("Minimum"));
        minTextField.getTooltip().setShowDelay(Duration.millis(500));

        reset.setMaxWidth(Double.MAX_VALUE);
        this.titel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        setRange();

        //listener when reset button was pressed
        reset.setOnAction(event -> {
            slider.setLowValue(slider.getMin());
            slider.setHighValue(slider.getMax());
            notifyFilterChanged();
        });

        //listener when slider low value is changed
        slider.lowValueProperty().addListener((obs, oldValue, newValue) -> {
            minFormatter.setValue(newValue.intValue());
        });

        //listener when slider heigh value is changed
        slider.highValueProperty().addListener((obs, oldValue, newValue) -> {
            maxFormatter.setValue(newValue.intValue());
        });

        //listener for minimum text field on focus
        minTextField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && wasFocused) {
                try {
                    int value = minFormatter.getValue().intValue();
                    if (value >= slider.getMin() && value <= slider.getHighValue()) {
                        slider.setLowValue(value);
                    } else if (value < slider.getMin()) {
                        slider.setLowValue(slider.getMin());
                        minFormatter.setValue(slider.getMin());
                    } else if (value > slider.getHighValue()) {
                        slider.setLowValue(slider.getHighValue());
                        minFormatter.setValue(slider.getHighValue());
                    }
                    notifyFilterChanged();
                } catch (NumberFormatException ex) {
                }
            }
        });

        //listener for minimum text field on enter
        minTextField.setOnAction(e -> {
            try {
                int value = minFormatter.getValue().intValue();
                if (value >= slider.getMin() && value <= slider.getHighValue()) {
                    slider.setLowValue(value);
                } else if (value < slider.getMin()) {
                    slider.setLowValue(slider.getMin());
                    minFormatter.setValue(slider.getMin());
                } else if (value > slider.getHighValue()) {
                    slider.setLowValue(slider.getHighValue());
                    minFormatter.setValue(slider.getHighValue());
                }
                notifyFilterChanged();
            } catch (NumberFormatException ex) {
            }
        });

        //listener for maximum text field on focus
        maxTextField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && wasFocused) {
                try {
                    int value = maxFormatter.getValue().intValue();
                    if (value <= slider.getMax() && value >= slider.getLowValue()) {
                        slider.setHighValue(value);
                    } else if (value > slider.getMax()) {
                        slider.setHighValue(slider.getMax());
                        maxFormatter.setValue(slider.getMax());
                    } else if (value < slider.getMin()) {
                        slider.setHighValue(slider.getLowValue());
                        maxFormatter.setValue(slider.getLowValue());
                    }
                    notifyFilterChanged();
                } catch (NumberFormatException ex) {
                }
            }
        });

        //listener for maximum text field on enter
        maxTextField.setOnAction(e -> {
            try {
                int value = maxFormatter.getValue().intValue();
                if (value <= slider.getMax() && value >= slider.getLowValue()) {
                    slider.setHighValue(value);
                } else if (value > slider.getMax()) {
                    slider.setHighValue(slider.getMax());
                    maxFormatter.setValue(slider.getMax());
                } else if (value < slider.getMin()) {
                    slider.setHighValue(slider.getLowValue());
                    maxFormatter.setValue(slider.getLowValue());
                }
                notifyFilterChanged();
            } catch (NumberFormatException ex) {
            }
        });

        //listener when slider low value was set
        slider.lowValueChangingProperty().addListener((obs, wasChangin, isChanging) -> {
            if (!isChanging && wasChangin) {
                minFormatter.setValue(slider.getLowValue());
                notifyFilterChanged();
            }
        });

        //listener when slider heigh value was set
        slider.highValueChangingProperty().addListener((obs, wasChangin, isChanging) -> {
            if (!isChanging && wasChangin) {
                maxFormatter.setValue(slider.getHighValue());
                notifyFilterChanged();
            }
        });

    }

    //method to register the callback
    public void setOnFilterChanged(FilterChangeListener listener) {
        this.changeListener = listener;
    }

    //helper method to call back
    private void notifyFilterChanged() {
        if (changeListener != null) {
            changeListener.onFilterChanged();
        }
    }

    /**
     * Creates a test-function
     *
     * @return returns the value as int
     */
    public Predicate<Project> getPredicate() {
        //von claude.ai
        return project -> {
            int value = valueExtractor.apply(project);
            return value >= slider.getLowValue() && value <= slider.getHighValue();
        };
    }

    /**
     * sets the range (min to max) of the range-slider, must be updatet when a new project is added.
     */
    public void setRange() {
        int min = minSupplier.get();
        int max = maxSupplier.get();
        slider.setMin(min);
        slider.setMax(max);
        slider.setLowValue(min);
        slider.setHighValue(max);
        notifyFilterChanged();
    }

    /**
     *
     * @return the titel Label element
     */
    public Label getTitelLabel() {
        return titel;
    }

    /**
     *
     * @return the reset Button element
     */
    public Button getResetButton() {
        return reset;
    }

    /**
     *
     * @return the range-slider element
     */
    public RangeSlider getSlider() {
        return slider;
    }

    /**
     *
     * @return the min TextField element
     */
    public TextField getMinTextField() {
        return minTextField;
    }

    /**
     *
     * @return the max TextField element
     */
    public TextField getMaxTextField() {
        return maxTextField;
    }

    //Callback methode interface (from claude.ai)
    @FunctionalInterface
    public interface FilterChangeListener {
        void onFilterChanged();
    }

}