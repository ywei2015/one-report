package com.nokia.export.util;

import com.nokia.kml.model.StyleRule;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public class XMLFileTools {

    private static final String PropertyIsGreaterThanOrEqualTo = "PropertyIsGreaterThanOrEqualTo";
    private static final String PropertyIsEqualTo = "PropertyIsEqualTo";
    private static final String PropertyIsLessThanOrEqualTo = "PropertyIsLessThanOrEqualTo";
    private static final String PropertyIsGreaterThan = "PropertyIsGreaterThan";
    private static final String PropertyIsLessThan = "PropertyIsLessThan";


    /**
     * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
     * @param xmlString
     * @return
     */
    public static Map<String, String> parseXml(String xmlString) {
        if (xmlString == null) {
            return Collections.emptyMap();
        }
        if (xmlString.length() == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> m = new HashMap<>();
        //xmlString = xmlString.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

        try {
            // 1、读取XML文件，获得Document对象
            SAXReader reader = new SAXReader();
            InputStream in = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
            Document doc = reader.read(in);
            Element root = doc.getRootElement();
            for (Object c : root.elements()) {
                Element e = (Element) c;
                String k = e.getName();
                String v = "";
                @SuppressWarnings("rawtypes")
                List children = e.elements();
                if (children.isEmpty()) {
                    v = e.getText();
                } else {
                    v = getChildrenText(children);
                }
                m.put(k, v);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return m;
    }

    /**
     * 获取子结点的xml
     *
     * @param children
     * @return String
     */
    public static String getChildrenText(List children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                Element e = (Element) it.next();
                String name = e.getName();
                String value = e.getText();
                List list = e.elements();
                sb.append("<" + name + ">");
                if (!list.isEmpty()) {
                    sb.append(getChildrenText(list));
                }
                sb.append(value);
                sb.append("</" + name + ">");
            }
        }

        return sb.toString();
    }


    /**
     * 从inputStream获取sql并设置参数
     * @param filePath
     * @param param
     * @param filter
     * @return
     */
    public static String getSqlAndSetDefaultParam(InputStream filePath, String param, String filter) {
        SAXReader reader = new SAXReader();
        // 创建读取对象
        Document doc = null;
        try {
            doc = reader.read(filePath);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Element rootElement = doc.getRootElement();

        List<Element> pathElements_ = rootElement.element("metadata").elements("entry");
        Element entry = pathElements_.stream().filter(element -> element.element("virtualTable") != null).collect(Collectors.toList()).get(0);
        Element pathElement = entry.element("virtualTable").element("sql");
        String sql = pathElement.getText();
        String geometryName =  entry.element("virtualTable")
                .element("geometry").elementText("name");
        sql = StringTools.getFormatSqlForGeo(sql, param, filter,geometryName);
        List<Element> pathElements = entry.element("virtualTable").elements("parameter");
        for (Element element : pathElements){
            String name = element.elementText("name");
            String default_value = element.elementText("defaultValue");
            if (sql.contains("%"+name+"%")){
                sql = sql.replaceAll("%"+name+"%", default_value);
            }
        }
        return sql;
    }

    public static List<StyleRule> getStyleRuleByContent(String res, String styler){
        SAXReader reader = new SAXReader();
        InputStream in = null;
        Document doc = null;
        List<StyleRule> styleRules = null;
        try {
            in = new ByteArrayInputStream(res.getBytes("UTF-8"));
            doc = reader.read(in);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element rootElement = doc.getRootElement();
        List<Element> rules = rootElement.element("NamedLayer").element("UserStyle")
                .element("FeatureTypeStyle").elements("Rule");
        styleRules = analyzeStyle(rules);
        return styleRules;
    }

    private static List<StyleRule> analyzeStyle(List<Element> rules){
        List<StyleRule> styleRules = new LinkedList<>();
        rules.forEach(element -> {
            StyleRule styleRule = new StyleRule();
            if (element.element("Filter") != null) {
                Element and = element.element("Filter").element("And");
                if (and.element(PropertyIsLessThanOrEqualTo) != null){
                    Element thanOrEqualTo = and.element(PropertyIsLessThanOrEqualTo);
                    String propertyName = thanOrEqualTo.elementText("PropertyName");
                    styleRule.setName(propertyName);
                    String max = thanOrEqualTo.elementText("Literal");
                    styleRule.setMaxValue(Double.valueOf(max));
                }
                if (and.element(PropertyIsGreaterThanOrEqualTo) != null){
                    Element thanOrEqualTo = and.element(PropertyIsGreaterThanOrEqualTo);
                    String propertyName = thanOrEqualTo.elementText("PropertyName");
                    styleRule.setName(propertyName);
                    String min = thanOrEqualTo.elementText("Literal");
                    styleRule.setMinValue(Double.valueOf(min));
                }
                if (and.element(PropertyIsEqualTo) != null){
                    Element thanOrEqualTo = and.element(PropertyIsEqualTo);
                    String propertyName = thanOrEqualTo.elementText("PropertyName");
                    styleRule.setName(propertyName);
                    String value = thanOrEqualTo.elementText("Literal");
                    styleRule.setMinValue(Double.valueOf(value));
                    styleRule.setMaxValue(Double.valueOf(value));
                }
                if (and.element(PropertyIsGreaterThan) != null){
                    Element GreaterThan = and.element(PropertyIsGreaterThan);
                    String propertyName = GreaterThan.elementText("PropertyName");
                    styleRule.setName(propertyName);
                    String value = GreaterThan.elementText("Literal");
                    styleRule.setMinValue_(Double.valueOf(value));
                }
                if (and.element(PropertyIsLessThan) != null){
                    Element LessThan = and.element(PropertyIsLessThan);
                    String propertyName = LessThan.elementText("PropertyName");
                    styleRule.setName(propertyName);
                    String value = LessThan.elementText("Literal");
                    styleRule.setMaxValue_(Double.valueOf(value));
                }
                styleRule.setName(styleRule.getName().trim());
            }

            Element polygonSymbolizer = element.element("PolygonSymbolizer");
            if (polygonSymbolizer != null){
                styleRule.setType("polygonSymbolizer");
                if (polygonSymbolizer.element("Fill") != null){
                    List<Element> fills = polygonSymbolizer.element("Fill").elements("SvgParameter");
                    String fill = fills.get(0).getTextTrim();
                    String fill_opacity = fills.get(1).getTextTrim();
                    styleRule.setFill(fill);
                    styleRule.setFillOpacity(fill_opacity);
                }
                // line样式
                if (polygonSymbolizer.element("Stroke") != null){
                    List<Element> line_fills = polygonSymbolizer.element("Stroke").elements("SvgParameter");
                    List<Element> stroke_line_fills = line_fills.stream().filter(element1 -> element1.attributeValue("name")
                            .equalsIgnoreCase("stroke")).collect(Collectors.toList());
                    if (stroke_line_fills.size() > 0){
                        styleRule.setLineFill(stroke_line_fills.get(0).getTextTrim());
                    }
                    List<Element> width_line_fills = line_fills.stream().filter(element1 -> element1.attributeValue("name")
                            .equalsIgnoreCase("stroke-width")).collect(Collectors.toList());
                    if (width_line_fills.size() > 0){
                        String line_width = width_line_fills.get(0).getTextTrim();
                        if (StringUtils.isNotBlank(line_width)){
                            styleRule.setLineWidth(Double.valueOf(line_width));
                        }
                    }
                    List<Element> opacity_line_fills = line_fills.stream().filter(element1 -> element1.attributeValue("name")
                            .equalsIgnoreCase("stroke-opacity")).collect(Collectors.toList());
                    if (opacity_line_fills.size() > 0){
                        styleRule.setLineOpacity(opacity_line_fills.get(0).getTextTrim());
                    }
                }
            }
            Element textSymbolizer = element.element("TextSymbolizer");
            if (textSymbolizer != null){
                styleRule.setType("textSymbolizer");
                String propertyName = textSymbolizer.element("Label").element("Function").element("PropertyName").getText();
                styleRule.setLabelPropertyName(propertyName);
                List<Element> elements = textSymbolizer.element("Font").elements("SvgParameter");
                List<Element> elements_ = elements.stream().filter(element1 -> element1.attributeValue("name").equals("font-size")).collect(Collectors.toList());
                if (elements_.size() >0){
                    String font_size = elements_.get(0).getText();
                    styleRule.setFontSize(Double.valueOf(font_size));
                }
                String font_fill = textSymbolizer.element("Fill").elementText("SvgParameter");
                styleRule.setTextFill(font_fill);
            }
            styleRules.add(styleRule);

        });
        return styleRules;
    }

    private static List<StyleRule> analyzeTextStyle(List<Element> rules){
        List<StyleRule> styleRules = new LinkedList<>();
        for (Element element : rules){
            StyleRule styleRule = new StyleRule();
            Element textSymbolizer = element.element("TextSymbolizer");
            if (textSymbolizer != null){
                styleRule.setName("textSymbolizer");
                String propertyName = textSymbolizer.element("Label").element("Function").element("PropertyName").getText();
                styleRule.setLabelPropertyName(propertyName);
                List<Element> elements = textSymbolizer.element("Font").elements("SvgParameter");
                List<Element> elements_ = elements.stream().filter(element1 -> element1.attributeValue("name").equals("font-size")).collect(Collectors.toList());
                if (elements_.size() >0){
                    String font_size = elements_.get(0).getText();
                    styleRule.setFontSize(Double.valueOf(font_size));
                }
                String font_fill = textSymbolizer.element("Fill").elementText("SvgParameter");
                styleRule.setTextFill(font_fill);
            }
            Element polygonSymbolizer = element.element("PolygonSymbolizer");
            if (polygonSymbolizer != null){
                styleRule.setName("polygonSymbolizer");
                List<Element> fills = polygonSymbolizer.element("Stroke").elements("SvgParameter");
                String line_fill = fills.get(0).getTextTrim();
                String line_width = fills.get(1).getTextTrim();
                styleRule.setLineFill(line_fill);
                if (StringUtils.isNotBlank(line_width)){
                    styleRule.setLineWidth(Double.valueOf(line_width));
                }
            }
            styleRules.add(styleRule);
        }
        return styleRules;
    }
    public static String getOpacity(double opacity){
        int alpha = (int) Math.round(opacity * 255);
        String hex = Integer.toHexString(alpha).toUpperCase();
        if (hex.length() == 1) {
            hex = "0" + hex;
        }
        return hex;
    }
}
