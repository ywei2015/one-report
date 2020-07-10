package com.nokia.report.util.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;

public class CustomXSSFWorkbook extends XSSFWorkbook {

	private XSSFCellStyle font_border_bold_style;
	/**
	 * 设置四个边框
	 */
	private  XSSFCellStyle border_bold_style;
	private  XSSFCellStyle big_title_style;
	private  XSSFCellStyle title_style;

	public CustomXSSFWorkbook() {
		super();
		init();
	}

	private void init() {
		font_border_bold_style = this.createCellStyle();
		border_bold_style = this.createCellStyle();
		big_title_style = this.createCellStyle();
		title_style = this.createCellStyle();

		XSSFFont xssfFont = this.createFont();
		xssfFont.setBold(true);
		xssfFont.setFamily(FontFamily.ROMAN);
		font_border_bold_style.setFont(xssfFont);
		font_border_bold_style.setBorderBottom(BorderStyle.THIN);
		font_border_bold_style.setBorderLeft(BorderStyle.THIN);
		font_border_bold_style.setBorderRight(BorderStyle.THIN);
		font_border_bold_style.setBorderTop(BorderStyle.THIN);

		border_bold_style.setBorderBottom(BorderStyle.THIN);
		border_bold_style.setBorderLeft(BorderStyle.THIN);
		border_bold_style.setBorderRight(BorderStyle.THIN);
		border_bold_style.setBorderTop(BorderStyle.THIN);
		border_bold_style.setAlignment(HorizontalAlignment.CENTER);

		XSSFFont xssfFont2 = this.createFont();
		xssfFont2.setFontName("等线");
		xssfFont2.setBold(true);
		xssfFont2.setFontHeightInPoints((short) 11);
		big_title_style.setFont(xssfFont2);
		XSSFColor xssfColor = new XSSFColor(new Color(0,176,240));
		big_title_style.setFillBackgroundColor(xssfColor);
		big_title_style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		big_title_style.setAlignment(HorizontalAlignment.CENTER);
		title_style.setAlignment(HorizontalAlignment.CENTER);
	}

	public XSSFCellStyle getFont_border_bold_style() {
		return font_border_bold_style;
	}

	public void setFont_border_bold_style(XSSFCellStyle font_border_bold_style) {
		this.font_border_bold_style = font_border_bold_style;
	}

	public XSSFCellStyle getBorder_bold_style() {
		return border_bold_style;
	}

	public void setBorder_bold_style(XSSFCellStyle border_bold_style) {
		this.border_bold_style = border_bold_style;
	}

	public XSSFCellStyle getBig_title_style() {
		return big_title_style;
	}

	public void setBig_title_style(XSSFCellStyle big_title_style) {
		this.big_title_style = big_title_style;
	}

	public XSSFCellStyle getTitle_style() {
		return title_style;
	}

	public void setTitle_style(XSSFCellStyle title_style) {
		this.title_style = title_style;
	}
}
