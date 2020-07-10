package com.nokia.export.enums;

public enum PhantomjsTaskEnums {
	FINISHED(2000,"finished"),
	FAIL(5000,"failed"),
	RUNNING(1000,"running"),
	UNDOMAIN(5004,"unknown");


	private Integer code;
	private String msg;
	PhantomjsTaskEnums(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
