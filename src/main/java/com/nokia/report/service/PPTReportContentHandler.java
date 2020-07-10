package com.nokia.report.service;

import com.nokia.report.pojo.PPTRemarkDTO;
import com.nokia.report.service.yiqing.YiQingReportService;
import com.nokia.report.util.BasePowerPointFileUtil;
import com.nokia.report.util.RgexStrSubUtil;
import com.nokia.report.util.ProductConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.xslf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.nokia.report.util.BasePowerPointFileUtil.renderShape;

/**
 * 一键报告ppt生成处理工具类
 * @author 56980
 */
public class PPTReportContentHandler extends AbstractReportContentHandler {
	private static PPTReportContentHandler pptReportContentHandler = new PPTReportContentHandler();

	private PPTReportContentHandler(){
		super();
	}

	public static PPTReportContentHandler getPptReportContentHandler() {
		return pptReportContentHandler;
	}

	@Override
	public  void renderContent(Map<String,Object> data, POIXMLDocument slideShow1){
		XMLSlideShow slideShow = null;
		if (slideShow1 != null) {
			slideShow = (XMLSlideShow) slideShow1;
		}
		List<XSLFSlide> slides = slideShow.getSlides();
		List<String> cellIds = (List<String>) data.get(ProductConstant.SYSTEM_CELLIDS);
		BasePowerPointFileUtil.trimSlides(slideShow,cellIds.size());
		for (int i = 0; i < slides.size(); i++) {
			int index = i;
			XSLFSlide slide = slides.get(i);
			String note = BasePowerPointFileUtil.reaSlideNote(slide);
			if (StringUtils.isNotBlank(note)){
				prepareNoteSlide(note,slide,data);
				List<XSLFShape> shapes = slide.getShapes();
				List<XSLFShape> shapes_ = new LinkedList<>();
				copyList(shapes,shapes_);
				for (XSLFShape shape : shapes_) {
					try {
						renderShape(shape,data,slideShow,index);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 准备带有标签slide数据
	 * @param note
	 * @param slide
	 * @param data
	 */
	private void prepareNoteSlide(String note, XSLFSlide slide, Map<String, Object> data) {
		List<String> cellIds = (List<String>) data.get(ProductConstant.SYSTEM_CELLIDS);
		PPTRemarkDTO pptRemarkDTO = new PPTRemarkDTO(note);
		List<XSLFShape> xslfShapes = slide.getShapes();
		String img_str = ProductConstant.IMG + "-" + pptRemarkDTO.getMr();
		String table_str = ProductConstant.TABLE + "-" + pptRemarkDTO.getMr();

		xslfShapes.stream().forEach(xslfShape -> {
			if (xslfShape instanceof TextShape){
				TextShape textShape = (TextShape) xslfShape;
				String text = textShape.getText().replaceAll("\n","").trim();
				if (text.contains(img_str)){
					prepareSlideImgData(text,textShape,cellIds,pptRemarkDTO);
				}
			}else if (xslfShape instanceof XSLFTable){
				XSLFTable tableShape = ((XSLFTable) xslfShape);
				if (tableShape.getRows().size() > 1){
					XSLFTableCell cell = tableShape.getCell(1,0);
					String text = cell.getText().replaceAll("\n","").trim();
					String key = RgexStrSubUtil.getSubUtilSimple(text, "\\$\\{(.+?)\\}");
					if (key.contains(table_str)){
						List<HashMap<String,String>> value = (List<HashMap<String, String>>) data.get(table_str);
						if (value == null){
							return;
						}
						String replace_str = table_str + "-"+pptRemarkDTO.getIndex();
						prepareSlideTableData(cellIds,pptRemarkDTO,value,data,replace_str);
						BasePowerPointFileUtil.replaceTextShapeAdd(cell,replace_str);
					}
				}
			}
		});
	}

	private void prepareSlideImgData(String text, TextShape textShape, List<String> cellIds, PPTRemarkDTO pptRemarkDTO) {
		String img_index_str = text.substring(text.lastIndexOf("-")+1,text.length()-1);
		int img_index = Integer.valueOf(img_index_str);
		List<String> strings = getSlideCellId(pptRemarkDTO,cellIds);
		if (strings.size() >= img_index){
			String text_replace = ProductConstant.IMG + "-" + pptRemarkDTO.getMr() + "-"+strings.get(img_index-1);
			BasePowerPointFileUtil.replaceTextShapeAdd(textShape,text_replace);
		}
	}

	private void prepareSlideTableData(List<String> cellIds, PPTRemarkDTO pptRemarkDTO, List<HashMap<String, String>> value, Map<String, Object> data, String replace_str) {
		List<HashMap<String, String>> res = new LinkedList<>();
		List<String> strings = getSlideCellId(pptRemarkDTO,cellIds);
		for (int i = value.size() - 1; i >= 0; i--) {
			HashMap<String, String> stringStringHashMap = value.get(i);
			String cellId = stringStringHashMap.get(ProductConstant.CELL);
			if (strings.contains(cellId.trim())){
				res.add(stringStringHashMap);
			}
		}
		data.put(replace_str,res);
	}

	private List<String> getSlideCellId(PPTRemarkDTO pptRemarkDTO,List<String> cellIds){
		int step = pptRemarkDTO.getStep();
		int index = pptRemarkDTO.getIndex();
		int start = (index-1)*step;
		int end = start+step-1;
		List<String> strings;
		if (cellIds.size()-1 >= end){
			strings = cellIds.subList(start,index);
		}else {
			strings = cellIds.subList(start,cellIds.size()-1);
		}
		return strings;
	}

	public static void copyList(List<XSLFShape> shapes, List<XSLFShape> shapes_) {
		shapes.forEach(shape -> {
			shapes_.add(shape);
		});
	}

	/**
	 * @param ppt
	 * @param paths
	 * @param index 开始页
	 * @return next index
	 */
	public static int rendImageToPPt(XMLSlideShow ppt, List<String> paths, String[] phoneFlags,int index) {
		Double picSize = Integer.valueOf(paths.size()).doubleValue();
		int pageSize = YiQingReportService.PER_PPT_PICTURE_SIZE;
		Double incre = Math.ceil(picSize/pageSize);
		for (int i = 0; i < incre; i++) {
			XSLFSlide xslfSlide = ppt.getSlides().get(index+i);
			int firstIndex = i*YiQingReportService.PER_PPT_PICTURE_SIZE;
			//int secondIndex = i*YiQingReportService.PER_PPT_PICTURE_SIZE+1;
			/*if (firstIndex < paths.size()){
				if (secondIndex < paths.size()){
					rendImageOne(ppt,xslfSlide,paths.get(firstIndex),paths.get(secondIndex),name);
				}else {
					rendImageOne(ppt,xslfSlide,paths.get(firstIndex),null,name);
					clearXSLFSlideShapes(xslfSlide);
				}
			}*/
			rendImageOne(ppt,xslfSlide,paths.get(firstIndex),null,phoneFlags[firstIndex]);

		}
		return incre.intValue() + index;
	}

	private static void clearXSLFSlideShapes(XSLFSlide xslfSlide) {
		List<XSLFShape> shapes = xslfSlide.getShapes();
		for (XSLFShape shape : shapes) {
			try {
				if (shape instanceof TextShape){
					String text = ((TextShape) shape).getText();
					if (text.contains("img-2")) {
						BasePowerPointFileUtil.replaceTextShape((TextShape) shape,"");
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private static void rendImageOne(XMLSlideShow ppt, XSLFSlide xslfSlide, String path1, String path2, String name) {
		List<XSLFShape> shapes = xslfSlide.getShapes();
		int size = shapes.size();
		Map<String, Object> data = new LinkedHashMap<>(YiQingReportService.PER_PPT_PICTURE_SIZE);
		data.put("text-title",name);
		data.put("img-1",path1);
		if (path2 != null) {
			data.put("img-2",path2);
		}
		for (int i = 0; i < size; i++) {
			try {
				XSLFShape shape = shapes.get(i);
				/*String text = ((TextShape) shape).getText();
				if (path2 == null) {
					if (text.contains("img-2")){
						continue;
					}
				}*/
				renderShape(shape,data,ppt,xslfSlide.getSlideNumber()-1);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
	}
}
