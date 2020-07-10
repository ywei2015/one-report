package com.nokia.report.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nokia.export.util.CommonPath;
import com.nokia.export.util.FileOperation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class SqlTableReadUtil {
	private static Integer size = 4;
	public static List<HashMap> hashMaps;
	private static final String path = CommonPath.getProjectRootPath()+"excelSqlFileNameConfig.json";

	private static void initMap(){
		//hashMaps.clear();
		List<HashMap> list = new LinkedList<>();
		String json = FileOperation.readFile(path);
		List<JSONObject> jsonObjects = (List<JSONObject>) JSONArray.parse(json);
		jsonObjects.forEach(jsonObject -> {
			HashMap<String,String> hashMap = new LinkedHashMap<>(2);
			hashMap.put("fileName",jsonObject.getString("fileName"));
			hashMap.put("sqlName",jsonObject.getString("sqlName"));
			list.add(hashMap);
		});
		hashMaps = list;
		size = hashMaps.size();
	}

	public static synchronized List<HashMap> readSqlConf(){
		initMap();
		return hashMaps;
	}

}
