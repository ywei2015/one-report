package com.nokia.report.util;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.*;

public class BaseWordFileUtil {

	public static String readwriteWord(String filePath){
		File isExist = new File(filePath);
		/**判断源文件是否存在*/
		if(!isExist.exists()){
			return "源文件不存在！";
		}
		XWPFDocument document;
		try {
			/**打开word2007的文件*/
			OPCPackage opc = POIXMLDocument.openPackage(filePath);
			document = new XWPFDocument(opc);
			/**替换word2007的纯文本内容*/
			List<XWPFRun> listRun;
			List<XWPFParagraph> listParagraphs = document.getParagraphs();
			for (int i = 0; i < listParagraphs.size(); i++) {
				listRun = listParagraphs.get(i).getRuns();
				for (int j = 0; j < listRun.size(); j++) {
					if("#{text}#".equals(listRun.get(j).getText(0))){
						listRun.get(j).setText("替换的纯文本内容！",0);
					}
				}
			}
			/**取得文本的所有表格*/
			Iterator<XWPFTable> it = document.getTablesIterator();
			while(it.hasNext()){/**循环操作表格*/
				XWPFTable table = it.next();
				List<XWPFTableRow> rows = table.getRows();
				for(XWPFTableRow row:rows){/**取得表格的行*/
					List<XWPFTableCell> cells = row.getTableCells();
					for(XWPFTableCell cell:cells){/**取得单元格*/
						if("#{img}#".equals(cell.getText())){
							/**判断单元格的内容是否为需要替换的图片内容*/
							File pic = new File("E:\\test\\xiaosimm.png");
							FileInputStream is = new FileInputStream(pic);
							cell.removeParagraph(0);
							XWPFParagraph pargraph = cell.addParagraph();
							String bid = document.addPictureData(is, XWPFDocument.PICTURE_TYPE_PNG);
							int id =  document.getNextPicNameNumber(XWPFDocument.PICTURE_TYPE_PNG);
							//createPicture(bid,id,600, 395, pargrap);
							if(is != null){
								is.close();
							}
						}
						List<XWPFParagraph> pars = cell.getParagraphs();
						for(XWPFParagraph par:pars){
							List<XWPFRun> runs = par.getRuns();
							for(XWPFRun run:runs){
								run.removeBreak();
							}
						}
						if("#{table}#".equals(cell.getText())){/**判断单元格中是否为需要替换的文本内容*/
							cell.removeParagraph(0);
							cell.setText("替换表格中的文本内容！");
						}
					}
				}
			}
			String downloadPath = "D:\\replace.docx";
			OutputStream os = new FileOutputStream(downloadPath);
			document.write(os);
			if(os != null){
				os.close();
			}
			if(opc != null){
				opc.close();
			}
			return "文件转换成功！路径为："+downloadPath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filePath;
	}

	public static void createPicture(String path, int width, int height,XWPFParagraph paragraph,XWPFDocument document) throws IOException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(path);
			String bid = document.addPictureData(is, XWPFDocument.PICTURE_TYPE_PNG);
			int id =  document.getNextPicNameNumber(XWPFDocument.PICTURE_TYPE_PNG);
			final int EMU = 9525;
			width *= EMU;
			height *= EMU;
			CTInline inline = paragraph.createRun().getCTR().addNewDrawing().addNewInline();
			String picXml = "" +
					"<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
					"   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
					"      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
					"         <pic:nvPicPr>" +
					"            <pic:cNvPr id=\"" + id + "\" name=\"Generated\"/>" +
					"            <pic:cNvPicPr/>" +
					"         </pic:nvPicPr>" +
					"         <pic:blipFill>" +
					"            <a:blip r:embed=\"" + bid + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>" +
					"            <a:stretch>" +
					"               <a:fillRect/>" +
					"            </a:stretch>" +
					"         </pic:blipFill>" +
					"         <pic:spPr>" +
					"            <a:xfrm>" +
					"               <a:off x=\"0\" y=\"0\"/>" +
					"               <a:ext cx=\"" + width + "\" cy=\"" + height + "\"/>" +
					"            </a:xfrm>" +
					"            <a:prstGeom prst=\"rect\">" +
					"               <a:avLst/>" +
					"            </a:prstGeom>" +
					"         </pic:spPr>" +
					"      </pic:pic>" +
					"   </a:graphicData>" +
					"</a:graphic>";

			// CTGraphicalObjectData graphicData =
			inline.addNewGraphic().addNewGraphicData();
			XmlToken xmlToken = null;
			try {
				xmlToken = XmlToken.Factory.parse(picXml);
			} catch (XmlException xe) {
				xe.printStackTrace();
			}
			inline.set(xmlToken);
			inline.setDistT(0);
			inline.setDistB(0);
			inline.setDistL(0);
			inline.setDistR(0);

			CTPositiveSize2D extent = inline.addNewExtent();
			extent.setCx(width);
			extent.setCy(height);

			CTNonVisualDrawingProps docPr = inline.addNewDocPr();
			docPr.setId(id);
			docPr.setName("Picture" + id);
			docPr.setDescr("Generated");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}finally {
			is.close();
		}

	}

	/**
	 * 替换非表格埋点值
	 * @param xwpfDocument
	 * @param textMap  需要替换的文本入参
	 */
	public static void replaceText(XWPFDocument xwpfDocument,Map<String,String> textMap){
		List<XWPFParagraph> paras=xwpfDocument.getParagraphs();
		Set<String> keySet=textMap.keySet();
		for (XWPFParagraph para : paras) {
			//当前段落的属性
			System.out.println("打印获取到的段落的每一行数据++++++++>>>>>>>"+para.getText());
			String str=para.getText();
			System.out.println("========================>>>>>>"+para.getParagraphText());

			List<XWPFRun> list=para.getRuns();
			for(XWPFRun run:list){
				for(String key:keySet){
					if(key.equals(run.text())){
						run.setText(textMap.get(key),0);
					}
				}
			}
		}
	}


	/**
	 * 循环填充表格内容
	 * @param xwpfDocument
	 * @param params
	 * @param tableIndex
	 * @throws Exception
	 */
	private static  void insertValueToTable(XWPFDocument xwpfDocument, List<Map<String,String>> params, int tableIndex) throws Exception {
		List<XWPFTable> tableList = xwpfDocument.getTables();
		if (tableList.size() <= tableIndex) {
			throw new Exception("tableIndex对应的表格不存在");
		}
		XWPFTable table = tableList.get(tableIndex);
		List<XWPFTableRow> rows = table.getRows();
		if (rows.size() < 2) {
			throw new Exception("tableIndex对应表格应该为2行");
		}
		//模板的那一行
		XWPFTableRow tmpRow = rows.get(1);
		List<XWPFTableCell> tmpCells = null;
		List<XWPFTableCell> cells = null;
		XWPFTableCell tmpCell = null;
		tmpCells = tmpRow.getTableCells();


		String cellText = null;
		String cellTextKey = null;
		Map<String, Object> totalMap = null;
		for (int i = 0, len = params.size(); i < len; i++) {
			Map<String, String> map = params.get(i);
			// 创建新的一行
			XWPFTableRow row = table.createRow();
			// 获取模板的行高 设置为新一行的行高
			row.setHeight(tmpRow.getHeight());
			cells = row.getTableCells();
			for (int k = 0, klen = cells.size(); k < klen; k++) {
				tmpCell = tmpCells.get(k);
				XWPFTableCell cell = cells.get(k);
				cellText = tmpCell.getText();
				if (StringUtils.isNotBlank(cellText)) {
					//转换为mapkey对应的字段
					cellTextKey = cellText.replace("$", "").replace("{", "").replace("}", "");
					if (map.containsKey(cellTextKey)) {
						// 填充内容 并且复制模板行的属性
						setCellText(tmpCell, cell, map.get(cellTextKey));
					}
				}
			}

		}
		// 删除模版行
		table.removeRow(1);
	}

	/**
	 *  复制模板行的属性
	 * @param tmpCell
	 * @param cell
	 * @param text
	 * @throws Exception
	 */
	private static void setCellText(XWPFTableCell tmpCell, XWPFTableCell cell,String text) throws Exception {

		CTTc cttc2 = tmpCell.getCTTc();
		CTTcPr ctPr2 = cttc2.getTcPr();
		CTTc cttc = cell.getCTTc();
		CTTcPr ctPr = cttc.addNewTcPr();
		if (ctPr2.getTcW() != null) {
			ctPr.addNewTcW().setW(ctPr2.getTcW().getW());
		}
		if (ctPr2.getVAlign() != null) {
			ctPr.addNewVAlign().setVal(ctPr2.getVAlign().getVal());
		}
		if (cttc2.getPList().size() > 0) {
			CTP ctp = cttc2.getPList().get(0);
			if (ctp.getPPr() != null) {
				if (ctp.getPPr().getJc() != null) {
					cttc.getPList().get(0).addNewPPr().addNewJc()
							.setVal(ctp.getPPr().getJc().getVal());
				}
			}
		}
		if (ctPr2.getTcBorders() != null) {
			ctPr.setTcBorders(ctPr2.getTcBorders());
		}

		XWPFParagraph tmpP = tmpCell.getParagraphs().get(0);
		XWPFParagraph cellP = cell.getParagraphs().get(0);
		XWPFRun tmpR = null;
		if (tmpP.getRuns() != null && tmpP.getRuns().size() > 0) {
			tmpR = tmpP.getRuns().get(0);
		}
		XWPFRun cellR = cellP.createRun();
		cellR.setText(text);
		// 复制字体信息
		if (tmpR != null) {
			if(!cellR.isBold()){
				cellR.setBold(tmpR.isBold());
			}
			cellR.setItalic(tmpR.isItalic());
			cellR.setUnderline(tmpR.getUnderline());
			cellR.setColor(tmpR.getColor());
			cellR.setTextPosition(tmpR.getTextPosition());
			if (tmpR.getFontSize() != -1) {
				cellR.setFontSize(tmpR.getFontSize());
			}
			if (tmpR.getFontFamily() != null) {
				cellR.setFontFamily(tmpR.getFontFamily());
			}
			if (tmpR.getCTR() != null) {
				if (tmpR.getCTR().isSetRPr()) {
					CTRPr tmpRPr = tmpR.getCTR().getRPr();
					if (tmpRPr.isSetRFonts()) {
						CTFonts tmpFonts = tmpRPr.getRFonts();
						CTRPr cellRPr = cellR.getCTR().isSetRPr() ? cellR
								.getCTR().getRPr() : cellR.getCTR().addNewRPr();
						CTFonts cellFonts = cellRPr.isSetRFonts() ? cellRPr
								.getRFonts() : cellRPr.addNewRFonts();
						cellFonts.setAscii(tmpFonts.getAscii());
						cellFonts.setAsciiTheme(tmpFonts.getAsciiTheme());
						cellFonts.setCs(tmpFonts.getCs());
						cellFonts.setCstheme(tmpFonts.getCstheme());
						cellFonts.setEastAsia(tmpFonts.getEastAsia());
						cellFonts.setEastAsiaTheme(tmpFonts.getEastAsiaTheme());
						cellFonts.setHAnsi(tmpFonts.getHAnsi());
						cellFonts.setHAnsiTheme(tmpFonts.getHAnsiTheme());
					}
				}
			}

		}
		// 复制段落信息
		cellP.setAlignment(tmpP.getAlignment());
		cellP.setVerticalAlignment(tmpP.getVerticalAlignment());
		cellP.setBorderBetween(tmpP.getBorderBetween());
		cellP.setBorderBottom(tmpP.getBorderBottom());
		cellP.setBorderLeft(tmpP.getBorderLeft());
		cellP.setBorderRight(tmpP.getBorderRight());
		cellP.setBorderTop(tmpP.getBorderTop());
		cellP.setPageBreak(tmpP.isPageBreak());
		if (tmpP.getCTP() != null) {
			if (tmpP.getCTP().getPPr() != null) {
				CTPPr tmpPPr = tmpP.getCTP().getPPr();
				CTPPr cellPPr = cellP.getCTP().getPPr() != null ? cellP
						.getCTP().getPPr() : cellP.getCTP().addNewPPr();
				// 复制段落间距信息
				CTSpacing tmpSpacing = tmpPPr.getSpacing();
				if (tmpSpacing != null) {
					CTSpacing cellSpacing = cellPPr.getSpacing() != null ? cellPPr
							.getSpacing() : cellPPr.addNewSpacing();
					if (tmpSpacing.getAfter() != null) {
						cellSpacing.setAfter(tmpSpacing.getAfter());
					}
					if (tmpSpacing.getAfterAutospacing() != null) {
						cellSpacing.setAfterAutospacing(tmpSpacing
								.getAfterAutospacing());
					}
					if (tmpSpacing.getAfterLines() != null) {
						cellSpacing.setAfterLines(tmpSpacing.getAfterLines());
					}
					if (tmpSpacing.getBefore() != null) {
						cellSpacing.setBefore(tmpSpacing.getBefore());
					}
					if (tmpSpacing.getBeforeAutospacing() != null) {
						cellSpacing.setBeforeAutospacing(tmpSpacing
								.getBeforeAutospacing());
					}
					if (tmpSpacing.getBeforeLines() != null) {
						cellSpacing.setBeforeLines(tmpSpacing.getBeforeLines());
					}
					if (tmpSpacing.getLine() != null) {
						cellSpacing.setLine(tmpSpacing.getLine());
					}
					if (tmpSpacing.getLineRule() != null) {
						cellSpacing.setLineRule(tmpSpacing.getLineRule());
					}
				}
				// 复制段落缩进信息
				CTInd tmpInd = tmpPPr.getInd();
				if (tmpInd != null) {
					CTInd cellInd = cellPPr.getInd() != null ? cellPPr.getInd()
							: cellPPr.addNewInd();
					if (tmpInd.getFirstLine() != null) {
						cellInd.setFirstLine(tmpInd.getFirstLine());
					}
					if (tmpInd.getFirstLineChars() != null) {
						cellInd.setFirstLineChars(tmpInd.getFirstLineChars());
					}
					if (tmpInd.getHanging() != null) {
						cellInd.setHanging(tmpInd.getHanging());
					}
					if (tmpInd.getHangingChars() != null) {
						cellInd.setHangingChars(tmpInd.getHangingChars());
					}
					if (tmpInd.getLeft() != null) {
						cellInd.setLeft(tmpInd.getLeft());
					}
					if (tmpInd.getLeftChars() != null) {
						cellInd.setLeftChars(tmpInd.getLeftChars());
					}
					if (tmpInd.getRight() != null) {
						cellInd.setRight(tmpInd.getRight());
					}
					if (tmpInd.getRightChars() != null) {
						cellInd.setRightChars(tmpInd.getRightChars());
					}
				}
			}
		}
	}

	/**
	 * @param ctGraphicalObject 图片数据
	 * @param deskFileName      图片描述
	 * @param width             宽
	 * @param height            高
	 * @param leftOffset        水平偏移 left
	 * @param topOffset         垂直偏移 top
	 * @param behind            文字上方，文字下方
	 * @return
	 * @throws Exception
	 */
	public static CTAnchor getAnchorWithGraphic(CTGraphicalObject ctGraphicalObject,
												String deskFileName, int width, int height,
												int leftOffset, int topOffset, boolean behind) {
		String anchorXML =
				"<wp:anchor xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
						+ "simplePos=\"0\" relativeHeight=\"0\" behindDoc=\"" + ((behind) ? 1 : 0) + "\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
						+ "<wp:simplePos x=\"0\" y=\"0\"/>"
						+ "<wp:positionH relativeFrom=\"column\">"
						+ "<wp:posOffset>" + leftOffset + "</wp:posOffset>"
						+ "</wp:positionH>"
						+ "<wp:positionV relativeFrom=\"paragraph\">"
						+ "<wp:posOffset>" + topOffset + "</wp:posOffset>" +
						"</wp:positionV>"
						+ "<wp:extent cx=\"" + width + "\" cy=\"" + height + "\"/>"
						+ "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
						+ "<wp:wrapNone/>"
						+ "<wp:docPr id=\"1\" name=\"Drawing 0\" descr=\"" + deskFileName + "\"/><wp:cNvGraphicFramePr/>"
						+ "</wp:anchor>";

		CTDrawing drawing = null;
		try {
			drawing = CTDrawing.Factory.parse(anchorXML);
		} catch (XmlException e) {
			e.printStackTrace();
		}
		CTAnchor anchor = drawing.getAnchorArray(0);
		anchor.setGraphic(ctGraphicalObject);
		return anchor;
	}

	public static void main(String[] args) throws IOException {
		String path = "C:\\Users\\56980\\Desktop\\工作文档\\功能开发\\一键报告新版\\template\\test.docx";
		String img_path = "C:\\Users\\56980\\Pictures\\work\\head.png";
		XWPFDocument document = null;
		OutputStream os = null;
		try {
			document = new XWPFDocument(new FileInputStream(path));
			List<XWPFParagraph> list = document.getParagraphs();
			XWPFParagraph paragraph0 = list.get(0);
			for (XWPFParagraph paragraph : list) {
				if (paragraph.getText().contains("{abc}")){
					createPicture(img_path,250, 200, paragraph,document);
					createPicture(img_path,250, 200, paragraph,document);
				}
			}
			//表格
			List<Map<String,String>>maps = new LinkedList<>();
			Map<String,String> map = new LinkedHashMap<>();
			map.put("name","XiaoHong");
			map.put("age","16");
			map.put("sex","female");
			map.put("interest","shopping");
			maps.add(map);
			
			Map<String,String> map2 = new LinkedHashMap<>();
			map2.put("name","XiaoMing");
			map2.put("age","16");
			map2.put("sex","male");
			map2.put("interest","football");
			maps.add(map2);
			insertValueToTable(document,maps,0);
			String downloadPath = "C:\\Users\\56980\\Desktop\\工作文档\\功能开发\\一键报告新版\\template\\targe.docx";
			os = new FileOutputStream(downloadPath);
			document.write(os);
		} catch (Exception e){
			e.printStackTrace();
		}finally {
			if (document != null) {
				document.close();
			}
			if (os != null) {
				os.close();
			}
		}

	}
}
