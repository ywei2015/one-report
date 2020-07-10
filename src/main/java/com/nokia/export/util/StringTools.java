package com.nokia.export.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.text.MessageFormat;
import java.util.Map;

public class StringTools {

	private static Logger logger = LoggerFactory.getLogger(StringTools.class);
	/**
	 * @Title: getFormatSql
	 * @Description: TODO(格式化SQL语句)
	 * @param: @param
	 *             sql
	 * @param: @param
	 *             object
	 * @param: @return
	 * @return: String
	 */
	public static String getFormatSql(String sql, Object[] object) {
		return MessageFormat.format(sql.replaceAll("'", "''"), object).replaceAll("\\{\\d+\\}","");
	}

	public static String getReportFetchSql(String sql, Map<String,Object> map){
		for (Map.Entry<String,Object> entry : map.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue().toString();
			String pattern = "\\$\\{"+ key +"}";
			sql = sql.replaceAll(pattern,value);
		}
		return sql;
	}

	/**
	 * 为geo sql 设置传入参数
	 * @param sql
	 * @param param
	 * @param filter
	 * @param geometryName 坐标类型关键字
	 * @return
	 */
	public static String getFormatSqlForGeo(String sql, String param,String filter, String geometryName) {
		if (StringUtils.isNotBlank(param)){
			String[] params = param.split(";");
			for (String string : params) {
				String[] key = string.split(":");
				if (StringUtils.isNotBlank(key[0])){
					if (sql.contains("%"+key[0]+"%")){
						sql = sql.replaceAll("%"+key[0]+"%", key[1]);
					}
				}
			}
		}
		sql = formatSqlForGeo(sql, geometryName);
		if (StringUtils.isNotBlank(filter)){
			sql = "select * from (" + sql +") where " + filter;
		}
		return sql;
	}

	private static String formatSqlForGeo(String sql, String geometryName) {
		String gs = "SDO_GEOMETRY.GET_WKT({0}) as SPY";
		String upper_name = geometryName.toUpperCase();
		String lower_name = geometryName.toLowerCase();
		String name = "";
		if (sql.contains(upper_name)){
			name = upper_name;
			gs = MessageFormat.format(gs,upper_name);
		}else if (sql.contains(lower_name)){
			name = lower_name;
			gs = MessageFormat.format(gs,lower_name);
		}
		// 去掉SDO_GEOMETRY前面的别名
		int index  = sql.indexOf(name);
		String bs = sql.substring(index -1,index);
		StringBuilder str_replace = new StringBuilder(name);
		if (bs.equals(".")){
			String str = sql.substring(index -2,index);
			str_replace = new StringBuilder(str).append(str_replace);
		}
		sql = sql.replace(str_replace,gs);
		return sql;
	}

	public static String transformUrl(String url){
		if (url.contains("#")) {
			url = url.replaceAll("#","%23");
		}
		return url;
	}

	public static String reTransformUrl(String url){
		if (url.contains("%23")) {
			url = url.replaceAll("%23","#");
		}
		return url;
	}

}
