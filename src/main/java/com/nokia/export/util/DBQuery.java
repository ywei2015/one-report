package com.nokia.export.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StopWatch;

public class DBQuery {
	private Connection con;
	private PreparedStatement ps;
	private ResultSet rs;
	private String type_;
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public ResultSet getRes(String db,String sql,Environment env) throws Exception {
		Class.forName(env.getProperty(db+".driver-class-name"));
        String url = env.getProperty(db+".url");
        String username = env.getProperty(db+".username");
        String password = env.getProperty(db+".password");
        String type=env.getProperty(db+".type");
        logger.info(url+","+username+","+password+","+env.getProperty(db+".driver-class-name")+","+type);
        con = DriverManager.getConnection(url, username, password);
        if("gp".equals(type)){
        	 logger.info("type==1"+type);
        	 ps = con.prepareStatement(sql);
        }else{
        	logger.info("type==2"+type);
        	 ps = con.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY);
        }
        ps.setFetchSize(50000);
        rs = ps.executeQuery();
        return rs;
	}

	public ResultSet getResByConnection(String sql) throws Exception {
		logger.info("execute sql : {}", sql);
		if("gp".equals(type_)){
			ps = con.prepareStatement(sql);
		}else{
			ps = con.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY);
		}
		ps.setFetchSize(50000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		rs = ps.executeQuery();
		stopWatch.stop();
		logger.info("select end in time :{}",stopWatch.getTotalTimeSeconds());
		return rs;
	}

	/**
	 * 执行更新语句
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public int excSql(String db,String sql,Environment env) throws Exception {
		logger.info("update start");
		Class.forName(env.getProperty(db+".driver-class-name"));
        String url = env.getProperty(db+".url");
        String username = env.getProperty(db+".username");
        String password = env.getProperty(db+".password");
        String type=env.getProperty(db+".type");
        logger.info(url+","+username+","+password+","+env.getProperty(db+".driver-class-name")+","+type);
        con = DriverManager.getConnection(url, username, password);
		int count=0;
		ps = con.prepareStatement(sql);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		count = ps.executeUpdate();
		stopWatch.stop();
		logger.info("update end in time :{}",stopWatch.getTotalTimeSeconds());
		return count;
	}
	
	public Connection getCon(String db,Environment env) throws Exception {
		Class.forName(env.getProperty(db+".driver-class-name"));
        String url = env.getProperty(db+".url");
        String username = env.getProperty(db+".username");
        String password = env.getProperty(db+".password");
		type_ = env.getProperty(db+".type");
        logger.info("==="+url+","+username+","+password+","+env.getProperty(db+".driver-class-name"));
        con = DriverManager.getConnection(url, username, password);
        return con;
	}
	
	public void close() throws SQLException {
		//释放资源
		if (rs != null) {
			rs.close();
		}
		if (ps != null) {
			ps.close();
		}
		if (con != null) {
			con.close();
		}
	}

	public void closeSource() throws SQLException {
		//释放资源
		if (rs != null) {
			rs.close();
		}
		if (ps != null) {
			ps.close();
		}
	}

}
