package com.nokia.report.thread;

import com.alibaba.fastjson.JSONObject;
import com.nokia.export.util.CommonPath;
import com.nokia.export.util.FileOperation;
import com.nokia.report.pojo.YQParamDTO;
import com.nokia.report.service.yiqing.YiQingReportMonitor;
import com.nokia.report.service.yiqing.YiQingReportService;
import com.nokia.report.util.PhantomjsTools;
import com.nokia.report.util.ProductConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
/**
 * 自动截图
 */
public class YQPhantomPictureGatherThread implements Callable {
	private static final String NO_DATA_CODE = "no data";
	private final static String YQ_PTM_URL = CommonPath.getConfig("YQPhantomUrl")+"%Akeys=";
	private final static String YQ_PTM_FILE = CommonPath.COMMON_PATH+"phantomjs/test.js";
	private final static String PHANTOMJS_CONFIG_FILE = CommonPath.COMMON_PATH+"phantomjs/config.json";
	private YQParamDTO yqParamDTO;
	private String path;
	private String key;
	private String info_params;
	private static final int maxTaskRetryTimes = 3;
	private int taskRetryTimes;
	protected String res_msg = "";
	protected String phantom_params;

	private static final Logger logger = LoggerFactory.getLogger(YQPhantomPictureGatherThread.class);

	public YQPhantomPictureGatherThread(YQParamDTO yqParamDTO, String path, String key) {
		this.yqParamDTO = yqParamDTO;
		this.info_params = yqParamDTO.toString();
		this.path = path.replace("all",yqParamDTO.getUserInput());
		this.key = key;
	}

	@Override
	public String call() throws Exception {
		String status = ProductConstant.FINISHED;
		try {
			String blankItem = fileExist();
			if (StringUtils.isBlank(blankItem)){
				YiQingReportMonitor.addTaskProgress(key,1);
				return status;
			}
			while(taskRetryTimes <= maxTaskRetryTimes){
				calculatePhantomParam(blankItem);
				status = PhantomjsTools.printUrlScreenjpg(this);
				blankItem = fileExist();
				if (StringUtils.isBlank(blankItem)){
					YiQingReportMonitor.addTaskProgress(key,1);
					return status;
				}else {
					logger.error("[{}]times has wrong:[{}] msg:[{}]",taskRetryTimes == 0 ? 1:(taskRetryTimes+1)," "+yqParamDTO.toString(),status+"-"+res_msg);
					if (res_msg.contains(NO_DATA_CODE)){
						break;
					}
					taskRetryTimes ++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		YiQingReportMonitor.addTaskProgress(key,1);
		return status;
	}

	private String fileExist() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < YiQingReportService.LAYER_NAMES.length; i++) {
			String path_ = path + i +".png";
			if (!FileOperation.fileExist(path_)) {
				if (i == YiQingReportService.LAYER_NAMES.length-1){
					str.append("all").append("-");
				}else {
					str.append(i).append("-");
				}
			}
		}
		if (str.length() >0) {
			return str.substring(0,str.length()-1);
		}else {
			return str.toString();
		}
	}


	private void calculatePhantomParam(String items){
		String str;
		String param_str = JSONObject.toJSONString(yqParamDTO);
		try {
			param_str = URLEncoder.encode(param_str,"UTF-8").replaceAll("\\+","%20").replaceAll("%3A",":").replaceAll("%2C",",");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String webUrl = YQ_PTM_URL + param_str;
		String cacheParam = "--config="+PHANTOMJS_CONFIG_FILE;
		String picPath = path.replaceAll(" ","%20");
		if (StringUtils.isBlank(items)){
			str = cacheParam+ " "+YQ_PTM_FILE + " " +webUrl + " " +"null"+" "+ picPath;
		}else {
			str =  cacheParam+ " "+YQ_PTM_FILE + " " +webUrl + " " +items+" "+ picPath;
		}
		phantom_params = str;
	}

	public String getInfo_params() {
		return info_params;
	}

	public void setRes_msg(String res_msg) {
		this.res_msg = res_msg;
	}

	public String getPhantom_params() {
		return phantom_params;
	}


	public int getTaskRetryTimes() {
		return taskRetryTimes;
	}
}
