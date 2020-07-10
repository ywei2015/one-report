package com.nokia.kml.controller;

import com.nokia.kml.service.KMLService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class KMLExportController {

    @Autowired
    private KMLService kmlService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("kml")
    public ResponseEntity<JSONObject> getKml(@RequestParam String db,@RequestParam String spaceName
            , @RequestParam String layer, @RequestParam String styler,@RequestParam(required = false) String isCreate
            ,@RequestParam(required = false) String param,@RequestParam(required = false) String cqlFilter
            ,@RequestParam(required = false) Integer limit){
        logger.info("db:[{}],spaceName:[{}],layer:[{}],styler:[{}],isCreate:[{}],param:[{}],cqlFilter:[{}],limit:[{}]"
                ,db,spaceName,layer,styler,isCreate,param,cqlFilter,limit);
        Map<String,Object> map = kmlService.getKmls(db,spaceName,layer,styler,param,cqlFilter,limit);
        ResponseEntity responseEntity = ResponseEntity.ok(map);
        return responseEntity;
    }
}
