package com.nokia.export.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.nokia.report.service.yiqing.YiQingReportService.PASSWORD;

public class DBUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

    private final static String DATA_BASE_TYPE= CommonPath.getConfig("databaseType");

    public static int getCount(DBQuery dbQuery,String sql) throws Exception {
        int count =0;
        sql = "select count(*) as count from ("+sql+")";
        ResultSet rs = dbQuery.getResByConnection(sql);
        if (rs.next()){
            count = rs.getInt("count");
        }
        dbQuery.closeSource();
        return count;
    }

    public static String getPageSql(String sqlStr, Integer limit ,Integer page){
        if (page != null && limit != null){
            int begin = (page)*limit;
            int end = begin + limit;
            if ("gp".equalsIgnoreCase(DATA_BASE_TYPE)) {
                sqlStr ="select * from("+sqlStr+") a"  +" limit "+end+" OFFSET "+begin;
            }else {
                sqlStr = "select * from(select a.*,rownum rn from("+sqlStr+") a where rownum<="+end+") where rn>"+begin;

            }
        }
        return sqlStr;
    }

    public static String querySql(String sqlName, String sqlType, String project, String module, String keys, Environment env){
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
                if (StringUtils.isNotBlank(keys) && keys.contains(";")) {
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
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
            if(dbQuery!=null){
                try {
                    dbQuery.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        }
        logger.info("sqlContext:"+sqlContext);
        return sqlContext;
    }

    public static String findPasswordByUserId(String userId,Environment env) {

        String password = PASSWORD;
        String sql = "select password from pure_user_file_password where user_id = "+userId+"";
        DBQuery dbQuery = new DBQuery();
        ResultSet rs =null;
        try {
            rs = dbQuery.getRes("portal", sql,  env);
            if(rs!=null){
                while (rs.next()) {
                    logger.info(rs.toString());
                    password=rs.getString("password");
                }
            }
        } catch (SQLException e) {
            password = PASSWORD;
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            password = PASSWORD;
            logger.error(e.getMessage());
        } finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
            if(dbQuery!=null){
                try {
                    dbQuery.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        }
        return password;
    }
}
