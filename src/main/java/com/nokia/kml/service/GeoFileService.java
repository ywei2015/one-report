package com.nokia.kml.service;

import com.nokia.kml.model.StyleRule;
import com.nokia.export.util.CommonPath;
import com.nokia.export.util.XMLFileTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;

/**
 * 访问geoserver接口获取sql file ，styler file
 */
@Service
public class GeoFileService {

    @Autowired
    private RestTemplate restTemplate;

    private static HashMap<String,Object> COOKIES = new LinkedHashMap<>();
    private static final String ACCOUNT = CommonPath.getConfig("geoAccount");
    private static final String PASSWORD = CommonPath.getConfig("geoPassword");
    private static final String GEO_HOST = CommonPath.getConfig("geoHost");
    private static final String GEO_LOGIN_URL = GEO_HOST + "geoserver/j_spring_security_check";
    private static final String GEO_SQL_FILE_URL = GEO_HOST + "geoserver/rest/workspaces/{0}/datastores/{1}/featuretypes/{2}.xml";
    private static final String GEO_STYLER_FILE_URL = GEO_HOST + "geoserver/rest/workspaces/{0}/styles/{1}.sld";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 模拟登陆geoserver web获取cookie
     */
    private String login(){
        String url = GEO_LOGIN_URL + "?" + "username=" + ACCOUNT + "&password=" +PASSWORD;
        Map<String,Object> map = new LinkedHashMap<>();
        ResponseEntity responseEntity = restTemplate.postForEntity(url,null, String.class, map);
        if (responseEntity != null){
            HttpHeaders httpHeaders = responseEntity.getHeaders();
            List<String> strings =  httpHeaders.get("Set-Cookie");
            String cookies = strings.get(0);
            return cookies;
        }else {
            throw new RuntimeException("模拟登陆GEOSESRVER失败");
        }
    }

    private String sendHttp(String url){
        String cookies = login();
        List<String> cookieList = new ArrayList<>();
        cookieList.add(cookies);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.put("Cookie", cookieList);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        String res = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
        return res;
    }

    /**
     * 从geo获取sql 并设置默认值
     * @param workSpace
     * @param dataStore
     * @param feature
     * @return*/

   /* public String getSql(String workSpace, String dataStore, String feature, String param, String filter){
        String url = MessageFormat.format(GEO_SQL_FILE_URL,workSpace,dataStore,feature);
        String res = sendHttp(url);
        String sql = null;
        try {
            InputStream inputStream = new ByteArrayInputStream(res.getBytes("UTF-8"));
            sql = XMLFileTools.getSqlAndSetDefaultParam(inputStream, param, filter);
            inputStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return sql;
    }

    public List<StyleRule> listStyler(String workSpace, String styler){
        String url = MessageFormat.format(GEO_STYLER_FILE_URL,workSpace,styler);
        String res = sendHttp(url);
        List<StyleRule> styleRules = XMLFileTools.getStyleRuleByContent(res,styler);
        return styleRules;
    }*/
    public String getSql(String workSpace, String dataStore, String feature, String param, String filter){
        String url = CommonPath.getConfig("geoPath")+"\\FAST\\FAST_ORACLE_2\\GET_CELL_GEO\\featuretype.xml";
        String sql = null;
        try {
            InputStream inputStream = new FileInputStream(url);
            sql = XMLFileTools.getSqlAndSetDefaultParam(inputStream, param, filter);
            inputStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return sql;
    }

    public List<StyleRule> listStyler(String workSpace, String styler){
        String url = "C:\\Users\\56980\\Documents\\WeChat Files\\w2015ei\\FileStorage\\File\\2019-10\\CELL_REAL_SEC_TEXT.sld";
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(url)));
            while (((line = in.readLine()) != null)){
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<StyleRule> styleRules = XMLFileTools.getStyleRuleByContent(buffer.toString(),null);
        return styleRules;
    }
}
