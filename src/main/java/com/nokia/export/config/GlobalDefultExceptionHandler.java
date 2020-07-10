package com.nokia.export.config;

import com.nokia.export.pojo.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestControllerAdvice
public class GlobalDefultExceptionHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@ExceptionHandler(RuntimeException.class)
	public R exceptionHandler(Exception e, HttpServletResponse response, HttpSession session) throws IOException {
		R resp = R.error();
		resp.setMessage(e.getMessage());
		logger.error("error:", e);
		return resp;
	}
}