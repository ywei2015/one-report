package com.nokia.report.util;

import com.nokia.export.util.FileOperation;
import com.nokia.report.pojo.PPTRemarkDTO;
import com.nokia.report.service.PPTReportContentHandler;
import com.nokia.report.service.yiqing.YiQingReportService;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

import static com.nokia.report.service.yiqing.YiQingReportService.KEYS;

public class BasePowerPointFileUtil {
	private static Logger logger = LoggerFactory.getLogger(BasePowerPointFileUtil.class);
	/**
	 * <p>读取PowerPoint文件中的幻灯片对象
	 *
	 * @param slideShow SlideShow对象
	 * @return 读取出的工作薄列表
	 */
	public static List readSlideShow(SlideShow slideShow) {

		List slideList = null;
		if (slideShow != null) {

			slideList = new ArrayList();
			List slides = slideShow.getSlides();
			for (int i = 0; i < slides.size(); i++) {
				slideList.add(BasePowerPointFileUtil.readSlide((Slide) slides.get(i)));
			}
		}
		return slideList;
	}

	/**
	 * <p>读取指定的Slide中的数据
	 *
	 * @param slide Slide对象
	 * @return 读取出的Slide数据列表
	 */
	public static List readSlide(Slide slide) {

		List shapeList = null;
		if (slide != null) {

			shapeList = new ArrayList();
			List shapes = slide.getShapes();
			for (int i = 0; i < shapes.size(); i++) {

				shapeList.add(BasePowerPointFileUtil.readShape((Shape) shapes.get(i)));
			}
		}
		return shapeList;
	}

	public static String reaSlideNote(XSLFSlide slide){
		logger.info("slide number:[{}]",slide.getSlideNumber());
		String str = "";
		XSLFNotes xslfNotes= slide.getNotes();
		if (xslfNotes == null) {
			return str;
		}
		List<List<XSLFTextParagraph>> textParagraphs = xslfNotes.getTextParagraphs();
		if (textParagraphs.size() > 1){
			if (textParagraphs.get(1).size() >0){
				str = textParagraphs.get(1).get(0).getText();
			}
		}
		return str;
	}

	/**
	 * <p>读取指定的图形的数据
	 *
	 * @param shape Slide中的图形对象
	 * @return 读取出的图形数据
	 */
	public static Object readShape(Shape shape) {

		String returnValue = null;
		if (shape != null) {

			if (shape instanceof TextShape) {
				try {

					returnValue = ((TextShape) shape).getText();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}
		}
		return returnValue;
	}

	public static void renderShape(Shape shape, Map<String, Object> data, XMLSlideShow ppt, int i) throws IOException {
		if (shape instanceof TextShape) {
			BasePowerPointFileUtil.replace(shape, data, ppt,i,false,null);
		} else if (shape instanceof GroupShape) {
			Iterator groupShapes = ((GroupShape) shape).iterator();
			while (groupShapes.hasNext()) {
				Shape groupShape = (Shape) groupShapes.next();
				BasePowerPointFileUtil.renderShape(groupShape, data, ppt,i);
			}
		} else if (shape instanceof TableShape) {
			XSLFTable tableShape = ((XSLFTable) shape);
			int column = tableShape.getNumberOfColumns();
			int row = tableShape.getNumberOfRows();
			for (int r = 0; r < row; r++) {
				for (int c = 0; c < column; c++) {
					BasePowerPointFileUtil.replace(tableShape.getCell(r, c), data, ppt, i,true, tableShape);
				}
			}
		}
	}

	public static void replace(Shape shape, Map<String, Object> data,XMLSlideShow ppt,int i, Boolean isTableCell, XSLFTable tableShape) throws IOException {
		if (shape instanceof TextShape) {
			TextShape textShape = (XSLFTextShape) shape;
			String text = textShape.getText().replaceAll("\n","").trim();
			String key = RgexStrSubUtil.getSubUtilSimple(text, "\\$\\{(.+?)\\}");
			if (StringUtils.isNotBlank(key)) {
				Object value = data.get(key);
				if (value != null) {
					if(key.contains("text")) {
						text = text.replaceAll("\\$\\{" + key + "\\}", value.toString());
						List<XSLFTextParagraph> textParagraphs = textShape.getTextParagraphs();
						XSLFTextParagraph textParagraph = textParagraphs.get(0);
						XSLFTextRun textRun = textParagraph.getTextRuns().get(0);
						Double fontSize = textRun.getFontSize();
						String fontFamily = textRun.getFontFamily();
						textShape.setText("");
						TextRun textRun1 = textShape.appendText(text,false);
						textRun1.setFontSize(fontSize);
						textRun1.setFontFamily(fontFamily);
						if (isTableCell){
							TableCell tableCell = tableShape.getCell(0,0);
							List<XSLFTextParagraph> textParagraphs2 = tableCell.getTextParagraphs();
							XSLFTextParagraph textParagraph2 = textParagraphs2.get(0);
							XSLFTextRun textRun2 = textParagraph2.getTextRuns().get(0);
							textRun1.setFontColor(textRun2.getFontColor());
						}
					}
					if(key.contains("table") && isTableCell) {
						List<HashMap<String,Object>> table_data = (List<HashMap<String, Object>>) value;
						renderTable(tableShape,table_data);
					}
					if (key.contains("img")){
						String img_path = (String) value;
						if (FileOperation.fileExist(img_path)){
							renderImage(shape,ppt,i,img_path);
						}{
							textShape.setText("");
						}
					}
				}else {
					text = text.replaceAll("\\$\\{" + key + "\\}", "");
					textShape.setText(text);
					logger.warn("[{}]在data数据中无有效值",key);
				}
			}
		}
	}

	private static void renderImage(Shape shape, XMLSlideShow ppt, int i, String path){
		byte[] btImg;
		FileInputStream io = null;
		try {
			io = new FileInputStream(path);
			btImg = IOUtils.toByteArray(io);
			XSLFPictureData idx = ppt.addPicture(btImg, XSLFPictureData.PictureType.PNG);
			List<XSLFSlide> slides = ppt.getSlides();
			XSLFSlide slide = slides.get(i);
			XSLFPictureShape pic = slide.createPicture(idx);
			Rectangle2D anchor = shape.getAnchor();
			//anchor.setFrame();
			// 设置XSLFPictureShape的位置信息
			pic.setAnchor(anchor);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (io != null){
				try {
					io.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static void renderTable2(TextShape textShape, XSLFSlide slide, List<HashMap<String, Object>> hashMaps){
		/** 创建表格**/
		//XSLFSlide slide = ppt.getSlides().get(i);
		XSLFTable table = slide.createTable();
		/** 设置表格 x ,y ,width,height **/
		//Rectangle2D rectangle2D = new Rectangle2D.Double(5,5,width,500);
		Rectangle2D rectangle2D = textShape.getAnchor();
		double width = rectangle2D.getWidth();
		double height = rectangle2D.getHeight();
		table.setAnchor(rectangle2D);
		HashMap<String,Object> objectHashMap = hashMaps.get(0);
		Set<String> set = objectHashMap.keySet();
		Iterator<String> stringIterator = set.iterator();
		XSLFTableRow headRow = table.addRow();
		while (stringIterator.hasNext()){
			String key = stringIterator.next();
			XSLFTableCell tableCell = headRow.addCell();
			XSLFTextParagraph p = tableCell.addNewTextParagraph();
			XSLFTextRun tr = p.addNewTextRun();
			tr.setBold(true);
			tr.setFontSize(10d);
			p.setTextAlign(TextParagraph.TextAlign.CENTER);
			tr.setText(key);
			tableCell.setBorderColor(TableCell.BorderEdge.bottom,new Color(134, 134, 134));
			tableCell.setBorderColor(TableCell.BorderEdge.top,new Color(134, 134, 134));
			tableCell.setBorderColor(TableCell.BorderEdge.left,new Color(134, 134, 134));
			tableCell.setBorderColor(TableCell.BorderEdge.right,new Color(134, 134, 134));
		}
		for(int x = 0; x < hashMaps.size(); x++) {
			XSLFTableRow tableRow = table.addRow();
			HashMap<String,Object> hashMap = hashMaps.get(x);
			int col = 0;
			Iterator<String> stringIterator2 = set.iterator();
			while (stringIterator2.hasNext()){
				String key = stringIterator2.next();
				Object value = hashMap.get(key);
				tableRow.setHeight(20);
				XSLFTableCell tableCell = tableRow.addCell();
				XSLFTextParagraph p = tableCell.addNewTextParagraph();
				XSLFTextRun tr = p.addNewTextRun();
				tr.setBold(true);
				tr.setFontSize(10d);
				tr.setText(String.valueOf(value));
				tableCell.setBorderColor(TableCell.BorderEdge.bottom,new Color(134, 134, 134));
				tableCell.setBorderColor(TableCell.BorderEdge.top,new Color(134, 134, 134));
				tableCell.setBorderColor(TableCell.BorderEdge.left,new Color(134, 134, 134));
				tableCell.setBorderColor(TableCell.BorderEdge.right,new Color(134, 134, 134));
				table.setColumnWidth(col,width/set.size());
				col++;
			}
		}
	}

	private static void renderTable(XSLFTable table, List<HashMap<String, Object>> hashMaps){
		XSLFTableRow headRow = table.getRows().get(0);
		List<XSLFTableCell> tableCells = headRow.getCells();
		List<String> head = new LinkedList<>();

		XSLFTableCell tableCell_remark = table.getCell(0,0);
		List<XSLFTextParagraph> textParagraphs1 = tableCell_remark.getTextParagraphs();
		XSLFTextParagraph textParagraph1 = textParagraphs1.get(0);
		XSLFTextRun textRun1 = textParagraph1.getTextRuns().get(0);
		double fontSize = textRun1.getFontSize() -1;

		XSLFTableCell tableCell_one = table.getCell(0,0);
		List<XSLFTextParagraph> textParagraphs2 = tableCell_one.getTextParagraphs();
		XSLFTextParagraph textParagraph2 = textParagraphs2.get(0);
		XSLFTextRun textRun2 = textParagraph2.getTextRuns().get(0);
		PaintStyle paintStyle = textRun2.getFontColor();
		Color borderColor = tableCell_one.getBorderColor(XSLFTableCell.BorderEdge.left);
		Color color_fill = tableCell_one.getFillColor();

		table.removeRow(1);
		for (XSLFTableCell tableCell : tableCells) {
			String str = tableCell.getText().replaceAll("\n","");
			head.add(str.trim());
		}
		for(int x = 0; x < hashMaps.size(); x++) {
			// 创建表格行
			XSLFTableRow tableRow = table.addRow();
			HashMap<String,Object> hashMap = hashMaps.get(x);
			for (String key : head) {
				Object value = hashMap.get(key);
				// 创建表格单元格
				XSLFTableCell tableCell = tableRow.addCell();
				tableCell.setFillColor(color_fill);
				XSLFTextParagraph p = tableCell.addNewTextParagraph();
				XSLFTextRun tr = p.addNewTextRun();
				tr.setFontColor(paintStyle);
				tr.setFontSize(fontSize);
				if (value != null){
					tr.setText(String.valueOf(value));
				}
				tableCell.setBorderColor(TableCell.BorderEdge.bottom,borderColor);
				tableCell.setBorderColor(TableCell.BorderEdge.top,borderColor);
				tableCell.setBorderColor(TableCell.BorderEdge.left,borderColor);
				tableCell.setBorderColor(TableCell.BorderEdge.right,borderColor);
			}
		}
	}

	/**
	 * 根据cellId size和模板标签数据
	 * 去除模板多余的幻灯片
	 * @param ppt
	 * @param size
	 */
	public static void trimSlides(XMLSlideShow ppt, int size){
		List<PPTRemarkDTO> pptRemarkDTOS = findPPtRemarksDTO(ppt);
		if (!pptRemarkDTOS.isEmpty()){
			Set<String> res = analyzeRedundantSlides(pptRemarkDTOS,size);
			deleteRedundantSlides(ppt,res);
		}
	}

	private static void deleteRedundantSlides(XMLSlideShow ppt, Set<String> res) {
		List<XSLFSlide> xslfSlides = ppt.getSlides();
		while (!res.isEmpty()){
			Integer index = null;
			for (int i = xslfSlides.size() - 1; i >= 0; i--) {
				XSLFSlide xslfSlide = xslfSlides.get(i);
				String note = reaSlideNote(xslfSlide);
				if (res.contains(note)){
					res.remove(note);
					index = xslfSlide.getSlideNumber()-1;
					break;
				}
			}
			if (index != null){
				ppt.removeSlide(index);
			}
		}
	}


	private static Set<String> analyzeRedundantSlides(List<PPTRemarkDTO> pptRemarkDTOS, int size) {
		Set<String> res = new LinkedHashSet<>();
		Set<String> stringSet = new LinkedHashSet<>();
		pptRemarkDTOS.forEach(pptRemarkDTO -> {
			String mr = pptRemarkDTO.getMr();
			if (stringSet.contains(mr)) {
				return;
			}
			stringSet.add(mr);
			double step = (double)pptRemarkDTO.getStep();
			Double num_d = Math.ceil((double)size/step);
			int num = num_d.intValue();
			for (int i = 0; i <pptRemarkDTOS.size() ; i++) {
				PPTRemarkDTO pptRemarkDTO1 = pptRemarkDTOS.get(i);
				if (pptRemarkDTO1.getMr().equals(mr)){
					if (num > 0){
						num --;
					}else {
						res.add(pptRemarkDTO1.getStr());
						System.out.println(pptRemarkDTO1.getStr());
					}
				}
			}
		});
		return res;
	}

	private static List<PPTRemarkDTO> findPPtRemarksDTO(XMLSlideShow ppt) {
		List<PPTRemarkDTO> pptRemarkDTOS = new LinkedList<>();
		List<XSLFSlide> xslfSlides = ppt.getSlides();
		xslfSlides.forEach(xslfSlide -> {
			String note = reaSlideNote(xslfSlide);
			logger.info("note:[{}]",note);
			if (StringUtils.isNotBlank(note)){
				PPTRemarkDTO pptRemarkDTO = new PPTRemarkDTO(note);
				pptRemarkDTOS.add(pptRemarkDTO);
			}
		});
		return pptRemarkDTOS;
	}

	public static void replaceTextShape(TextShape textShape, String str){
		textShape.setText("");
		textShape.setText(str);
	}

	public static void replaceTextShapeAdd(TextShape textShape, String str){
		str = "${" + str +"}";
		textShape.setText("");
		textShape.setText(str);
	}

	/**
	 * 根据userSize计算图片算量 从确定ppt页数
	 * @param size
	 */
	public static void CopyAndPreparePPT(XMLSlideShow ppt , int size){
		Double userSize = Integer.valueOf(size).doubleValue();
		String[] layerNames = YiQingReportService.LAYER_NAMES;
		Double pageSize = YiQingReportService.PER_PPT_PICTURE_SIZE.doubleValue();
		int pptSize = layerNames.length/YiQingReportService.PER_PPT_PICTURE_SIZE;
		Double pageCount = 0D;
		for (String name : layerNames) {
			Double pages = Math.ceil(userSize/pageSize);
			pageCount = pageCount + pages ;
		}
		copyPPTPageFromFirstPage(ppt,pageCount.intValue()-pptSize);
	}
	public static void clearSlideFromIndex(XMLSlideShow ppt , int index){
		int size = ppt.getSlides().size();
		if (size-1 >= index ){
			int num = size - index;
			for (int i = 0; i < num; i++) {
				ppt.removeSlide(index);
			}
		}

	}

	/**
	 * 从第一张ppt复制number张ppt
	 * @param ppt
	 * @param number
	 */
	private static void copyPPTPageFromFirstPage(XMLSlideShow ppt , int number) {
		XSLFSlide xslfSlide = ppt.getSlides().get(0);
		for (int i = 0; i < number ; i++) {
			XSLFSlide xslfSlide_ = ppt.createSlide();
			clearSlide(xslfSlide_);
			copySlide(xslfSlide,xslfSlide_);
		}
	}

	private static void clearSlide(XSLFSlide xslfSlide_) {
		List<XSLFShape> xslfShapes = xslfSlide_.getShapes();
		int size = xslfShapes.size();
		for (int i = 0; i < size; i++) {
			XSLFShape xslfShape = xslfSlide_.getShapes().get(0);
			xslfSlide_.removeShape(xslfShape);
		}
	}

	private static XSLFSlide copySlide(XSLFSlide sourceSlide, XSLFSlide targetSlide) {
		XSLFSlide s1 = null;
		try {
			for (int i =0 ; i <KEYS.length ; i++) {
				XSLFShape xslfShape = sourceSlide.getShapes().get(i);
				XSLFAutoShape slideAutoShape = targetSlide.createAutoShape();
				slideAutoShape.setText(KEYS[i]);
				slideAutoShape.setAnchor(xslfShape.getAnchor());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s1;
	}

	public static void write(XMLSlideShow ppt, String path) {
		FileOutputStream out = null;
		try {
			File file = new File(path);
			FileOperation.createFile(file);
			out = new FileOutputStream(file);
			ppt.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (out != null){
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		String path = "C:\\document\\一键报告\\test\\test.pptx" ;
		String path1 = "C:\\document\\一键报告\\test\\target.pptx" ;
		try {
			XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(path));
			CopyAndPreparePPT(ppt,3);
			ppt.write(new FileOutputStream(path1));
			ppt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
