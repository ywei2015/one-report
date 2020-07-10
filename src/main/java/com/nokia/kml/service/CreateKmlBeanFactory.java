package com.nokia.kml.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateKmlBeanFactory {
	@Autowired
	private CreatePolyKml createPolyKml;

	@Autowired
	private CreateTextKml createTextKml;

	public AbstractCreateKml getCreateKml(String styler){
		if (styler.endsWith("_TEXT")){
			return createTextKml;
		}else {
			return createPolyKml;
		}
	}
}
