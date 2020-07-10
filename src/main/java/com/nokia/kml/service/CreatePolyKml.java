package com.nokia.kml.service;

import com.nokia.kml.model.KMLAssit;
import com.nokia.kml.model.StyleRule;
import com.nokia.export.util.ColorTransform;
import com.nokia.export.util.DBQuery;
import com.nokia.export.util.FileOperation;
import com.nokia.export.util.RandomUtil;
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
public class CreatePolyKml extends AbstractCreateKml {
		private static String DBNAME = "dbzc";
		private  Logger logger = LoggerFactory.getLogger(this.getClass());


	public  String CreateKmlContent(String sql, String kmlFilePath, DBQuery dbQuery, KMLAssit kmlAssit) throws Exception {
		File file = new File(kmlFilePath);
		if(file.isFile()) {
			file.delete();
		}
		String markName = kmlFilePath.replaceAll(".kml","");
		StringBuilder styleBuilder = kmlAssit.getStyle();
		// 整理Placemark数据
		StringBuilder markBuilder = getPlaceMarks(kmlAssit,sql,kmlFilePath,dbQuery);
		// 整理标头和标尾
		String kmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\"> xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\r\n" +
				"<Document>\r\n" +
				"<name>" + markName + "</name>\r\n" +
				"<open>1</open>\r\n" +
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
		List<StyleRule> styleRules = kmlAssit.getStyleRules();
		String properName = styleRules.get(0).getName();
		logger.info("property:{}", properName);
		ResultSet rs = dbQuery.getResByConnection(sql);
		rsm = rs.getMetaData();
		int col = rsm.getColumnCount();
		List<String> colNames = new LinkedList<>();
		for (int i = 0; i < col; i++) {
			colNames.add(rsm.getColumnName( i + 1 ));
		}
		while (rs.next()){
			Object properObject = rs.getObject(properName);
			if (properObject == null){
				continue;
			}
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
					if(s.equalsIgnoreCase(properName)){
						Double value = ((BigDecimal) o).doubleValue();
						fill.append(getFill(value,styleRules,kmlAssit.getFill()));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			StringBuilder mark = calculatePlaceMark(description,fill,spoy,kmlFilePath);
			res.append(mark);
		}
		return res;
	}


	private static StringBuilder calculatePlaceMark(StringBuilder description, StringBuilder fill, StringBuilder spoy,String path) {
		String placename = "Id : "+ RandomUtil.randomUUID();
		String hexcolor = fill.append("_m").toString();
		StringBuilder ps = new StringBuilder().append("<Placemark>\r\n")
				.append("<name>") .append(placename)  .append("</name>\r\n")
				.append("<description>") .append(description) .append("</description>\r\n")
				.append("<styleUrl>#") .append(hexcolor) .append("</styleUrl>\r\n")
				.append("<Polygon>\r\n<tessellate>1</tessellate>\n<outerBoundaryIs>\n<LinearRing>\n")
				.append("<coordinates>\r\n").append(spoy.toString()).append("\r\n")
				.append("</coordinates>\n</LinearRing>\n</outerBoundaryIs>\n</Polygon>\n</Placemark>\n");
		return ps;
	}

	public  StringBuilder calculateStyles(KMLAssit kmlAssit){
		StringBuilder styleBuilder = new StringBuilder();
		String defaultHexcolorId = defaultHexcolor+"_m";
		String defaultStyle = "<Style id=\"" + defaultHexcolorId + "\">\r\n" +
				"<IconStyle>\r\n" +
				"<scale>0.0</scale>\r\n" +
				"<Icon>\r\n" +
				"<href>http://maps.google.com/mapfiles/kml/shapes/placemark_circle_highlight.png</href>\r\n" +
				"</Icon>\r\n" +
				"<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>\r\n" +
				"</IconStyle>\r\n" +
				"<LineStyle>\r\n" +
				"<color>" + defaultHexcolor + "</color>" +
				"</LineStyle>\r\n" +
				"<PolyStyle>\r\n" +
				"<color>" + defaultHexcolor + "</color>" +
				"</PolyStyle>\r\n" +
				"</Style>\r\n";
		styleBuilder.append(defaultStyle);
		styleBuilder.append(calculateStylerByStylerRulers(kmlAssit));
		return styleBuilder;
	}
	private StringBuilder calculateStylerByStylerRulers(KMLAssit kmlAssit) {
		List<StyleRule> styleRules = kmlAssit.getStyleRules();
		StringBuilder stylers = new StringBuilder();
		for(StyleRule styleRule : styleRules){
			String fill = (styleRule.getFillOpacity()+styleRule.getFill().replace("#","")).toLowerCase();
			String fill_ = ColorTransform.transformToGeoStyleFill(fill);
			String line_fill = (styleRule.getLineOpacity()+styleRule.getLineFill().replace("#","")).toLowerCase();
			String line_fill_ = ColorTransform.transformToGeoStyleFill(line_fill);
			String style = "<Style id=\"" + fill_ + "_m\">\r\n" +
					"<IconStyle>\r\n" +
					"<scale>0.0</scale>\r\n" +
					"<Icon>\r\n" +
					"<href>http://maps.google.com/mapfiles/kml/shapes/placemark_circle_highlight.png</href>\r\n" +
					"</Icon>\r\n" +
					"<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>\r\n" +
					"</IconStyle>\r\n" +
					"<LineStyle>\r\n" +
					"<color>" + line_fill_ + "</color>" +
					"</LineStyle>\r\n" +
					"<PolyStyle>\r\n" +
					"<color>" + fill_ + "</color>" +
					"</PolyStyle>\r\n" +
					"</Style>\r\n";
			stylers.append(style);
		}
		return stylers;
	}

}