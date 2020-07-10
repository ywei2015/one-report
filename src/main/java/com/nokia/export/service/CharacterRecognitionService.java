package com.nokia.export.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nokia.export.util.Base64Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 图像二维码识别
 */
@Service
public class CharacterRecognitionService {
    private static String SECRETKEY = "AwB7B6HYvVe9VnIgRBTZjTewxeei3GzI";
    private static String APIKEY = "S1mVtRWxKZUwXGUGBnQwBVwF";
    private static String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static String OCRUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    private static HashMap<String,Object> tokens = new LinkedHashMap<>(2);

    @Autowired
    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public  String getAccessToken() {
        String accessToken = "";
        String orign_token = (String) tokens.get("token");
        if (StringUtils.isNotBlank(orign_token)){
            long create_time = (long) tokens.get("createTime");
            long times = 7*24*3600*1000;
            if (times > (System.currentTimeMillis() - create_time)){
                return orign_token;
            }
        }
        MultiValueMap<String, Object> postParams = new LinkedMultiValueMap<>();
        postParams.add("grant_type", "client_credentials");
        postParams.add("client_id", APIKEY);
        postParams.add("client_secret", SECRETKEY);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(postParams,headers);
        String respText = restTemplate.postForObject(TOKEN_URL, httpEntity, String.class);
        logger.info("请求token返回的数据：{}", respText);
        JSONObject res = JSONObject.parseObject(respText);
        if (res != null && !res.isEmpty()){
            accessToken = (String) res.get("access_token");
            tokens.put("token",accessToken);
            tokens.put("createTime",System.currentTimeMillis());
        }
        return accessToken;
    }

    public  String OCRVCode(String imageUrl,String imgCode){
        String VCode = "";
        String access_token = getAccessToken();
        if (StringUtils.isBlank(access_token)) {
            logger.error("accessToken为空");
            return VCode;
        }
        OCRUrl = OCRUrl + "?access_token=" + access_token;
        MultiValueMap<String, Object> postParams = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        String image;
        if (StringUtils.isNotBlank(imgCode)){
            logger.info("收到前端code:{}",imgCode);
            image = imgCode;
        }else {
            image = encodeImgageToBase64(imageUrl);
        }
        postParams.add("image", image);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(postParams,headers);
        String respText = restTemplate.postForObject(OCRUrl, httpEntity, String.class);
        logger.info("请求OCRVCode返回的数据：{}", respText);
        JSONObject res = JSONObject.parseObject(respText);
        JSONArray wordsResult = res.getJSONArray("words_result");
        VCode = wordsResult.getJSONObject(0).getString("words");
        return VCode;
    }

    /**
     * 将本地图片进行Base64位编码
     * @param imageFile
     * @return
     */
    public static String encodeImgageToBase64(String imageFile) {
        // 其进行Base64编码处理
        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imageFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        String code = Base64Util.encode(data);
        System.out.println(code);
        return code;
    }

    public static void main(String[] args) {
        String path = "C:\\workspace\\nokia\\ExportServer\\src\\main\\resources\\static\\190.png";
        String coe = CharacterRecognitionService.encodeImgageToBase64(path);
        System.out.println(coe);
    }

}
