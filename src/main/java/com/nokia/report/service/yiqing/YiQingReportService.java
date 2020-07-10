package com.nokia.report.service.yiqing;

import com.nokia.export.util.CommonPath;
import com.nokia.export.util.DBUtil;
import com.nokia.export.util.DateUtil;
import com.nokia.export.util.FileOperation;
import com.nokia.report.pojo.YQParamDTO;
import com.nokia.export.service.DynamicSqlService;
import com.nokia.report.service.PPTReportContentHandler;
import com.nokia.report.thread.YQPhantomPictureGatherThread;
import com.nokia.report.thread.YQTableContextGatherThread;
import com.nokia.report.util.BasePowerPointFileUtil;
import com.nokia.report.util.SqlTableReadUtil;
import com.nokia.report.util.excel.CustomXSSFWorkbook;
import com.nokia.report.util.excel.ExcelTools;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class YiQingReportService {

	public final static String[] LAYER_NAMES = {"Dissemination Risk Assessment","Close Contacts","Roads","Residential Areas","Buildings","ScatterPlot","HeatMap","all"};
	public final static Integer PER_PPT_PICTURE_SIZE = 1;
	public final static String PIC_PATH = CommonPath.COMMON_PATH + "phantomjs/temp/pic/";
	public final static String EXCEL_PATH = CommonPath.COMMON_PATH + "phantomjs/temp/excel/";
	private final static String PPT_PATH = CommonPath.COMMON_PATH + "phantomjs/temp/ppt/";
	public final static String ZIP_PATH = CommonPath.COMMON_PATH + "phantomjs/report/";
	private final static String PPT_TEMPLATE_PATH = CommonPath.COMMON_PATH + "phantomjs/template/test.pptx";
	public final static String ALL = "all";

	public final static String ZC_EPIDEMIC_USER_TYPE_LIST = "zc_epidemic_user_type_list";
	public final static String ZOOM = CommonPath.getConfig("ZOOM") == null ? "12.5" : CommonPath.getConfig("ZOOM");
	public final static String PASSWORD = CommonPath.getConfig("zipPassword");
	public static final String[] KEYS = {"${img-1}","${text-title}"};
	public final static int[] zoomArray = {3268,6535,13071,26141,52283,104566};
	public final static int[] zoomIndex = {15,14,13,12,11,10};

	@Autowired
	private Environment env;
	@Autowired
	private DynamicSqlService dynamicSqlService;
	@Autowired
	private ExecutorService executorService;

	private static final Logger logger = LoggerFactory.getLogger(YiQingReportService.class);


	@Async
	public void doReport(String city, String startTime, String endTime, String userInput, String userType,String userId) {
		String date_star = startTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String date_end = endTime.replaceAll("-","").replace(" ","").replaceAll(":","").substring(0,8);
		String finalStartTime = DateUtil.dayStart(startTime);
		String finalEndTime = DateUtil.dayEnd(endTime);
		String userFlag = userInput;
		if (userInput.equalsIgnoreCase(ALL)){
			userFlag = userType;
		}
		String picPath = PIC_PATH + city + "/" + userFlag+ "/" +date_star+date_end + "_" ;
		String tablePath = EXCEL_PATH + city + "/" + userFlag+ "/" +userFlag +"_";
		String pptPath = PPT_PATH + city + "/"+ userFlag+ "/" +userFlag +"_" +date_star+"_"+date_end ;
		String zipPath = ZIP_PATH + city + "/" + userId+ "/" + userFlag+"_FullReport_"+date_star + "_" + date_end ;
		String key = city+"-" + userFlag +"-"+date_star + "-" + date_end;
		//收集table data
		List<Future<String>> futures = new LinkedList<>();
		YQTableContextGatherThread yqTableContextGatherThread = new YQTableContextGatherThread(dynamicSqlService,city,finalStartTime,finalEndTime,userInput,userId,userType,tablePath,key);
		Future<String> future = executorService.submit(yqTableContextGatherThread);
		futures.add(future);

		List<String> userGroupIds = null;

		if (ALL.equalsIgnoreCase(userInput)) {
			userGroupIds = findUserIdsByType(userType);
		}
		// 检查截图是否都存在
		List<String> userIds = checkPicture(picPath,userInput,userType,userGroupIds);

		if (!userIds.isEmpty()) {
			//收集图片
			String zoom = calculateZoom();
			userIds.forEach(phone -> {
				YQParamDTO yqParamDTO = new YQParamDTO(city, finalStartTime, finalEndTime,userType,phone,userId,zoom);
				YQPhantomPictureGatherThread yqPhantomPictureGatherThread = new YQPhantomPictureGatherThread(yqParamDTO,picPath,key);
				Future<String> futureP = executorService.submit(yqPhantomPictureGatherThread);
				futures.add(futureP);
			});
		}
		YiQingReportMonitor.initTaskProgress(key,userIds.size()+ SqlTableReadUtil.hashMaps.size());
		futures.stream().forEach(stringFuture -> {
			try {
				stringFuture.get(5 * 60 * 1000, TimeUnit.SECONDS);
			}catch (TimeoutException e) {
				logger.error("future 超时",e);
				stringFuture.cancel(true);
			}catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		});
		YiQingReportMonitor.modifyTaskProgress(key,"正在压缩资源包",90);
		exportZip(picPath,tablePath,pptPath,zipPath,userInput,userGroupIds,userId,userType,date_star+"_"+date_end);
		YiQingReportMonitor.modifyTaskFinish(key);
	}

	private String exportZip(String picPath, String tablePath, String pptPath, String zipPath, String userInput, List<String> userGroupIds, String userId, String userType, String dayStr) {
		List<String> paths = new LinkedList<>();
		String zip_path = zipPath + ".zip";
		String ppt_Path = rendImageToPPt(picPath,pptPath,userInput,userGroupIds,userType);
		if (FileOperation.fileExist(ppt_Path)) {
			paths.add(ppt_Path);
		}else {
			logger.warn("[{}] not exist",ppt_Path);
		}
		List<HashMap> hashMaps = SqlTableReadUtil.hashMaps;
		for (int i = 0; i < hashMaps.size(); i++) {
			HashMap hashMap = hashMaps.get(i);
			String table_Path = tablePath + hashMap.get("fileName") + "_"+dayStr+".xlsx";
			if (FileOperation.fileExist(table_Path)) {
				paths.add(table_Path);
			}else {
				logger.warn("[{}] not exist",table_Path);
			}
		}
		String password = DBUtil.findPasswordByUserId(userId,env);

		FileOperation.zipFilesAndEncrypt(paths,zip_path,password);
		return zip_path;
	}

	private String rendImageToPPt(String picPath, String pptPath, String userInput, List<String> userGroupIds, String userType) {
		String savePath = pptPath + ".pptx";
		XMLSlideShow ppt = null;
		try {
			ppt = new XMLSlideShow(new FileInputStream(PPT_TEMPLATE_PATH));
			if (ALL.equalsIgnoreCase(userInput)){
				BasePowerPointFileUtil.CopyAndPreparePPT(ppt,userGroupIds.size());
			}
			int index = 0;
			for (int i = 0; i < LAYER_NAMES.length; i++) {
				List<String> paths = new LinkedList<>();
				if (!ALL.equalsIgnoreCase(userInput)){
					String path = picPath + i + ".png";
					paths.add(path);
					index = PPTReportContentHandler.rendImageToPPt(ppt,paths,new String[]{LAYER_NAMES[i]+"_"+userInput},index);
				}else {
					String[] phoneFlags = new String[userGroupIds.size()];
					for (int j =0; j<userGroupIds.size(); j ++) {
						String phone = userGroupIds.get(j);
						String path = picPath.replace(ALL,phone) + i + ".png";
						phoneFlags[j] = LAYER_NAMES[i]+"_" +phone;
						if (phone.equalsIgnoreCase(ALL)){
							path = path.replaceAll("all",userType);
							phoneFlags[j] = LAYER_NAMES[i]+"_" +userType;
						}
						paths.add(path);
					}
					index = PPTReportContentHandler.rendImageToPPt(ppt,paths,phoneFlags,index);
				}
			}
			BasePowerPointFileUtil.clearSlideFromIndex(ppt,index);
			BasePowerPointFileUtil.write(ppt,savePath);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (ppt != null){
				try {
					ppt.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return savePath;
	}

	/**
	 * find some userIds which picture has not save in advance
	 * @param userId
	 * @param userType 用户组与表cfg_epidemic_user_inter中userType一致
	 * @param userGroupIds
	 * @return
	 */
	public List<String> checkPicture(String picPath, String userId, String userType, List<String> userGroupIds) {
		List<String> strings = new LinkedList<>();
		if (!ALL.equalsIgnoreCase(userId)) {
			String path1 = picPath + "0.png";
			String path2 = picPath + (LAYER_NAMES.length-1) + ".png";
			if(!FileOperation.fileExist(path1) || !FileOperation.fileExist(path2) ){
				strings.add(userId);
			}
		}else {
			// 添加用户组
			for (String id : userGroupIds) {
				String path_;
				if (!ALL.equalsIgnoreCase(id)) {
					path_ = picPath.replace("all",id);
				}else {
					path_ = picPath.replace("all",userType);
					id = userType;
				}
				String path1 = path_ + "0.png";
				String path2 = path_ + (LAYER_NAMES.length-1) + ".png";
				if(!FileOperation.fileExist(path1) || !FileOperation.fileExist(path2) ){
					strings.add(id);
				}
			}
		}
		return strings;
	}



	private List<String> findUserIdsByType(String userType) {
		String keys = userType+";";
		String sql = dynamicSqlService.querySql(ZC_EPIDEMIC_USER_TYPE_LIST,"oracle","zhongchou","interval",keys);
		if (StringUtils.isBlank(sql)) {
			logger.error("sql :[{}] is blank",ZC_EPIDEMIC_USER_TYPE_LIST);
			throw new RuntimeException(ZC_EPIDEMIC_USER_TYPE_LIST + "is blank");
		}else {
			List<String> userIds = dynamicSqlService.getStrsBySql(sql);
			return userIds;
		}
	}

	public String calculateZoom(){
		if (true) {
			return ZOOM;
		}
		String zoom = ZOOM;
		try {
			List<HashMap<String, Object>> hashMaps = dynamicSqlService.queryLongitudeRange();
			if (hashMaps.size() > 0) {
				HashMap<String, Object> hashmap = hashMaps.get(0);
				String maxx = (String) hashmap.get("MAXX");
				String minx = (String) hashmap.get("MINX");
				Double max = (Double.parseDouble(maxx)*110-Double.parseDouble(minx)*110);
				int zoomInt = calculateZoomByRange(max.intValue());
				zoom = Integer.toString(zoomInt);
			}
		}catch (Exception e){
			logger.error("find zoom error",e);
			zoom = ZOOM;
		}
		return zoom;
	}

	private int calculateZoomByRange(int intValue) {
		int zoom = Integer.valueOf(ZOOM);
		if (intValue <= zoomArray[0]) {
			zoom = zoomIndex[0];
		}else if (intValue > zoomArray[zoomArray.length-1]) {
			zoom =  zoomIndex[zoomArray.length-1];
		}
		for (int i = 1; i < zoomArray.length; i++) {
			if ( intValue <= zoomArray[i] && zoomArray[i-1] < intValue) {
				zoom =  zoomIndex[i];
			}
		}
		return zoom;
	}

	public synchronized void reZipFile(List<String> paths, String zipPath, String userId) {

		String password = DBUtil.findPasswordByUserId(userId,env);
		FileOperation.zipFilesAndEncrypt(paths,zipPath,password);
	}

	public boolean fileExist(String zipPath, String path_,String dayStr, String userId) {
		if (FileOperation.fileExist(zipPath)){
			return true;
		}
		String pptPath = PPT_PATH + path_ +dayStr +  ".pptx";
		if (!FileOperation.fileExist(pptPath)){
			return false;
		}
		List<String> paths = new LinkedList<>();
		paths.add(pptPath);
		List<HashMap> hashMaps = SqlTableReadUtil.hashMaps;
		for (int i = 0; i < hashMaps.size(); i++) {
			HashMap hashMap = hashMaps.get(i);
			String excel_path = EXCEL_PATH + path_ + hashMap.get("fileName") +"_"+ dayStr+".xlsx";
			if (!FileOperation.fileExist(excel_path)){
				return false;
			}
			paths.add(excel_path);
		}
		reZipFile(paths,zipPath,userId);
		return true;
	}

	public void doReportPPT(String city, String startTime, String endTime, String userInput, String userType, String userId_, String excel_zip_path, String tablePath, String s) {
		List<String> paths = new LinkedList<>();
		List<HashMap> confList = SqlTableReadUtil.readSqlConf();
		for (int i = 0; i < confList.size(); i++) {
			HashMap hashMap = confList.get(i);
			String fileName = (String) hashMap.get("fileName");
			String sqlName = (String) hashMap.get("sqlName");
			String savePath = tablePath + fileName + "_"+s+".xlsx";
			paths.add(savePath);
			if (FileOperation.fileExist(savePath)) {
				continue;
			}
			List<HashMap<String, Object>> hashMaps = new LinkedList<>();
			try{
				String keys =";"+city+";"+startTime+";"+endTime+";"+userType+";;"+userInput+";"+userId_;
				String sql = dynamicSqlService.querySql(sqlName,"oracle","vdt","interval",keys);
				if (StringUtils.isBlank(sql)) {
					logger.error("sql:[{}] is blank",sqlName);
					continue;
				}
				hashMaps = dynamicSqlService.getListBySql(sql);
				logger.info("obtain data:[{}] count:[{}]",sqlName,hashMaps.size());

			}catch (Exception e){
				logger.error("sql error:",e);
			}
			CustomXSSFWorkbook customXSSFWorkbook = new CustomXSSFWorkbook();
			if (!hashMaps.isEmpty()) {
				ExcelTools.renderTable(customXSSFWorkbook,hashMaps,null,null);
			}else {
				customXSSFWorkbook.createSheet("Sheet0");
			}
			ExcelTools.writeWorkBook(customXSSFWorkbook,savePath);
		}
		zipExcelFiles(paths,userId_,excel_zip_path);
	}

	private synchronized void zipExcelFiles(List<String> paths, String userId_, String excel_zip_path) {
		String password = DBUtil.findPasswordByUserId(userId_,env);
		FileOperation.zipFilesAndEncrypt(paths,excel_zip_path,password);
	}

	public synchronized void clearExcelCache() {
		String excel_path = EXCEL_PATH;
		String excel_zip = ZIP_PATH;
		FileOperation.deleteAllFile(excel_path,null);
		FileOperation.deleteAllFile(excel_zip,"excel");
	}
}
