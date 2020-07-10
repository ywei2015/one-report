package com.nokia.report.pojo;

public class PPTRemarkDTO {
	private String str;
	private String mr;
	private Integer step;
	private Integer index;

	public PPTRemarkDTO(String remark) {
		str = remark;
		String[] mes = remark.split("-");
		mr = mes[0];
		step = Integer.valueOf(mes[1]);
		index = Integer.valueOf(mes[2]);
	}

	public String getMr() {
		return mr;
	}

	public void setMr(String mr) {
		this.mr = mr;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
}
