package com.nokia.export.controller;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.export.util.CommonPath;
import com.nokia.export.util.DBQuery;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
public class ExportMIFController {
	@Autowired  
	private Environment env;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@RequestMapping(value = "exportMIF", method = { RequestMethod.POST, RequestMethod.GET })
	public void creatMifFile(@RequestParam("db") String db, @RequestParam("sql") String sql,
			@RequestParam("fileName") String fileName) throws Exception {
		DBQuery dbQuery = new DBQuery();
		String rootpath = CommonPath.getConfig("MIFPath") + "/";
		Connection conn = dbQuery.getCon(db, env);
		JSONArray cArr = cfgField(sql);
		CreateMifFile(conn, rootpath, sql, fileName, cArr);
	}

	public JSONArray cfgField(String dbSql) {
		String strSql = dbSql.substring(7, dbSql.indexOf("from"));
		String[] arrFld = strSql.split(",");
		JSONArray arr = new JSONArray();
		for (String field : arrFld) {
			String[] tmp = field.split("as");
			String fld = tmp[1].trim();
			JSONObject obj = new JSONObject();
			if (("LON".equals(fld)) || ("LAT".equals(fld)) || ("lon".equals(fld)) || ("lat".equals(fld))) {
				obj.put("label", fld.toUpperCase());
				obj.put("value", "Float");
			} else if (("VMAP".equals(fld)) || ("vmap".equals(fld))) {
				obj.put("label", fld.toUpperCase());
				obj.put("value", "Char(4000)");
			} else {
				obj.put("label", fld.toUpperCase());
				obj.put("value", "Char(100)");
			}
			arr.add(obj);
		}
		return arr;
	}

	public ResponseEntity<JSONObject> CreateMifFile(Connection conn, String rootpath, String sql, String fileN, JSONArray cArr) {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssms");
		String fileName = fileN + "_" + uuid + "_" + sdf.format(new Date());
		String mifFileN = fileName + ".mif";
		String midFileN = fileName + ".mid";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);
			ps.setFetchSize(10000);
			rs = ps.executeQuery();
			StringBuilder datasStr = new StringBuilder();
			StringBuilder columnsStr = new StringBuilder();

			StringBuilder rowdatasStr = new StringBuilder();
			int iColumn = rs.getMetaData().getColumnCount();
			ResultSetMetaData rsmd = rs.getMetaData();
			columnsStr.append("\r\n\r\n");
			columnsStr.append("Columns " + iColumn);
			columnsStr.append("\r\n");

			int fla = 0;
			int titleF = 0;
			String mifString = "Version   300\r\nCharset \"WindowsSimpChinese\"\r\nDelimiter \",\"\r\nIndex 2,3,4,6\r\nCoordSys Earth Projection 1, 0";
			while (rs.next()) {
				if (fla == 0) {
					for (int i = 1; i <= iColumn; i++) {
						JSONObject jsonO = JSONObject.fromObject(cArr.get(i - 1));
						if (rsmd.getColumnName(i).toString().equals(jsonO.get("label"))) {
							columnsStr.append(rsmd.getColumnName(i));
							columnsStr.append("  " + jsonO.get("value"));
							columnsStr.append("\r\n");
						}
					}
				}
				for (int i = 1; i <= iColumn; i++) {
					rowdatasStr.append(rs.getString(i));
					if (i != iColumn) {
						rowdatasStr.append(",");
					}
				}
				rowdatasStr.append("\r\n");
				datasStr.append("Region  1");
				datasStr.append("\r\n  ");
				String vmapStr = rs.getString("VMAP");
				String lonStr = rs.getString("LON");
				String latStr = rs.getString("LAT");
				String[] arr = vmapStr.split(";");

				datasStr.append("  " + arr.length);
				datasStr.append("\r\n  ");
				for (int n = 0; n < arr.length; n++) {
					datasStr.append(arr[n]);
					datasStr.append("\r\n  ");
				}
				datasStr.append("Pen (1,2,0)");
				datasStr.append("\r\n  ");
				datasStr.append("Brush (2,16777215,16777215)");
				datasStr.append("\r\n  ");
				datasStr.append("Center " + lonStr + " " + latStr);
				datasStr.append("\r\n");

				fla++;
				if (fla % 100000 == 0) {
					if (titleF == 0) {
						mifString = mifString + columnsStr.toString() + "\r\nData\r\n" + datasStr.toString();
						contentToTxt(rootpath + "\\\\" + mifFileN, mifString);
						titleF = 1;
					} else {
						contentToTxt(rootpath + "\\\\" + mifFileN, datasStr.toString());
					}
					datasStr = new StringBuilder();
					contentToTxt(rootpath + "\\\\" + midFileN, rowdatasStr.toString());
					rowdatasStr = new StringBuilder();
				}
			}
			if (titleF == 0) {
				mifString = mifString + columnsStr.toString() + "\r\nData\r\n" + datasStr.toString();
				contentToTxt(rootpath + "\\\\" + mifFileN, mifString);
				titleF = 1;
			} else {
				contentToTxt(rootpath + "\\\\" + mifFileN, datasStr.toString());
			}
			contentToTxt(rootpath + "\\\\" + midFileN, rowdatasStr.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		JSONObject res = new JSONObject();
		//res.put("status", status);
		res.put("filePath", rootpath + "\\\\" + fileName);
		return ResponseEntity.ok(res);
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
			return 1;
		} catch (Exception e) {
			return 2;
		}
	}
}
