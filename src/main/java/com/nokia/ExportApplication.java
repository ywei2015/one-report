package com.nokia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ExportApplication extends SpringBootServletInitializer{

	private static final int AWAIT_TERMINATION_SECONDS = 30;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ExportApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ExportApplication.class, args);
	}

	/**
	 * 全局线程池。
	 * @return 线程池实例
	 */
	@Bean
	public ThreadPoolExecutorFactoryBean executorService() {
		int size = Runtime.getRuntime().availableProcessors();
		System.out.println("availableProcessors:" + size);
		int process = 3;
		if (process > 10) {
			process = 10;
		}
		ThreadPoolExecutorFactoryBean f = new ThreadPoolExecutorFactoryBean();
		f.setCorePoolSize(process);
		f.setMaxPoolSize(process);
		f.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
		return f;
	}
}
