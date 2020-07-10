package com.nokia.export.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nokia.export.util.CommonPath;


@Service
public class OperateExcel {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public static HSSFSheet creatSheet(String sheetName, String[] title, HSSFWorkbook wb) {
		// 声明一个单子并命名
		HSSFSheet sheet = wb.createSheet(sheetName);
		// 给单子名称一个长度
		sheet.setDefaultColumnWidth((short) 15);
		// 生成一个样式
		HSSFCellStyle style = wb.createCellStyle();
		// 创建第一行（也可以称为表头）
		HSSFRow firstRow = sheet.createRow(0);
		// 样式字体居中
		style.setAlignment(HorizontalAlignment.CENTER);
		// 创建表头
		for (int i = 0, len = title.length; i < len; i++) {
			HSSFCell cell = firstRow.createCell(i);
			cell.setCellValue(title[i]);
			cell.setCellStyle(style);
		}
		return sheet;
	}

	public int generateExcel(List<Object[]> results, String[] title, String fileName) {
		// 声明一个工作薄
		HSSFWorkbook wb = new HSSFWorkbook();
		int sheetNum = 0;
		HSSFSheet sheet = null;
		if(results.size()==0) {//只创建表头
			sheet = creatSheet("sheet"+sheetNum,title,wb);
//			return 4;//sql查询结果为空
		}
		// 添加数据
		for (int i = 0, rowLen = results.size(); i < rowLen; i++) {
			if(i%65535==0) {//记录条数达到65535，新建sheet
				sheetNum+=1;
				sheet = creatSheet("sheet"+sheetNum,title,wb);
			}
			HSSFRow row = sheet.createRow(i%65535 + 1);
			for (int j = 0, columnLen = results.get(i).length; j < columnLen; j++) {
				HSSFCell cell = row.createCell(j);
				cell.setCellValue(results.get(i)[j].toString());
			}
		}

		try {
			File file = new File(CommonPath.getConfig("ExcelPath"));
			boolean mkdirs = file.exists();
			if (mkdirs == false) {
				file.mkdirs();
			}
			FileOutputStream out = new FileOutputStream(
					CommonPath.getConfig("ExcelPath") + "/" + fileName);
			wb.write(out);
			out.close();
			return 1;//成功
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 2;//文件不存在
		} catch (IOException e) {
			e.printStackTrace();
			return 3;//io异常
		}
	}
	
	//删除文件夹
	public static boolean delFolder(String folderPath) {
		deleteAllFile(folderPath);
		File file = new File(folderPath); 
		boolean res = file.delete();
		return res;
		
	}
	
	//删除指定文件夹下所有文件
	public static boolean deleteAllFile(String path) {
		System.out.println("开始清理");
		boolean flag = false;
		File file = new File(path);
		if(!file.exists()) {
			return flag;
		}
		if(!file.isDirectory()) {
			return flag;
		}
		String[] templist = file.list();
		File temp = null;
		for(int i = 0;i < templist.length;i++) {
			if(path.endsWith(File.separator)) {
				temp = new File(path + templist[i]);
			}else {
				temp = new File(path + File.separator + templist[i]);
			}
			if(temp.isFile()) {
				temp.delete();
			}
			if(temp.isDirectory()) {
				deleteAllFile(path + File.separatorChar + templist[i]);
				delFolder(path + File.separator + templist[i]);
				flag = true;
			}
		}
		return flag;
	}
}
