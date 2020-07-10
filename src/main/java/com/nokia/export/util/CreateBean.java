package com.nokia.export.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nokia.export.sso.SpringCasAutoconfig;

@Configuration
public class CreateBean {
	@Bean
	public SpringCasAutoconfig createSpringCasAutoconfig() {
		return new SpringCasAutoconfig();
	}
	
}
