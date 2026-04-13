package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;

class LabelFactory {
    private final double cellHeight;
    private final double cellWidth;

    public LabelFactory(double cellHeight, double cellWidth) {
        this.cellHeight = cellHeight;
        this.cellWidth = cellWidth;
    }

    public <T> Label getLabel(T text, LabelType type, boolean border, boolean textAlignRight) {
        Label label = new Label();
        label.setPadding(new Insets(5, 10, 0, 10));
        label.setMinHeight(type == LabelType.TALL ? cellHeight * 2 : cellHeight);
        label.setMaxHeight(type == LabelType.TALL ? cellHeight * 2 : cellHeight);
        label.setPrefHeight(type == LabelType.TALL ? cellHeight * 2 : cellHeight);
        label.setMaxWidth(cellWidth);

        if (border) {
            label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        }

        if (textAlignRight) {
            label.setAlignment(Pos.CENTER_RIGHT);
            label.setTextAlignment(TextAlignment.RIGHT);
        }

        if (type == LabelType.TALL) {
            label.setWrapText(true);
        }

        if (type == LabelType.NUMBER && text instanceof Number num && num.intValue() != 0) {
            label.setText(String.format(FormatConfig.numberFormat(), "%,d", num.intValue()));
        } else if (type == LabelType.NUMBER) {
            label.setText("");
        } else {
            label.setText(text != null ? text.toString() : "");
        }

        return label;
    }

    public enum LabelType {
        TEXT,
        NUMBER,
        TALL
    }
}


