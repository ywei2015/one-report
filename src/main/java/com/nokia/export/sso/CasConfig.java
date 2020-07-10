package com.nokia.export.sso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.bonc.sso.client.SSOFilter; 
  

//@Configuration
@Component
public class CasConfig {
  
    private static boolean casEnabled  = true;  
    @Autowired
    private SpringCasAutoconfig springCasAutoconfig;
    /** 
     * 用于实现单点登出功能 
     */  
    @Bean  
    public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener() {  
        ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> listener = new ServletListenerRegistrationBean<>();  
        listener.setEnabled(casEnabled);  
        listener.setListener(new SingleSignOutHttpSessionListener());  
        listener.setOrder(1);
        System.out.println("单点登出！");
        return listener;  
    }  
 
    //@Bean
    public FilterRegistrationBean authenticationFilterRegistrationBean() {
        FilterRegistrationBean authenticationFilter = new FilterRegistrationBean();  
        authenticationFilter.setFilter(new SSOFilter());  
        Map<String, String> initParameters = new HashMap<String, String>();
        initParameters.put("serverName", springCasAutoconfig.getServerName());
        initParameters.put("casServerUrlPrefix", springCasAutoconfig.getCasServerUrlPrefix());  
        initParameters.put("casServerLoginUrl", springCasAutoconfig.getCasServerLoginUrl());
        initParameters.put("singleSignOut", springCasAutoconfig.getSingleSignOut());
        initParameters.put("loginUserHandle", springCasAutoconfig.getLoginUserHandle());
        initParameters.put("characterEncoding", springCasAutoconfig.getCharacterEncoding());
        initParameters.put("encoding", springCasAutoconfig.getEncoding());
        initParameters.put("skipUrls", springCasAutoconfig.getSkipUrls());
        authenticationFilter.setInitParameters(initParameters);
        authenticationFilter.setOrder(2);  
        List<String> urlPatterns = new ArrayList<String>();  
        urlPatterns.add("/login");
        authenticationFilter.setUrlPatterns(urlPatterns);
        return authenticationFilter;  
    }  
}