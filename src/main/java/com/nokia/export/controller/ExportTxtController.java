package com.nokia.export.controller;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.nokia.export.util.CommonPath;
import com.nokia.export.util.DBQuery;
import com.nokia.export.util.FileOperation;
import com.nokia.export.util.StringTools;

import net.sf.json.JSONObject;
@RestController
public class ExportTxtController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	@Autowired
	private Environment env;
	private FileOperation operation;
	@RequestMapping(value = "exportTxt_bysqlName", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<JSONObject> exportTxt_bysqlName(
			HttpServletResponse response,
			@RequestParam("fileName") String fileName,
			@RequestParam(value = "title", required = false) String titleStr,
			@RequestParam("sqlName") String sqlName,
			@RequestParam("sqlType") String sqlType,
			@RequestParam(value ="dateType",required = false) String dateType,
			@RequestParam("project") String project,
			@RequestParam("module") String module,
			@RequestParam("table") String table,
			@RequestParam(value="sqlId",required = false) String sqlId,
			@RequestParam(value = "keys", required = false) String keys){
		fileName = fileName + ".sql";
		DBQuery dbQuery =null; 
		JSONObject res = new JSONObject();
		logger.info("fileName:"+fileName);
		logger.info("titleStr:"+titleStr);
		logger.info("sqlName:"+sqlName);
		logger.info("sqlType:"+sqlType);
		logger.info("project:"+project);
		logger.info("module:"+module);
		logger.info("dateType:"+dateType);
		try {
			dbQuery=new DBQuery();
			//查询SQL
			String db="dbjqgh";
			//根据条件查询所需SQL
			if(dateType!=null && !"".equals(dateType)){
				sqlName=sqlName+"_"+dateType;
				logger.info("new sqlName:"+sqlName);
			}
			String sql =querySql(sqlName, sqlType, project, module, keys);
			
			if("zhongchou".equals(project)){
				db="dbzc";
			}
			logger.info("excel的sql:" + sql);
			ResultSet rs = dbQuery.getRes(db, sql, env);
			int columnCount = rs.getMetaData().getColumnCount();
			List<Object[]> results = new ArrayList<>();
			String[] title;
			StringBuffer titilestr= new StringBuffer();
			titilestr.append("insert into ").append(table).append("(");
			if (titleStr != null) {
				title = titleStr.split(",");
			} else {
//				title = new String[columnCount];
				for (int i = 1; i < columnCount + 1; i++) {
					String colunmName = rs.getMetaData().getColumnName(i);

						if(i==1){
							titilestr.append(colunmName);
						}else{
							titilestr.append(",").append(colunmName);
						}
					
					
				}
				titilestr.append(") values(");
				
			}
			StringBuffer str= new StringBuffer();
			while (rs.next()) {
				Object[] result = new Object[columnCount];
				str.append(titilestr);
				String resultstr="";
				for (int i = 1; i < columnCount + 1; i++) {
					if (rs.getObject(i) == null) {
//						result[i - 1] = "";
						resultstr="";
					} else {
						resultstr = rs.getObject(i).toString();
						resultstr=resultstr.replace("'", "''");
					}
					if(i==1 && sqlId!=null && !"".equals(sqlId)){
						str.append(sqlId);
					}else{
						if(i==1){
							str.append("'").append(resultstr).append("'");
						}else{
							str.append(",").append("'").append(resultstr).append("'");
						}
					}
					
				}
				str.append(");").append("\n");
				results.add(result);
			}

			dbQuery.close();
//			if(results.size()>0) {
				int status =0; //operateExcel.generateExcel(results, title, fileName);
				String filePath = CommonPath.getConfig("ExcelPath") + "/" + fileName;
				File file = new File(filePath);
				operation.createFile(file);
				operation.writeTxtFile(str.toString(), file,true);
				res.put("status", status);
				res.put("filePath", filePath);
				return ResponseEntity.ok(res);
//			}else {

//			}
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
	
	public String querySql(	String sqlName,
			String sqlType,
			String project,
			String module,
			 String keys){
		String sqlContext="";
		String db="dbjqgh";
		String sql ="select a.sql_context from "
				+ "sql_param_model a where a.sql_type ='"+sqlType+"'"
				+ " and a.project='"+project+"'"
				+ "and a.module='"+module+"'"
				+ "and a.sql_name='"+sqlName+"'";
		logger.info("paramsql:"+sql);
		DBQuery dbQuery = new DBQuery();
		ResultSet rs =null;
		try {
			rs = dbQuery.getRes(db, sql, env);
			if(rs!=null){
				while (rs.next()) {
					logger.info(rs.toString());
					sqlContext=rs.getString("sql_context");
				}
			}
			if(!"".equals(sqlContext)){
				if (keys != null && keys != "" && keys.split(";").length > 0) {
					Object[] pars = keys.split(";");
					sqlContext = StringTools.getFormatSql(sqlContext, pars);
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}finally {
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e.getMessage());
				}
			}
			if(dbQuery!=null){
				try {
					dbQuery.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e.getMessage());
				}
			}
		}
		logger.info("sqlContext:"+sqlContext);
		return sqlContext;
	}
}
