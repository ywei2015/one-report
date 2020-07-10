package com.nokia.export.enums;

public enum DateTypeEnums {
	DAY("DAY","YYYYMMDD"),
	MTH("MTH","YYYYMM"),
	WK("WK","YYYYWW");

	private String dateType;
	private String dateFormat;

	DateTypeEnums(String dateType, String format) {
		this.dateFormat = format;
		this.dateType = dateType;
	}

	public String getDateType() {
		return dateType;
	}

	public String getDateFormat() {
		return dateFormat;
	}
}
