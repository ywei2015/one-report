package com.nokia.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.nokia.export.util.CommonPath;
import com.nokia.export.util.DBQuery;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.ARRAY;
import oracle.sql.Datum; 
import oracle.sql.STRUCT;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MIFTests {
	@Autowired  
	private Environment env;
	
	@Test
	public void creatMifFile() throws Exception {
		String db = "db2";
//		String sql = "select t.sdate,\r\n" + 
//				"       t.city city,\r\n" + 
//				"       t.road_id,\r\n" + 
//				"       t.road_Name,\r\n" + 
//				"       t.road_type,\r\n" + 
//				"       t.gridx,\r\n" + 
//				"       t.gridy,\r\n" + 
//				"       t.sum_samples,\r\n" + 
//				"       t.poor_samples,\r\n" + 
//				"       t.AVG_RSRP,\r\n" + 
//				"       t.POOR_COVERAGE_110_RATE,\r\n" + 
//				"       t.AVG_RSRQ,\r\n" + 
//				"       t.avg_sinr_ul,\r\n" + 
//				"       t.avg_sinr_dl,\r\n" + 
//				"       t.avg_ta,\r\n" + 
//				"       t.OVER_LAPPING_RATE,\r\n" + 
//				"       t.POOR_QUALITY_RATE,\r\n" + 
//				"       t.geom\r\n" + 
//				" from CWL_ROAD_GRID10_RESULT t\r\n";
//				" where ROWNUM <= 1000";
		String sql = "select \r\n" + 
				"'201801' as SDATE,\r\n" + 
				"t.city\r\n" + 
				",road_id ROAD_ID\r\n" + 
				",round(t.avg_rsrp,2) as MAXRSRP\r\n" + 
				",round(t.avg_rsrq,2) as AVG_RSRQ\r\n" + 
				",t.spoint \r\n" + 
				"from\r\n" + 
				"DT_RAWDATA_GRID10_GEO t\r\n" + 
				"where city='ZHENJIANG'";
		String fileName = "测试111";
		DBQuery dbQuery = new DBQuery();
		String rootpath = CommonPath.getConfig("MIFPath") + "/";
		ResultSet rs = dbQuery.getRes(db, sql, env);
		CreateMifFile(rs,rootpath,sql,fileName);
		dbQuery.close();
		/*int columnCount = rs.getMetaData().getColumnCount();
		
		while(rs.next()) {
			Object[] result = new Object[columnCount];
			for (int i = 1; i < columnCount + 1; i++) {
				if (rs.getObject(i) == null) {
					result[i - 1] = "";
				} else if(rs.getObject(i) instanceof STRUCT){
					result[i - 1] = (STRUCT)rs.getObject(i);
					STRUCT a = (STRUCT)rs.getObject(i);
					Object[] attributes = a.getAttributes();
					ARRAY o = (ARRAY) attributes[4];
					Datum[] oracleArray = o.getOracleArray();
					System.out.println(oracleArray.length);
					for(int k = 0;k < oracleArray.length;k++) {
						System.out.print(oracleArray[k].doubleValue()+" ");
					}
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				}else {
					result[i - 1] = rs.getObject(i);
				}
				System.out.print(result[i - 1]+" ");
			}
			System.out.println("");
		}*/
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
						if(attributes[4]!=null) {
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
