package com.nokia.report.thread;

import com.nokia.export.util.FileOperation;
import com.nokia.export.service.DynamicSqlService;
import com.nokia.report.service.yiqing.YiQingReportMonitor;
import com.nokia.report.util.ProductConstant;
import com.nokia.report.util.SqlTableReadUtil;
import com.nokia.report.util.excel.CustomXSSFWorkbook;
import com.nokia.report.util.excel.ExcelTools;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 一键报告表格信息
 */
public class YQTableContextGatherThread implements Callable {

	private String city;
	private String start;
	private String end;
	private String userInput;
	private String userId;
	private String userType;
	private String path;
	private String key;
	private DynamicSqlService dynamicSqlService;
	//public final static String[] ZC_EPIDEMIC_MONITOR_EXPORTS = {"zc_epidemic_monitor_export_1","zc_epidemic_monitor_export_2","zc_epidemic_monitor_export_3","zc_epidemic_monitor_export_4"};
	//public final static String[] FILE_NAMES = {"Suspected Infectious Areas","Location Tracking","Trajectory","Close Contacts"};


	private static final Logger logger = LoggerFactory.getLogger(YQTableContextGatherThread.class);

	public YQTableContextGatherThread(DynamicSqlService dynamicSqlService, String city, String start, String end, String userInput,String userId, String userType, String path, String key) {
		this.dynamicSqlService = dynamicSqlService;
		this.city = city;
		this.start = start;
		this.end = end;
		this.userInput = userInput;
		this.userId = userId;
		this.userType = userType;
		this.dynamicSqlService = dynamicSqlService;
		this.path = path;
		this.key = key;
	}

	@Override
	public String call() throws Exception {
		String status = ProductConstant.FINISHED;
		String date_star = start.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String date_end = end.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String date_str = date_star+ "_" + date_end;
		List<HashMap> configList = SqlTableReadUtil.readSqlConf();
		for (int i = 0; i < configList.size(); i++) {
			HashMap hashMap = configList.get(i);
			String savePath = path + hashMap.get("FileName") +"_"+ date_str + ".xlsx";
			if (FileOperation.fileExist(savePath)) {
				YiQingReportMonitor.addTaskProgress(key,1);
				continue;
			}
			String sqlName = (String) hashMap.get("sqlName");
			String keys =";"+city+";"+start+";"+end+";"+userType+";;"+userInput+";"+userId;
			String sql = dynamicSqlService.querySql(sqlName,"oracle","vdt","interval",keys);
			if (StringUtils.isBlank(sql)) {
				logger.error("sql:[{}] is blank",sqlName);
				YiQingReportMonitor.addTaskProgress(key,1);
				continue;
			}
			List<HashMap<String, Object>> hashMaps = dynamicSqlService.getListBySql(sql);
			logger.info("obtain data:[{}] count:[{}]",sqlName,hashMaps.size());
			CustomXSSFWorkbook customXSSFWorkbook = new CustomXSSFWorkbook();
			if (!hashMaps.isEmpty()) {
				ExcelTools.renderTable(customXSSFWorkbook,hashMaps,null,null);
			}else {
				customXSSFWorkbook.createSheet("Sheet0");
			}
			ExcelTools.writeWorkBook(customXSSFWorkbook,savePath);
			YiQingReportMonitor.addTaskProgress(key,1);
		}
		return status;
	}

}
