package com.nokia.export.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.export.util.CommonPath;
import com.nokia.export.util.DBQuery;

import net.sf.json.JSONObject;
import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

@RestController
public class MIFExportController {
	@Autowired  
	private Environment env;
	
	@RequestMapping(value = "MIFexport", method = { RequestMethod.POST, RequestMethod.GET })
	public void creatMifFile(@RequestParam("sql") String tsql, @RequestParam("fileName") String fileName, @RequestParam("db") String db) throws Exception {
		System.out.println("getPostRequest; sql="+tsql);
		String sql = "select to_char(t.sdate, 'YYYYMMDD') sdate,\r\n" +
				"       --t.ecity,\r\n" + 
				"       t.city,\r\n" + 
				"       t.area,\r\n" + 
				"       t.region3,\r\n" + 
				"       t.btsname_cn,\r\n" + 
				"       t.cellname_cn,\r\n" + 
				"       t.vendorname,\r\n" + 
				"       t.sitetype,\r\n" + 
				"       t.scenario,\r\n" + 
				"       t.longitude,\r\n" + 
				"       t.latitude,\r\n" + 
				"       t.height,\r\n" + 
				"       t.downtilt,\r\n" + 
				"       t.azimuth,\r\n" + 
				"       t.enbid,\r\n" + 
				"       t.eci,\r\n" + 
				"       t.band,\r\n" + 
				"       t.earfcn,\r\n" + 
				"       t.spolygon\r\n" + 
				"  from CFG_SITEINFO_TDLTE t\r\n" + 
				" where t.city = 'ZHENJIANG'";
		DBQuery dbQuery = new DBQuery();
		String rootpath = CommonPath.getConfig("MIFPath") + "/";
		ResultSet rs = dbQuery.getRes(db, sql, env);
		CreateMifFile(rs,rootpath,sql,fileName);
		dbQuery.close();
	}
	
	public static int contentToTxt(String filePath, String content) {
		try {
			File f = new File(filePath);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			if (!f.exists()) {
				f.createNewFile();
			}
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
			BufferedWriter writer = new BufferedWriter(write);
			writer.write(content);
			writer.close();
			return 1;//成功
		} catch (Exception e) {
			System.out.println("MIF文件生成失败！");
			return 2;//失败
		}
	}
	
	public ResponseEntity<JSONObject> CreateMifFile(ResultSet rs, String rootpath, String sql, String fileN) {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssms");
		String fileName = fileN + "_" + uuid + "_" + sdf.format(new Date());
		String mifFileN = fileName + ".mif";
		String midFileN = fileName + ".mid";
		try {
			String mifHeadStr = "Version   300\r\nCharset \"WindowsSimpChinese\"\r\nDelimiter \",\"\r\nIndex 2,3,4,6\r\nCoordSys Earth Projection 1, 0";
			StringBuilder columnsStr = new StringBuilder();
			StringBuilder datasStr = new StringBuilder();

			StringBuilder rowdatasStr = new StringBuilder();
			int columnCount = rs.getMetaData().getColumnCount();
			ResultSetMetaData metaData = rs.getMetaData();
			
			columnsStr.append("\r\n\r\n");
			columnsStr.append("Columns " + columnCount);
			columnsStr.append("\r\n");
			
			datasStr.append("Data\r\n");
			for (int i = 1; i <= columnCount; i++) {
				columnsStr.append(metaData.getColumnName(i));
				columnsStr.append("  " + "Char(200)");
				//columnsStr.append("  " + metaData.getColumnTypeName(i));
				columnsStr.append("\r\n");
			}
			
			int fla = 0;//定义溢写标识
			while (rs.next()) {
				fla++;
				datasStr.append("Region  1\r\n");
				for (int i = 1; i <= columnCount; i++) {
					rowdatasStr.append(rs.getObject(i));//拼接数据到mid
					if (i < columnCount) {
						rowdatasStr.append(",");
					}
					
					if(rs.getObject(i) instanceof STRUCT){//只有是geo对象才获取data拼接到mif
						STRUCT struct = (STRUCT)rs.getObject(i);
						Object[] attributes = struct.getAttributes();
						ARRAY o = (ARRAY) attributes[4];
						Datum[] oracleArray = o.getOracleArray();
						int len = oracleArray.length;
						datasStr.append("  "+len/2+"\r\n");
						for(int k = 0;k < len;k++) {
							datasStr.append(oracleArray[k].doubleValue()+" ");
							if(k%2==1) {
								datasStr.append("\r\n");//一行坐标对拼完换行
							}
						}
					}
				}
				rowdatasStr.append("\r\n");
				
				datasStr.append("  Pen (1,2,0)");
				datasStr.append("\r\n");
				datasStr.append("  Brush (2,16777215,16777215)");
				datasStr.append("\r\n");
				
				if(fla == 100000) {//10万条数据溢写一次
					contentToTxt(rootpath+"/"+mifFileN, mifHeadStr+columnsStr.toString()+datasStr.toString());
					contentToTxt(rootpath+"/"+midFileN, rowdatasStr.toString());
					
					fla = 0;//以下，清空缓存
					rowdatasStr.delete(0, datasStr.length());
					datasStr.delete(0, datasStr.length());
				}
			}
			contentToTxt(rootpath+"/"+mifFileN, mifHeadStr+columnsStr.toString()+datasStr.toString());
			contentToTxt(rootpath+"/"+midFileN, rowdatasStr.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		JSONObject res = new JSONObject();
		//res.put("status", status);
		res.put("filePath", rootpath + "/" + fileName);
		return ResponseEntity.ok(res);
	}
}
