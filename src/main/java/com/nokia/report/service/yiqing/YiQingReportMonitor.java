package com.nokia.report.service.yiqing;

import com.nokia.export.enums.PhantomjsTaskEnums;
import com.nokia.report.pojo.ReportMonitor;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

public class YiQingReportMonitor {

	private static ConcurrentHashMap<String,ReportMonitor> monitorData = new ConcurrentHashMap<String, ReportMonitor>();


	public static Boolean isTaskRunning(String key) {
		Integer stauts = getTaskStatus(key);
		if(stauts != null && stauts.equals(PhantomjsTaskEnums.RUNNING.getCode()) ){
			return true;
		}
		return  false;
	}

	private static Integer getTaskStatus(String key) {
		ReportMonitor reportMonitor = monitorData.get(key);
		if (reportMonitor != null){
			return reportMonitor.getStatus();
		}
		return null;
	}

	public static boolean addTask(String key, ReportMonitor reportMonitor) {
		Integer status = getTaskStatus(key);
		if (status == null || PhantomjsTaskEnums.FINISHED.getCode().equals(status) ) {
			monitorData.put(key,reportMonitor);
			return true;
		}else {
			return false;
		}
	}

	public static void modifyTaskStatus(String key, Integer code) {
		monitorData.get(key).setStatus(code);
	}

	public static void modifyTaskFinish(String key) {
		ReportMonitor reportMonitor = monitorData.get(key);
		reportMonitor.setStatus(PhantomjsTaskEnums.FINISHED.getCode());
		reportMonitor.setMsg(PhantomjsTaskEnums.FINISHED.getMsg());
		reportMonitor.setProgress("100");
	}

	public static void modifyTaskProgress(String key, String msg, Integer i) {
		ReportMonitor reportMonitor = monitorData.get(key);
		reportMonitor.setStatus(PhantomjsTaskEnums.RUNNING.getCode());
		reportMonitor.setMsg(msg);
		reportMonitor.setProgress(i.toString());
	}

	public static String getTaskProgress(String key) {
		ReportMonitor reportMonitor = monitorData.get(key);
		Integer total = reportMonitor.getTotal();
		if (total == null || total == 0) {
			return "0";
		}
		if (reportMonitor.getStatus().equals(PhantomjsTaskEnums.FINISHED.getCode())){
			return "100";
		}
		Integer number = reportMonitor.getNumber() ;
		Double d = ((double)number/total)*0.9*100;
		String d_ = d.toString();
		BigDecimal bd = new BigDecimal(d_);
		BigDecimal  bd2 = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
		reportMonitor.setProgress(bd2.toString());
		return reportMonitor.getProgress();
	}

	public static void initTaskProgress(String key, int size) {
		ReportMonitor reportMonitor = monitorData.get(key);
		reportMonitor.setTotal(size);
		reportMonitor.setMsg("collect picture...");
	}

	public static void addTaskProgress(String key, int i) {
		if (key != null) {
			ReportMonitor reportMonitor = monitorData.get(key);
			Integer number = reportMonitor.getNumber() + i;
			reportMonitor.setNumber(number);
		}
	}
}
