package com.nokia.export.service;

import com.nokia.export.util.CommonPath;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;

@Service
public class ExportExcelService {
	private static final String ExcelPath = CommonPath.getConfig("ExcelPath");

	public int generateSVC(ResultSet res, String fileName) {
		int status;
		FileOutputStream fos;
		CSVPrinter csvPrinter = null;
		try {
			File file = new File(ExcelPath);
			boolean mkdirs = file.exists();
			if (mkdirs == false) {
				file.mkdirs();
			}
			fos = new FileOutputStream(new File(ExcelPath + "/" + fileName));

			OutputStreamWriter osw = new OutputStreamWriter(fos, "GBK");
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(res);
			csvPrinter = new CSVPrinter(osw, csvFormat);
			csvPrinter.printRecords(res);
			csvPrinter.flush();
			csvPrinter.close();
			status = 1;
		} catch (Exception e){
			status = 3;
			e.printStackTrace();
		} finally {
			try {
				csvPrinter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return status;
	}
}
