package com.nokia.kml.service;

import com.nokia.kml.model.KMLAssit;
import com.nokia.kml.model.StyleRule;
import com.nokia.export.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.util.*;

/**
 * @author yww
 * @Date 2019-07-01
 */
@Service
public class KMLService {
    @Autowired
    private Environment env;
    @Autowired
    private GeoFileService geoFileService;
    @Autowired
    private CreateKmlBeanFactory createKmlBeanFactory;



    private static String DBNAME = "dbzc";
    private static int limit_ = Integer.valueOf(CommonPath.getConfig("kmlFileLimit"));
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Map<String, Object> getKmls( String db, String spaceName, String layer, String style, String param
            , String filter, Integer limit) {
        Map<String, Object> map = new LinkedHashMap<>();
        String commonName = CommonPath.getConfig("KMLPath")+ layer;
        String randomStr = RandomUtil.randomString(8).toUpperCase();
        String zipPath =  commonName+"/"+ layer +"-"+ randomStr+".zip";
        logger.info("开始生成kml:{}",new Date(System.currentTimeMillis()));

        List<StyleRule> styleRules = geoFileService.listStyler(spaceName,style);
        String sql = geoFileService.getSql(spaceName,db,layer,param,filter);
        DBQuery dbQuery = new DBQuery();
        try {
            dbQuery.getCon(DBNAME,env);
            int count = DBUtil.getCount(dbQuery,sql);
            logger.info("一共:{}条数据",count);
            KMLAssit kmlAssit = new KMLAssit(styleRules,layer);
            AbstractCreateKml createKml = createKmlBeanFactory.getCreateKml(style);
            if (count > 0){
                StringBuilder styleStr = createKml.calculateStyles(kmlAssit);
                kmlAssit.setStyle(styleStr);
            }else {
                dbQuery.close();
                return map;
            }
            List<String> pathList = new LinkedList<>();
            if (limit == null || limit <= 0){
                limit = limit_;
            }
            for (int i = 0; i*limit <= count ; i++) {
                String sql_ = DBUtil.getPageSql(sql,limit,i);
                String path = commonName +"/temp/" + layer +"-"+ randomStr+"-" +i +".kml";
                createKml.CreateKmlContent(sql_,path,dbQuery,kmlAssit);
                pathList.add(path);
                dbQuery.closeSource();
            }
            map = outPutStrategy(pathList,zipPath);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("异常:",e.toString());
        }finally {
            try {
                dbQuery.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private Map<String, Object> outPutStrategy(List<String> pathList,String zipPath) {
        Map<String, Object> map = new LinkedHashMap<>();
        String outPath;
        if (pathList.size() > 1){
            try {
                FileOperation.zipFilesAndEncrypt(pathList,zipPath,null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            outPath = zipPath;
        }else {
            outPath = pathList.get(0);
        }
        map.put("path",outPath);
        long size = FileOperation.getFileSize(outPath);
        map.put("size",size);
        map.put("unit","byte");
        logger.info("生成kml结束:{}",new Date(System.currentTimeMillis()));
        return map;
    }

}
