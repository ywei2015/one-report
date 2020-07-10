package com.nokia.export.util;

import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;

public class FileOperation {
	private static Logger logger = LoggerFactory.getLogger("FileOperation");
	/**
	 * 创建文件
	 * @param file
	 * @return
	 */
	public static boolean createFile(File file) {
		boolean flag = false;
		try {
			if (!file.exists()) {
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				file.createNewFile();
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 读取文件内容
	 * 
	 * @param filePathAndName
	 *            String 如 c:\\1.txt 绝对路径
	 * @return boolean
	 */
	public static String readFile(String filePathAndName) {
		String result = "";
		StringBuffer sb = new StringBuffer();
		FileInputStream fis = null;
		InputStreamReader read = null;
		BufferedReader reader = null;
		try {
			File file = new File(filePathAndName);
			if (file.isFile() && file.exists()) {
				fis = new FileInputStream(file);
				read = new InputStreamReader(fis, "UTF-8");
				reader = new BufferedReader(read);
				String line = "";
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				result = sb.toString();
				sb.setLength(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (read != null) {
					read.close();
					read = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 读SQL文件内容
	 * 
	 * @param file
	 * @return
	 */
	public static String readSQLFile(File file) {
		String result = "";
		StringBuffer sb = new StringBuffer();
		FileInputStream fis = null;
		InputStreamReader read = null;
		BufferedReader reader = null;
		try {
			if (file != null && file.isFile() && file.exists()) {
				fis = new FileInputStream(file);
				read = new InputStreamReader(fis, "UTF-8");
				reader = new BufferedReader(read);
				String line;
				boolean ck = true;
				while ((line = reader.readLine()) != null) {
					//logger.info(line);
					if (line.contains("*/")) {
						ck = true;
					}
					if(ck) {
						if (line != null && !line.contains("--") && !line.contains("/*") && !line.contains("*/")) {// 没注释的
							sb.append(line + "\r\n");
						} else if (line != null && (line.contains("--") || line.contains("/*") || line.contains("*/"))) {// 有注释的
							if (line.length() > 0) {
								line = line.replaceAll("\\/\\*.+?\\*\\/", "");// 先去掉/*...*/
								if (line.contains("/*")) {
									line = line.substring(0, line.indexOf("/*"));// 再去掉/*...
									ck = false;
								}
								if (line.contains("*/")) {
									line = line.substring(line.indexOf("*/"), line.length() - 1);// 再去掉...*/
								}
							}
							if (line.split("--").length > 0 && line.split("--")[0].trim().length() > 1) {
								sb.append(line.split("--")[0] + "\r\n");;
							}
						}
					}
				}
				result = sb.toString();
				result = result.trim();
				result = result.replaceAll("\\[", "【");
				result = result.replaceAll("\\]", "】");
				sb.setLength(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (read != null) {
					read.close();
					read = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 读TXT文件内容
	 * 
	 * @param file
	 * @return
	 */
	public static String readTxtFile(File file) {
		String result = "";
		StringBuffer sb = new StringBuffer();
		InputStreamReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			if (file != null && file.isFile() && file.exists()) {
				// fileReader = new FileReader(file);
				fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
				bufferedReader = new BufferedReader(fileReader);
				String read = null;
				while ((read = bufferedReader.readLine()) != null) {
					sb.append(read);
				}
				result = sb.toString();
				sb.setLength(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
					bufferedReader = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fileReader != null) {
					fileReader.close();
					fileReader = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 写入文件
	 * 
	 * @param content
	 * @param file
	 * @throws Exception
	 */
	public static boolean writeTxtFile(String content, File file,boolean append) {
		boolean flag = false;
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;
		try {
			fos = new FileOutputStream(file,append);
			osw = new OutputStreamWriter(fos, "UTF-8");
			writer = new BufferedWriter(osw);
			writer.write(content);
			writer.flush();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (osw != null) {
					osw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * 重新写文件
	 * 
	 * @param filePath
	 * @param content
	 */
	public static void contentToTxt(String filePath, String content) {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;
		try {
			File file = new File(filePath);
			if (!(file.isFile() && file.exists())) {
				file.getParentFile().mkdirs();
				file.createNewFile();// 文件不存在则创建
			}
			fos = new FileOutputStream(file);
			osw = new OutputStreamWriter(fos, "UTF-8");
			writer = new BufferedWriter(osw);
			writer.write(content);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("file===:"+e.getMessage()+"   filePath:"+filePath);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (osw != null) {
					osw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 根据java.io.*的流获取文件大小
	 * @param path
	 */
	public static long getFileSize(String path){
		File file = new File(path);
		FileChannel fc = null;
		try {
			if(file.exists() && file.isFile()){
				FileInputStream fis = new FileInputStream(file);
				fc = fis.getChannel();
				return fc.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null!=fc){
				try {
					fc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	/**
	 *
	 * @Title: zipFilesAndEncrypt
	 * @Description: 将指定路径下的文件压缩至指定zip文件，并以指定密码加密 若密码为空，则不进行加密保护
	 * @param zipFileName zip文件名
	 * @param password 加密密码
	 * @return
	 */
	public static void zipFilesAndEncrypt(List<String> paths,String zipFileName,String password){
		ZipOutputStream outputStream = null;
		InputStream inputStream = null;
		try {
			FileOperation.createFile(new File(zipFileName));
			outputStream = new ZipOutputStream(new FileOutputStream(new File(zipFileName)));
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			if(!StringUtils.isEmpty(password)){
				parameters.setEncryptFiles(true);
				parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
				parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
				parameters.setPassword(password);
			}

			int fileNums = paths.size();
			for (int i = 0; i < fileNums; i++) {
				File file = new File(paths.get(i));

				outputStream.putNextEntry(file,parameters);

				if (file.isDirectory()) {
					outputStream.closeEntry();
					continue;
				}

				inputStream = new FileInputStream(file);
				byte[] readBuff = new byte[4096];
				int readLen = -1;
				while ((readLen = inputStream.read(readBuff)) != -1) {
					outputStream.write(readBuff, 0, readLen);
				}
				outputStream.closeEntry();
				inputStream.close();
			}
			outputStream.finish();
		} catch (Exception e) {
			logger.error("文件压缩出错", e);
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.error("压缩文件输出错误", e);
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("压缩文件错误", e);
					e.printStackTrace();
				}
			}
		}
	}

	/**复制文件的方法*/
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { //文件存在时
				InputStream inStream = new FileInputStream(oldPath); //读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; //字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		}
		catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		}
	}

	public static Boolean fileExist(String path){
		File file = new File(path);
		if (file.exists()){
			return true;
		}else {
			return false;
		}
	}
	public static void main(String[] args) {
		String path1= "C:\\document\\一键报告\\0-tes.png";
		String path2 = "C:\\document\\一键报告\\1-tes.png";
		String path3 = "C:\\document\\一键报告\\2-tes.png";
		String path4 = "C:\\document\\一键报告\\test.pptx";
		List<String> paths = new LinkedList<>();
		paths.add(path1);
		paths.add(path2);
		paths.add(path3);
		paths.add(path4);
		String zipPath = "C:\\document\\test\\test.zip";
		try {
			FileOperation.zipFilesAndEncrypt(paths,zipPath,"abc");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteFile(String path) {
		File file = new File(path);
		if (file.exists()){
			file.delete();
		}
	}

	//删除文件夹
	public static boolean delFolder(String folderPath,String urlPar) {
		//deleteAllFile(folderPath,urlPar);
		boolean res = false;
		File file = new File(folderPath);
		if (StringUtils.isNotBlank(urlPar)){
			if (file.getPath().contains(urlPar)) {
				 res = file.delete();
			}
		}else {
			 res = file.delete();
		}
		return res;

	}

	//删除指定文件夹下所有文件
	public static boolean deleteAllFile(String path,String urlPar) {
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
				if (StringUtils.isNotBlank(urlPar)){
					if (temp.getPath().contains(urlPar)) {
						temp.delete();
					}
				}else {
					temp.delete();
				}
			}
			if(temp.isDirectory()) {
				deleteAllFile(path + File.separatorChar + templist[i],urlPar);
				delFolder(path + File.separator + templist[i],urlPar);
				flag = true;
			}
		}
		return flag;
	}


	public static void save(InputStream inputStream, String path) {
		OutputStream os = null;
		try {
			// 2、保存到临时文件
			// 1K的数据缓冲
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			// 输出的文件流保存到本地文件

			File tempFile = new File(path);
			os = new FileOutputStream(tempFile);
			// 开始读取
			while ((len = inputStream.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 完毕，关闭所有链接
			try {
				os.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}