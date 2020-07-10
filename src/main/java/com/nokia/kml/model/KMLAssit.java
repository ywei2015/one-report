package com.nokia.kml.model;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class KMLAssit {
    private StringBuilder style;
    private HashMap<String,String> fill = new HashMap<>();
    private List<StyleRule> styleRules;
    private String layerName;
    public KMLAssit(List<StyleRule> styleRules, String layer) {
        this.styleRules = styleRules;
        this.layerName = layer;
    }

    public StringBuilder getStyle() {
        return style;
    }

    public void setStyle(StringBuilder style) {
        this.style = style;
    }

    public HashMap<String, String> getFill() {
        return fill;
    }

    public void setFill(HashMap<String, String> fill) {
        this.fill = fill;
    }

    public List<StyleRule> getStyleRules() {
        return styleRules;
    }

    public void setStyleRules(List<StyleRule> styleRules) {
        this.styleRules = styleRules;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public List<StyleRule> getPolygonStyles(){
        if (this.styleRules.size() >0){
            List<StyleRule> line_styles = this.styleRules.stream().filter(styleRule -> styleRule.getType().equals("polygonSymbolizer"))
                    .collect(Collectors.toList());
            return line_styles;
        }
        return null;
    }

    public Integer getPolygonStyleSize(){
        if (this.styleRules.size() >0){
            List<StyleRule> line_styles = this.styleRules.stream().filter(styleRule -> styleRule.getType().equals("polygonSymbolizer"))
                    .collect(Collectors.toList());
            return line_styles.size();
        }
        return 1;
    }

    public StyleRule getTextStyle(){
        if (this.styleRules.size() >0){
            StyleRule text_style = this.styleRules.stream().filter(styleRule -> styleRule.getType().equals("textSymbolizer"))
                    .collect(Collectors.toList()).get(0);
            return text_style;
        }
        return null;
    }

    public Boolean isContainTextStyle(){
        if (this.styleRules.size() >0){
            List<StyleRule> text_styles = this.styleRules.stream().filter(styleRule -> styleRule.getType().equals("textSymbolizer"))
                    .collect(Collectors.toList());
            return text_styles.size() >0;
        }
        return false;
    }

    public String getPropertyName(){
        String properName = getPolygonStyles().get(0).getName();
        return properName;
    }
}
