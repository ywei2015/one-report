package com.nokia.kml.service;

import com.nokia.kml.model.KMLAssit;
import com.nokia.kml.model.StyleRule;
import com.nokia.export.util.ColorTransform;
import com.nokia.export.util.DBQuery;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCreateKml {
	protected static String defaultHexcolor = ColorTransform.FromColorToHex(Color.blue);
	public abstract  String CreateKmlContent(String sql, String kmlFilePath, DBQuery dbQuery, KMLAssit kmlAssit) throws Exception;
	public abstract StringBuilder calculateStyles(KMLAssit kmlAssit);



	protected String getFill(Double value, List<StyleRule> styleRules, HashMap<String,String> fillMap) {
		String fill_;
		String key = value.toString();
		String v = fillMap.get(key);
		if (StringUtils.isNotBlank(v)){
			return v;
		}else {
			List<StyleRule> styleRule2s = styleRules.stream().filter(styleRule -> styleRule.isInStyleRule(value))
					.collect(Collectors.toList());
			if (styleRule2s.size() > 0){
				StyleRule styleRule_ = styleRule2s.get(0);
				fill_ = styleRule_.getFillOpacity()+styleRule_.getFill().replace("#","");
				fill_ = ColorTransform.transformToGeoStyleFill(fill_).toLowerCase();
			}else {
				fill_ = defaultHexcolor;
			}
			fillMap.put(key,fill_);
		}
		return fill_;
	}
}