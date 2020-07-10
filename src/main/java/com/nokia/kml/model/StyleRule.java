package com.nokia.kml.model;

import com.nokia.export.util.XMLFileTools;

public class StyleRule {
    private String name;
    /**
     * 对应大于等于
     */
    private Double minValue;
    /**
     * 对应小于等于
     */
    private Double maxValue;
    /**
     * 对应大于
     */
    private Double minValue_;

    /**
     * 对应小于
     */
    private Double maxValue_;
    private String fill;
    private String fillOpacity;
    private String lineFill;
    private String lineOpacity;
    private Double lineWidth;
    private Double fontSize;
    private String textFill;
    private String labelPropertyName;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }

    public String getFillOpacity() {
        if (fillOpacity == null){
            return "ff";
        }
        return fillOpacity;
    }

    public void setFillOpacity(String fillOpacity) {
        if (fillOpacity == null){
            this.fillOpacity = "ff";
        }else {
            this.fillOpacity = XMLFileTools.getOpacity(Double.valueOf(fillOpacity));
        }
    }
    public Double getMinValue_() {
        return minValue_;
    }

    public void setMinValue_(Double minValue_) {
        this.minValue_ = minValue_;
    }

    public Double getMaxValue_() {
        return maxValue_;
    }

    public void setMaxValue_(Double maxValue_) {
        this.maxValue_ = maxValue_;
    }


    public boolean isInStyleRule(double d){
        if (minValue != null){
            if (!(d >= minValue)){
                return false;
            }
        }
        if (minValue_ != null){
            if (!(d > minValue_)){
                return false;
            }
        }
        if (maxValue != null){
            if (!(d <= maxValue)){
                return false;
            }
        }
        if (maxValue_ != null){
            if (!(d < maxValue_)){
                return false;
            }
        }
        return true;
    }

    public String getLineFill() {
        return lineFill;
    }

    public void setLineFill(String lineFill) {
        this.lineFill = lineFill;
    }

    public String getLineOpacity() {
        if (lineOpacity == null){
            return "ff";
        }
        return lineOpacity;
    }

    public void setLineOpacity(String lineOpacity) {
        if (lineOpacity == null){
            this.lineOpacity = "ff";
        }else {
            this.lineOpacity = XMLFileTools.getOpacity(Double.valueOf(lineOpacity));
        }
    }

    public Double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(Double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public Double getFontSize() {
        return fontSize;
    }

    public void setFontSize(Double fontSize) {
        this.fontSize = fontSize;
    }

    public String getTextFill() {
        return textFill;
    }

    public void setTextFill(String textFill) {
        this.textFill = textFill;
    }

    public String getLabelPropertyName() {
        return labelPropertyName;
    }

    public void setLabelPropertyName(String labelPropertyName) {
        this.labelPropertyName = labelPropertyName;
    }
}
