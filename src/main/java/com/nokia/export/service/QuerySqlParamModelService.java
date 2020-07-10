package com.nokia.export.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.nokia.export.util.DBQuery;
import com.nokia.export.util.StringTools;

@Service
public class QuerySqlParamModelService {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private Environment env;
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
				if (keys != null && keys != "" && keys.split(";;;").length > 0) {
					Object[] pars = keys.split(";;;");
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
