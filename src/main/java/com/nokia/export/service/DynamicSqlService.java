package com.nokia.export.service;

import com.nokia.export.util.StringTools;
import com.nokia.report.mapper.DynamicSqlMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
public class DynamicSqlService {
	
	@Resource
	private DynamicSqlMapper dynamicSqlMapper;

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public List<HashMap<String, Object>> getListBySql(String sqlStr) {
		List<HashMap<String, Object>> list= dynamicSqlMapper.getListBySql(sqlStr);
		return list;
	}

	public List<String> getStrsBySql(String sqlStr) {
		List<String> list= dynamicSqlMapper.getStrsBySql(sqlStr);
		return list;
	}

	public String querySql(String sqlName, String sqlType, String project, String module, String keys) {
		String sql = dynamicSqlMapper.querySqlByParam(sqlName,sqlType,project,module);
		if(StringUtils.isNotBlank(sql)){
			if (StringUtils.isNotBlank(keys) && keys.contains(";")) {
				Object[] pars = keys.split(";");
				sql = StringTools.getFormatSql(sql, pars);
			}
		}
		logger.info("sql:[{}]",sql);
		return sql;
	}


	public List<HashMap<String, Object>> queryLongitudeRange() {
		return dynamicSqlMapper.queryLongitudeRange();
	}
}
