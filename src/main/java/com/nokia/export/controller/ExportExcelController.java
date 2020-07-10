package com.nokia.export.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.nokia.export.service.ExportExcelService;
import com.nokia.export.util.DBUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.export.service.OperateExcel;
import com.nokia.export.util.CommonPath;
import com.nokia.export.util.DBQuery;
import com.nokia.export.util.StringTools;

import net.sf.json.JSONObject;

@RestController
public class ExportExcelController {
	@Autowired
	private Environment env;
	@Autowired
	private OperateExcel operateExcel;
	@Autowired
	private ExportExcelService exportExcelService;

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@RequestMapping(value = "exportExcel", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<JSONObject> exportExcel(HttpServletResponse response,@RequestParam("db") String db, @RequestParam("sql") String sql,
			@RequestParam("fileName") String fileName, @RequestParam(value = "title", required = false) String titleStr)
			throws Exception {
		fileName = fileName + ".xls";
		DBQuery dbQuery = new DBQuery();
		logger.info("excel的sql:" + sql);
		ResultSet rs = dbQuery.getRes(db, sql, env);
		int columnCount = rs.getMetaData().getColumnCount();
		List<Object[]> results = new ArrayList<>();
		while (rs.next()) {
			Object[] result = new Object[columnCount];
			for (int i = 1; i < columnCount + 1; i++) {
				if (rs.getObject(i) == null) {
					result[i - 1] = "";
				} else {
					result[i - 1] = rs.getObject(i);
				}
			}
			results.add(result);
		}
		String[] title;
		if (titleStr != null) {
			title = titleStr.split(",");
		} else {
			title = new String[columnCount];
			for (int i = 1; i < columnCount + 1; i++) {
				title[i - 1] = rs.getMetaData().getColumnName(i);
			}
		}
		dbQuery.close();
		if(results.size()>0) {
			int status = operateExcel.generateExcel(results, title, fileName);
			JSONObject res = new JSONObject();
			String filePath = CommonPath.getConfig("ExcelPath") + "/" + fileName;
			res.put("status", status);
			res.put("filePath", filePath);
			return ResponseEntity.ok(res);
		}else {
			JSONObject res = new JSONObject();
			res.put("status", "fail");
			res.put("filePath", "undefined");
			return ResponseEntity.ok(res);
		}
		
	}
	
	@RequestMapping(value = "exportExcel_bysqlName", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<JSONObject> exportExcel_bysqlName(
			HttpServletResponse response,
			@RequestParam("fileName") String fileName,
			@RequestParam(value = "fileType", required = false) String fileType,
			@RequestParam(value = "title", required = false) String titleStr,
			@RequestParam("sqlName") String sqlName,
			@RequestParam("sqlType") String sqlType,
			@RequestParam(value ="dateType",required = false) String dateType,
			@RequestParam("project") String project,
			@RequestParam("module") String module,
			@RequestParam(value = "keys", required = false) String keys,
			@RequestParam(value = "ds", required = false) String ds){
		DBQuery dbQuery =null;
		JSONObject res = new JSONObject();
		logger.info("fileName:"+fileName);
		logger.info("fileType:"+fileType);
		logger.info("titleStr:"+titleStr);
		logger.info("sqlName:"+sqlName);
		logger.info("sqlType:"+sqlType);
		logger.info("project:"+project);
		logger.info("dateType:"+dateType);
		logger.info("keys:"+keys);
		logger.info("ds:"+ds);
		logger.info("dateType:"+dateType);
		try {
			dbQuery=new DBQuery();
			//查询SQL
			String db;
			//根据条件查询所需SQL
			if(StringUtils.isNotBlank(dateType)){
				sqlName=sqlName+"_"+dateType;
				logger.info("new sqlName:"+sqlName);
			}
			String sql = DBUtil.querySql(sqlName, sqlType, project, module, keys,env);
			
			if(StringUtils.isBlank(ds)){
				db="dbzc";
			}else{
				db=ds;
			}
			logger.info("excel的sql:" + sql);
			ResultSet rs = dbQuery.getRes(db, sql, env);

			int status;
			if ("csv".equalsIgnoreCase(fileType)){
				fileName = fileName + ".csv";
				status = exportExcelService.generateSVC(rs, fileName);
			}else {
				fileName = fileName + ".xls";
				int columnCount = rs.getMetaData().getColumnCount();
				List<Object[]> results = new ArrayList<>();
				while (rs.next()) {
					Object[] result = new Object[columnCount];
					for (int i = 1; i < columnCount + 1; i++) {
						if (rs.getObject(i) == null) {
							result[i - 1] = "";
						} else {
							result[i - 1] = rs.getObject(i);
						}
					}
					results.add(result);
				}
				String[] title;
				if (titleStr != null) {
					title = titleStr.split(",");
				} else {
					title = new String[columnCount];
					for (int i = 1; i < columnCount + 1; i++) {
						title[i - 1] = rs.getMetaData().getColumnName(i);
					}
				}
				status = operateExcel.generateExcel(results, title, fileName);
			}
			dbQuery.close();
			String filePath = CommonPath.getConfig("ExcelPath") + "/" + fileName;
			res.put("status", status);
			res.put("filePath", filePath);
			return ResponseEntity.ok(res);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage());
			res.put("status", "fail");
			res.put("filePath", "undefined");
			res.put("msg", e.getMessage());
			return ResponseEntity.ok(res);
		}finally {
			if(dbQuery!=null){
				try {
					dbQuery.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	

	
	
}
