package com.nokia.report.util.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

public class ExcelTools {

    private static Logger logger = LoggerFactory.getLogger(ExcelTools.class);

    /**
     * 将图片插入到excel-2007 xlsx指定位置
     * @param excel_path
     * @param img_path
     */
    public static void writeImg2007(String excel_path , String img_path, int[] location){
        FileOutputStream fileOut = null;
        BufferedImage bufferImg;
        try {
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            bufferImg = ImageIO.read(new File(img_path));
            ImageIO.write(bufferImg, "jpg", byteArrayOut);

            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(excel_path));
            XSSFSheet sheet1 = wb.getSheetAt(0);
            //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
            XSSFDrawing patriarch = sheet1.createDrawingPatriarch();
            //anchor主要用于设置图片的属性
            //HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255,(short) 1, 1, (short) 5, 8);
            XSSFClientAnchor anchor = new XSSFClientAnchor(location[0], location[1], location[2], location[3],(short) location[4], location[5], (short) location[6], location[7]);
            anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
            //插入图片
            patriarch.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
            fileOut = new FileOutputStream(excel_path);
            // 写入excel文件
            wb.write(fileOut);
            wb.close();
            logger.info("----Excle文件已生成------");
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(fileOut != null){
                try {
                    fileOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeImg(String img_path, XSSFWorkbook wb,int[] location){
        BufferedImage bufferImg;
        ByteArrayOutputStream byteArrayOut = null;
        try {
            byteArrayOut = new ByteArrayOutputStream();
            bufferImg = ImageIO.read(new File(img_path));
            ImageIO.write(bufferImg, "jpg", byteArrayOut);

            XSSFSheet sheet1 = wb.getSheetAt(0);
            //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
            XSSFDrawing patriarch = sheet1.createDrawingPatriarch();
            //anchor主要用于设置图片的属性
            //HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255,(short) 1, 1, (short) 5, 8);
            XSSFClientAnchor anchor = new XSSFClientAnchor(location[0], location[1], location[2], location[3],(short) location[4], location[5], (short) location[6], location[7]);
            anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
            //插入图片
            patriarch.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG));
            logger.info("----picture insert------");
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                byteArrayOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeWorkBook(XSSFWorkbook wb,String path){
        FileOutputStream fileOut = null;
        // 写入excel文件
        try {
            File file = new File(path);
            File parenFile = file.getParentFile();
            if (!parenFile.exists()){
                parenFile.mkdirs();
                file.createNewFile();
            }
            fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                wb.close();
                if (fileOut != null){
                    fileOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void renderTable(CustomXSSFWorkbook wb, List<HashMap<String, Object>> hashMaps,Integer x, Integer y){
        try {
            XSSFSheet xssfSheet = wb.createSheet();
            xssfSheet.setDefaultColumnWidth(25);
            if (hashMaps == null || hashMaps.size() == 0){
                logger.warn("[{}] table data is null",hashMaps);
                return ;
            }
            if (x == null) {
                x = 0;
            }
            if (y == null) {
                y = 0;
            }
            //设置表头
            HashMap<String,Object> objectHashMap = hashMaps.get(0);
            List<String> headList = new LinkedList<String>();
            Set<String> set = objectHashMap.keySet();
            Iterator<String> stringIterator = set.iterator();
            int head_index = 0;
            XSSFRow xssfRowHead = xssfSheet.createRow(y);
            while (stringIterator.hasNext()){
                String hk = stringIterator.next();
                headList.add(hk);
                XSSFCell site_cell = xssfRowHead.createCell(x+head_index);
                site_cell.setCellValue(hk);
                site_cell.setCellStyle(wb.getFont_border_bold_style());
                head_index++;
            }
            for (int i = 0; i < hashMaps.size(); i++) {
                HashMap<String,Object> map = hashMaps.get(i);
                XSSFRow xssfRow = xssfSheet.createRow(y+1+i);
                for (int j = 0; j < headList.size(); j++) {
                    String key_ = headList.get(j);
                    String value = getValueByType(map.get(key_));
                    XSSFCell xssfCell = xssfRow.createCell(x+j);
                    xssfCell.setCellValue(value);
                    //logger.info("row [{}] col :[{}] value:[{}] ",y+1+i,x+j,value);
                    //设置表格边框
                    XSSFCellStyle xssfCellStyle = wb.getBorder_bold_style();
                    xssfCellStyle.setWrapText(true);
                    xssfCell.setCellStyle(xssfCellStyle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }

    }

    private static String getValueByType(Object key) {
        if (key !=null){
            try {
                if (key instanceof BigDecimal){
                    //BigDecimal o = (BigDecimal) key;
                    //DecimalFormat df1 = new DecimalFormat("0.00");
                    //String str = df1.format(o);
                    String str = key.toString();
                    return str;
                }else {
                    String str = key.toString();
                    if (str.length() > 50){
                        str = str.substring(0,50);
                    }
                    return str;
                }
            }catch (ClassCastException e){
                logger.error(e.toString());
                return "";
            }
        }
        return "";
    }

}
