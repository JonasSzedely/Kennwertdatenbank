package view;

import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;
import model.AppLogger;
import model.Project;
import org.controlsfx.control.RangeSlider;

import java.text.NumberFormat;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

class RangeFilter {
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

    public RangeFilter(String titel, String resetText, Function<Project, Integer> valueExtractor, Supplier<Integer> minSupplier, Supplier<Integer> maxSupplier) {
        this.titel = new Label(titel);
        this.reset = new Button(resetText);
        this.slider = new RangeSlider();
        this.minTextField = new TextField();
        this.maxTextField = new TextField();
        this.valueExtractor = valueExtractor;
        this.minSupplier = minSupplier;
        this.maxSupplier = maxSupplier;
        NumberFormat integerFormat = NumberFormat.getIntegerInstance(FormatConfig.numberFormat());
        integerFormat.setMaximumFractionDigits(0);
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

        reset.setOnAction(event -> {
            slider.setLowValue(slider.getMin());
            slider.setHighValue(slider.getMax());
            notifyFilterChanged();
        });

        slider.lowValueProperty().addListener((obs, oldValue, newValue) -> {
            minFormatter.setValue(newValue.intValue());
        });

        slider.highValueProperty().addListener((obs, oldValue, newValue) -> {
            maxFormatter.setValue(newValue.intValue());
        });

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
                    AppLogger.error(ex.getMessage());
                }
            }
        });

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
                    AppLogger.error(ex.getMessage());
                }
            }
        });

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
                AppLogger.error(ex.getMessage());
            }
        });

        slider.lowValueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && wasChanging) {
                minFormatter.setValue(slider.getLowValue());
                notifyFilterChanged();
            }
        });

        slider.highValueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && wasChanging) {
                maxFormatter.setValue(slider.getHighValue());
                notifyFilterChanged();
            }
        });
    }

    public void setOnFilterChanged(FilterChangeListener listener) {
        this.changeListener = listener;
    }

    private void notifyFilterChanged() {
        if (changeListener != null) {
            changeListener.onFilterChanged();
        }
    }

    public Predicate<Project> getPredicate() {
        return project -> {
            int value = valueExtractor.apply(project);
            return value >= slider.getLowValue() && value <= slider.getHighValue();
        };
    }

    public void setRange() {
        int min = minSupplier.get();
        int max = maxSupplier.get();
        slider.setMin(min);
        slider.setMax(max);
        slider.setLowValue(min);
        slider.setHighValue(max);
        notifyFilterChanged();
    }

    public Label getTitelLabel() { return titel; }
    public Button getResetButton() { return reset; }
    public RangeSlider getSlider() { return slider; }
    public TextField getMinTextField() { return minTextField; }
    public TextField getMaxTextField() { return maxTextField; }

    @FunctionalInterface
    public interface FilterChangeListener {
        void onFilterChanged();
    }
}