package com.nokia.report.pojo;


public class YQParamDTO {
	private String city;
	private String startTime;
	private String endTime;
	private String userType;
	private String userInput;
	private String userId;
	private String curCoord;
	private String zoom;

	public YQParamDTO(String city, String startTime, String endTime, String userType, String userInput, String userId, String zoom) {
		this.city = city;
		this.startTime = startTime;
		this.endTime = endTime;
		this.userType = userType;
		if (userType.equalsIgnoreCase(userInput)){
			this.userInput = "all";
		}else {
			this.userInput = userInput;
		}
		this.userId = userId;
		this.zoom = zoom;
		this.curCoord = "117.2351|39.1707";
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String usertype) {
		this.userType = usertype;
	}

	public String getUserInput() {
		return userInput;
	}

	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getZoom() {
		return zoom;
	}

	public void setZoom(String zoom) {
		this.zoom = zoom;
	}

	public String getCurCoord() {
		return curCoord;
	}

	public void setCurCoord(String curCoord) {
		this.curCoord = curCoord;
	}

	@Override
	public String toString() {
		return city + "-" + userInput + "-"+userType;
	}
}
