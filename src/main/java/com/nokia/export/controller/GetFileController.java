package com.nokia.export.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nokia.export.util.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetFileController {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@GetMapping(value = "getFile")
	public String getFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("filePath") String filePath,@RequestParam(value = "userId",required = false) String userId) throws IOException {
		logger.info("文件路径："+filePath);
		if (filePath.contains("phantomjs")) {
			filePath = StringTools.reTransformUrl(filePath);
		}
		File file = new File(filePath);
		if (file.exists()) {
			logger.info("正在下载"+filePath+"路径下的文件！");
			String fileName = file.getName();
			response.setContentType("application/force-download");
			fileName = new String(fileName.getBytes(), "ISO-8859-1");
			response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
			byte[] buffer = new byte[1024];
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			OutputStream os = null;
			try {
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				os = response.getOutputStream();
				int i = bis.read(buffer);
				while (i != -1) {
					os.write(buffer, 0, i);
					i = bis.read(buffer);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			logger.info("下载成功！");
			return "success";
		}else {
			logger.info("当前路径"+filePath+"文件不存在！");
			response.setContentType("text/html; charset=UTF-8"); // 转码
			PrintWriter out = response.getWriter();
			out.flush();
			out.println("<script>");
			out.println("alert('记录数为0,文件未生成!');");
			out.println("history.back();");
			out.println("</script>");
			return "fail";
		}
	}

}
