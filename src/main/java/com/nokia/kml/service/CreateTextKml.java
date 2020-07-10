package com.nokia.kml.service;

import com.nokia.kml.model.KMLAssit;
import com.nokia.kml.model.StyleRule;
import com.nokia.export.util.ColorTransform;
import com.nokia.export.util.DBQuery;
import com.nokia.export.util.FileOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Component
public class CreateTextKml extends AbstractCreateKml {
		private static String DBNAME = "dbzc";
		private  Logger logger = LoggerFactory.getLogger(this.getClass());


	public  String CreateKmlContent(String sql, String kmlFilePath, DBQuery dbQuery, KMLAssit kmlAssit) throws Exception {
		File file = new File(kmlFilePath);
		if(file.isFile()) {
			file.delete();
		}
		String markName = "FAST"+kmlAssit.getLayerName();
		StringBuilder styleBuilder = kmlAssit.getStyle();
		// 整理Placemark数据
		StringBuilder markBuilder = getPlaceMarks(kmlAssit,sql,kmlFilePath,dbQuery);
		// 整理标头和标尾
		String kmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\"> xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\r\n" +
				"<Document>\r\n" +
				styleBuilder +
				"<Folder>" +
				"<name>" + markName + "</name>" +
				markBuilder +
				"</Folder>" +
				"</Document>\r\n" +
				"</kml>";
		FileOperation.contentToTxt(kmlFilePath, kmlString);
		return kmlFilePath;
	}



	private  StringBuilder getPlaceMarks(KMLAssit kmlAssit, String sql, String kmlFilePath,DBQuery dbQuery) throws Exception {
		ResultSetMetaData rsm ;
		StringBuilder res = new StringBuilder();
		String label_properName = kmlAssit.getTextStyle().getLabelPropertyName();
		String propertyName = kmlAssit.getPropertyName();
		logger.info("label_properName:[{}],properName:[{}]", label_properName,propertyName);
		ResultSet rs = dbQuery.getResByConnection(sql);
		rsm = rs.getMetaData();
		int col = rsm.getColumnCount();
		List<String> colNames = new LinkedList<>();
		for (int i = 0; i < col; i++) {
			colNames.add(rsm.getColumnName( i + 1 ));
		}
		while (rs.next()){
			if (StringUtils.isNotBlank(propertyName)){
				Object properObject_ = rs.getObject(propertyName);
				if (properObject_ == null){
					continue;
				}
			}
			String properObject = (String) rs.getObject(label_properName);
			StringBuilder description = new StringBuilder();
			StringBuilder fill = new StringBuilder();
			StringBuilder spoy = new StringBuilder();
			colNames.forEach(s -> {
				try {
					Object o = rs.getObject(s);
					if (s.equalsIgnoreCase("spy")){
						Clob clob = (Clob) o;
						if (clob != null){
							String spy = clob.getSubString(1, (int) clob.length());
							spy = spy.substring(spy.lastIndexOf("(")+1,spy.indexOf(")"));
							spy = spy.replaceAll(", ",";");
							spy = spy.replaceAll(" ",",");
							spy = spy.replaceAll(";"," ");
							spoy.append(spy);
						}
					}else {
						description.append(s).append(": ").append(o).append("\r\n");
					}
					if(s.equalsIgnoreCase(propertyName)){
						Double value = ((BigDecimal) o).doubleValue();
						fill.append(getFill(value,kmlAssit.getPolygonStyles(),kmlAssit.getFill()));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			StringBuilder mark = calculatePlaceMark(description,fill,properObject,spoy);
			res.append(mark);
		}
		return res;
	}


	private  StringBuilder calculatePlaceMark(StringBuilder description, StringBuilder fill,String name, StringBuilder spoy) {
		String first_spoy = spoy.substring(0,spoy.indexOf(" "));
		String urlId = fill.toString();
		if (StringUtils.isBlank(urlId)){
			urlId = defaultHexcolor;
		}
		StringBuilder ps = new StringBuilder().append("<Placemark>\n")
				.append("<name>") .append(name)  .append("</name>\n")
				.append("<description>") .append(description) .append("</description>\n")
				.append("<styleUrl>#") .append(urlId) .append("</styleUrl>\n")
				.append("<MultiGeometry><Point><coordinates>").append(first_spoy)
				.append("</coordinates></Point>")
				.append("<Polygon>\n<outerBoundaryIs>\n<LinearRing>\n<tessellate>1</tessellate>\n")
				.append("<coordinates>\n").append(spoy.toString()).append("\n")
				.append("</coordinates></LinearRing>\n</outerBoundaryIs>\n</Polygon></MultiGeometry>\n</Placemark>\n");
		return ps;
	}

	public  StringBuilder calculateStyles(KMLAssit kmlAssit){
		StringBuilder styleBuilder = new StringBuilder();

		StyleRule line_style = kmlAssit.getPolygonStyles().get(0);
		StyleRule font_style = kmlAssit.getTextStyle();
		String line_color = line_style.getLineFill();
		line_color = line_style.getLineOpacity()+line_color.replace("#","");
		line_color = ColorTransform.transformToGeoStyleFill(line_color);
		Double line_width = line_style.getLineWidth();
		String font_color = font_style.getTextFill();
		font_color = "ff"+font_color.replace("#","");
		font_color = ColorTransform.transformToGeoStyleFill(font_color);
		Double font_size = font_style.getFontSize();
		font_size = font_size/10;
		String defaultStyle = "<Style id=\"" + defaultHexcolor + "\">\r\n" +
				"<IconStyle>\n" +
				"<color>00ffffff</color>\n"+
				"<scale>0.0</scale>\n" +
				"<heading>0.0</heading>\n"+
				"<Icon>\n" +
				"<href>http://icons.opengeo.org/markers/icon-poly.1.png</href>\n" +
				"</Icon>\n" +
				"</IconStyle>\n" +
				"<LineStyle>\n" +
				"<color>" + line_color + "</color>\n" +
				"<width>"+line_width+"</width>\n"+
				"</LineStyle>\n" +
				"<LabelStyle>\n" +
				"<color>"+font_color+"</color>\n" +
				"<scale>"+font_size+"</scale>\n" +
				"</LabelStyle>"+
				"<PolyStyle>\n" +
				"<color>00aaaaaa</color>\n" +
				"<outline>1</outline>\n"+
				"</PolyStyle>\n" +
				"</Style>\n";
		styleBuilder.append(defaultStyle);

		styleBuilder.append(calculateStylerByStylerRulers(kmlAssit));
		return styleBuilder;
	}

	private StringBuilder calculateStylerByStylerRulers(KMLAssit kmlAssit) {
		List<StyleRule> styleRules = kmlAssit.getPolygonStyles();
		StringBuilder stylers = new StringBuilder();
		StyleRule text_rule = kmlAssit.getTextStyle();
		for(StyleRule styleRule : styleRules){
			if (StringUtils.isBlank(styleRule.getFill())){
				continue;
			}
			String font_color = text_rule.getTextFill();
			Double font_size  = text_rule.getFontSize();
			String fill = (styleRule.getFillOpacity()+styleRule.getFill().replace("#","")).toLowerCase();
			String fill_ = ColorTransform.transformToGeoStyleFill(fill);
			String line_fill = (styleRule.getLineOpacity()+styleRule.getLineFill().replace("#","")).toLowerCase();
			String line_fill_ = ColorTransform.transformToGeoStyleFill(line_fill);
			Double line_width = styleRule.getLineWidth();

			font_color = "ff"+font_color.replace("#","");
			font_color = ColorTransform.transformToGeoStyleFill(font_color);

			font_size = font_size/10;
			String style = "<Style id=\"" + fill_ + "\">\r\n" +
					"<IconStyle>\r\n" +
					"<scale>0.0</scale>\r\n" +
					"<Icon>\r\n" +
					"<href>http://icons.opengeo.org/markers/icon-poly.1.png</href>\r\n" +
					"</Icon>\r\n" +
					"</IconStyle>\r\n" +
					"<LineStyle>\r\n" +
					"<color>" + line_fill_ + "</color>" +
					"<width>"+line_width+"</width>\n"+
					"</LineStyle>\r\n" +
					"<LabelStyle>\n" +
					"<color>"+font_color+"</color>\n" +
					"<scale>"+font_size+"</scale>\n" +
					"</LabelStyle>"+
					"<PolyStyle>\r\n" +
					"<color>" + fill_ + "</color>" +
					"<outline>1</outline>\n"+
					"</PolyStyle>\r\n" +
					"</Style>\r\n";
			stylers.append(style);
		}
		return stylers;
	}
}