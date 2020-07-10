package com.nokia.report.pojo;

import com.nokia.export.enums.PhantomjsTaskEnums;

public class ReportMonitor {
	private String msg;
	private Integer status;
	private String progress;
	private Integer number;
	private Integer total;
	private String userId;


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public ReportMonitor() {
		this.msg = "task created";
		this.status = PhantomjsTaskEnums.RUNNING.getCode();
		this.progress = "0";
		this.number = 0;
	}
}
