package com.nokia.export.task;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.nokia.export.service.OperateExcel;
import com.nokia.export.util.CommonPath;

/**
 * 定时清理文件缓存，每天0点清理一次
 * @author hamsun
 *
 */
@Component
public class MyApplicationRunner implements ApplicationRunner {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("开启定时垃圾文件清理任务!");
		Calendar curDate = Calendar.getInstance();
        Calendar nextDayDate = new GregorianCalendar(curDate.get(Calendar.YEAR), curDate.get(Calendar.MONTH), curDate.get(Calendar.DATE),1,0,0);
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        // 参数：1、任务体 2、首次执行的延时时间   3、任务执行间隔 4、间隔时间单位
        service.scheduleAtFixedRate(()->clearTempFile(), (nextDayDate.getTimeInMillis() - curDate.getTimeInMillis())/1000, 24*3600, TimeUnit.SECONDS);
	}

	private void clearTempFile(){
		OperateExcel.deleteAllFile(CommonPath.getConfig("ExcelPath"));
		OperateExcel.deleteAllFile(CommonPath.getConfig("KMLPath"));
	}
}

