package com.nokia.report.task;

import com.nokia.export.util.CommonPath;
import com.nokia.export.util.DateUtil;
import com.nokia.export.util.FileOperation;
import com.nokia.report.pojo.CityAndDay0DO;
import com.nokia.report.pojo.YQParamDTO;
import com.nokia.export.service.DynamicSqlService;
import com.nokia.report.service.yiqing.YiQingReportService;
import com.nokia.report.thread.YQPhantomPictureGatherThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.nokia.report.service.yiqing.YiQingReportService.*;

@Component
public class YiQingReportTask {
	@Autowired
	private DynamicSqlService dynamicSqlService;
	@Autowired
	private ExecutorService executorService;
	@Autowired
	private YiQingReportService yiQingReportService;
	/**
	 * 需要轮询的用户列表sql
	 */
	public final static String ZC_EPIDEMIC_MONITOR_USER_LIST = "zc_epidemic_monitor_user_list";

	/**
	 * 获取day0 city sql
	 */
	public final static String ZC_EPIDEMIC_MONITOR_SDATE_CITY = "zc_epidemic_monitor_sdate_city";

	public final static String MSISDN = "MSISDN";

	public final static String USER_TYPE  = "USER_TYPE";
	/**
	 * 虚操作ID
	 */
	public final static String USER_ID = "1";

	private final static String PHANTOMJS_AUTO_SWITCH= CommonPath.getConfig("phantomjsAutoSwitch");

	private final static String DATABASETYPE= CommonPath.getConfig("databaseType");

	@Scheduled(cron = "0 5 0 * * *")
	public void work() {
		if ("false".equalsIgnoreCase(PHANTOMJS_AUTO_SWITCH)){
			return;
		}
		List<HashMap<String,Object>> users = fetchUserList();
		CityAndDay0DO cityAndDay0DO = gainCityAndDay0DO();
		String city = cityAndDay0DO.getCity();

		Date day14 = DateUtil.addDay(new Date(),-14);
		String startTime = DateUtil.format(day14, DateUtil.DATE_TIME_PATTERN);
		startTime = DateUtil.dayStart(startTime);
		String endTime = DateUtil.format(DateUtil.dayEnd(new Date()),DateUtil.DATE_TIME_PATTERN);

		String date_star = startTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String date_end = endTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);

		if (cityAndDay0DO == null) {
			return;
		}
		String zoom = yiQingReportService.calculateZoom();

		String finalStartTime = startTime;
		String finalEndTime = endTime;
		users.stream().forEach(user -> {
			String userId = (String) user.get(MSISDN);
			String userType = (String) user.get(USER_TYPE);
			String picPath = PIC_PATH + city + "/" + userId+ "/" +date_star+date_end + "_" ;
			if (ALL.equalsIgnoreCase(userId)) {
				picPath = picPath.replace(ALL,userType);
			}
			boolean isExist = checkPicture(picPath);
			if (!isExist) {
				YQParamDTO yqParamDTO = new YQParamDTO(city, finalStartTime, finalEndTime,userType,userId,USER_ID,zoom);
				YQPhantomPictureGatherThread yqPhantomPictureGatherThread = new YQPhantomPictureGatherThread(yqParamDTO,picPath, null);
				executorService.submit(yqPhantomPictureGatherThread);
			}
		});
	}

	private Boolean checkPicture(String picPath) {
		String path1 = picPath + "0.png";
		String path2 = picPath + (LAYER_NAMES.length-1) + ".png";
		if(!FileOperation.fileExist(path1) || !FileOperation.fileExist(path2) ){
			return false;
		}
		return true;
	}

	private CityAndDay0DO gainCityAndDay0DO() {
		String sql = "select city,sdate from wk_imsi_detail_hour where ROWNUM = 1 order by sdate desc";
		if (DATABASETYPE.equalsIgnoreCase("gp")) {
			sql = "select city,sdate from wk_imsi_detail_hour order by sdate desc  limit 1 offset 0";
		}
		List<HashMap<String, Object>> hashMaps = dynamicSqlService.getListBySql(sql);
		if (!hashMaps.isEmpty()){
			HashMap<String, Object> objectHashMap = hashMaps.get(0);
			String city = (String) objectHashMap.get("CITY");
			Date date = (Date) objectHashMap.get("SDATE");
			String sdate = DateUtil.format(date,DateUtil.DATE_TIME_PATTERN);
			CityAndDay0DO cityAndDay0DO = new CityAndDay0DO();
			cityAndDay0DO.setCity(city);
			cityAndDay0DO.setDate(sdate);
			return cityAndDay0DO;
		}else {
			throw new RuntimeException("dayo0 data cant find");
		}
	}

	private List<HashMap<String, Object>> fetchUserList() {
		String sql = dynamicSqlService.querySql(ZC_EPIDEMIC_MONITOR_USER_LIST,"oracle","zhongchou","interval",null);
		List<HashMap<String, Object>> hashMaps = dynamicSqlService.getListBySql(sql);
		return hashMaps;
	}

}
