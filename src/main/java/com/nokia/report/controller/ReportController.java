package com.nokia.report.controller;

import com.alibaba.fastjson.JSONObject;
import com.nokia.export.enums.PhantomjsTaskEnums;
import com.nokia.export.pojo.R;
import com.nokia.export.util.DateUtil;
import com.nokia.export.util.FileOperation;
import com.nokia.export.util.StringTools;
import com.nokia.report.pojo.ReportMonitor;
import com.nokia.report.service.yiqing.YiQingReportMonitor;
import com.nokia.report.service.yiqing.YiQingReportService;
import com.nokia.report.task.YiQingReportTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.nokia.report.service.yiqing.YiQingReportService.*;

@RestController
public class ReportController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private YiQingReportService yiQingReportService;
	@Autowired
	private YiQingReportTask yiQingReportTask;

	@PostMapping("report/yq")
	public JSONObject doReportYQ(HttpServletRequest request, @RequestParam String city, @RequestParam String startTime, @RequestParam String endTime
			, @RequestParam String userType, @RequestParam String userInput , @RequestParam String userId){

		JSONObject res = new JSONObject();
		String userId_ = (String) request.getSession().getAttribute("userId");
		if(userId_== null){
			userId_ = userId;
		}
		String userFlag = userInput;
		if (userInput.equalsIgnoreCase(ALL)){
			userFlag = userType;
		}
		String date_star = startTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String date_end = endTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String path_ =  city + "/" + userFlag+ "/" +userFlag+"_" ;
		String day_str = date_star+"_"+date_end;
		String zipPath = ZIP_PATH + city + "/" + userId+ "/" + userFlag+"_FullReport_"+date_star + "_" + date_end + ".zip";
		String key = city+"-" + userFlag +"-"+date_star + "-" + date_end;

		if(YiQingReportMonitor.isTaskRunning(key) ){
			res.put("code", PhantomjsTaskEnums.RUNNING.getCode());
			res.put("msg",PhantomjsTaskEnums.RUNNING.getMsg());
			res.put("progress",YiQingReportMonitor.getTaskProgress(key));
		}//判断wenjian是否存在
		else if (yiQingReportService.fileExist(zipPath,path_,day_str,userId_)) {
			res.put("code", PhantomjsTaskEnums.FINISHED.getCode());
			res.put("msg", PhantomjsTaskEnums.FINISHED.getMsg());
			res.put("data", StringTools.transformUrl(zipPath));
		}else {
			res.put("code", PhantomjsTaskEnums.RUNNING.getCode());
			res.put("msg","task created");
			ReportMonitor reportMonitor = new ReportMonitor();
			reportMonitor.setUserId(userId_);
			if (YiQingReportMonitor.addTask(key,reportMonitor)){
				yiQingReportService.doReport(city, startTime,endTime,userInput,userType,userId_);
			}else {
				res.put("msg",PhantomjsTaskEnums.RUNNING.getMsg());
			}
		}
		return res;
	}

	@PostMapping("report/yq_excel")
	public R doReportYQPPT(HttpServletRequest request, @RequestParam String city, @RequestParam String startTime, @RequestParam String endTime
			, @RequestParam String userType, @RequestParam String userInput , @RequestParam String userId){
		logger.info("param:[{}]",city+"-"+startTime+"-"+endTime+"-"+userType+"-"+userInput+"-"+userId);
		R r = R.ok();
		String userId_ = (String) request.getSession().getAttribute("userId");
		if(userId_== null){
			userId_ = userId;
		}
		String userFlag = userInput;
		if (userInput.equalsIgnoreCase(ALL)){
			userFlag = userType;
		}
		startTime = DateUtil.dayStart(startTime);
		endTime = DateUtil.dayEnd(endTime);
		String date_star = startTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String date_end = endTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String excel_zip_path =  ZIP_PATH+city + "/" + userId+ "/" +"excel/"+ userFlag+"_Report_"+date_star + "_" + date_end + ".zip";
		r.put("data", StringTools.transformUrl(excel_zip_path));
		if (!FileOperation.fileExist(excel_zip_path)) {
			String tablePath = EXCEL_PATH + city + "/" + userFlag+ "/" +userFlag +"_" ;
			yiQingReportService.doReportPPT(city, startTime,endTime,userInput,userType,userId_,excel_zip_path,tablePath,date_star+"_"+date_end);
		}
		return r;
	}

	@PostMapping("report/clear_excel")
	public R clearExcelCache() {
		R re = R.ok();
		yiQingReportService.clearExcelCache();
		return re;
	}


	@PostMapping("report/test")
	public R testSchudle() {
		R re = R.ok();

		yiQingReportTask.work();
		return re;
	}

}
