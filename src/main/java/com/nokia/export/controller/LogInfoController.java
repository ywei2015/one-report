package com.nokia.export.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.export.service.QuerySqlParamModelService;
import com.nokia.export.util.DBQuery;

@RestController
public class LogInfoController {
	private static Logger  logger = LoggerFactory.getLogger(LogInfoController.class);
	@Autowired
	QuerySqlParamModelService sqlparmModelService;
	@Autowired
	private Environment env;

	@RequestMapping(value = "inserLogInfo", method = { RequestMethod.POST, RequestMethod.GET })
	public String inserLogInfo(HttpServletRequest request, HttpServletResponse response, @RequestParam("db") String db,
			@RequestParam("sqlName") String sqlName, @RequestParam("sqlType") String sqlType,
			@RequestParam("project") String project, @RequestParam("module") String module,
			@RequestParam("modular") String modular,
			@RequestParam("operation") String operation,
			@RequestParam("create_by") String create_by,
			@RequestParam("content_info") String content_info
			) throws Exception {
		DBQuery dbQuery = new DBQuery();
		int count = 0;
		// 获取IP
		String remoteAddr = getIpAddr(request);
		if(content_info!=null && !"".equals(content_info)){
			content_info=content_info.replace("'", "\"");
		}
		// 创建时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curDate = df.format(new Date());
		String keys = modular + ";;;" + operation+";;;"+content_info+";;;"+create_by+";;;"+remoteAddr + ";;;" + curDate;
		try {
			String sql = sqlparmModelService.querySql(sqlName, sqlType, project, module, keys);
			count = dbQuery.excSql(db, sql, env);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		if (count > 0) {
			return "suc";
		} else {
			return "fail";
		}
	}

	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		logger.info("IP0:"+ip);
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
			logger.info("IP1:"+ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
			logger.info("IP2:"+ip);
		}
		if (ip == null || ip.length() == 0 || "X-Real-IP".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
			logger.info("IP3:"+ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
			logger.info("IP4:"+ip);
		}
		return ip;
	}

}
