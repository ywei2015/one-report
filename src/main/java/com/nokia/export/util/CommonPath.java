package com.nokia.export.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

public class CommonPath {
	private static final Logger logger = LoggerFactory.getLogger(CommonPath.class);


	public static final String COMMON_PATH = getProjectRootPath();

	public static String getConfig(String filePath) {
		String str = null;
		SAXReader reader = new SAXReader();
		// 创建读取对象
		Document doc = null;
		try {
			System.out.println(ClassUtils.getDefaultClassLoader().getResource("").getPath() + "commonPath.xml");
			doc = reader.read(ClassUtils.getDefaultClassLoader().getResource("").getPath() + "commonPath.xml");
			Element root = doc.getRootElement();
			Element rootPath = root.element(filePath);
			str = rootPath.getText();
		} catch (DocumentException e) {
			logger.error("filePath not exist",e);
			return "";
		}
		return str;
	}

	public static String getProjectRootPath() {
		String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
		if (path.contains("%20")){
			path = path.replace("%20"," ");
		}
		path = path.substring(1,path.length());
		return path;
	}


}
