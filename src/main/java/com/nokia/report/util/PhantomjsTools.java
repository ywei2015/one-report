package com.nokia.report.util;

import com.nokia.export.util.CommonPath;
import com.nokia.report.thread.YQPhantomPictureGatherThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @Description:根据网页地址转换成图片
 * @Author: yww
 */
public class PhantomjsTools {
    //private static String tempPath = CommonPath.getConfig("PhantomjsImgPath");
    private static String tempPath = "C:/export/phantom/img";
    private static String BLANK = " ";
    private static String command = "phantomjs";
    private static String config_path = CommonPath.getProjectRootPath()+"phantomjs/config.json";
    private static String jsPath = CommonPath.getProjectRootPath() + "phantomjs/ptm.js";

    private static Logger logger = LoggerFactory.getLogger(PhantomjsTools.class);

    // 执行cmd命令
    public static String cmd(String imgagePath, String url,String id) {
        return command  + config_path + BLANK + jsPath + BLANK + url + BLANK + id + BLANK + imgagePath;
    }

    // 执行cmd命令
    public static String cmd2(String param) {
        String cmd = command + BLANK + param;
        logger.info(cmd);
        return cmd;
    }
    //关闭命令
    public static void close(Process process, BufferedReader bufferedReader) throws IOException {
        if (bufferedReader != null) {
            bufferedReader.close();
        }
        if (process != null) {
            process.destroy();
        }
    }

    /**
     * @throws IOException
     */
    public static String printUrlScreenjpg(YQPhantomPictureGatherThread phantomThread) throws IOException{
        String status = ProductConstant.FINISHED;
        //Java中使用Runtime和Process类运行外部程序
        String flag = phantomThread.getInfo_params();
        logger.info("[{}] begin [{}] times screenShot",flag,phantomThread.getTaskRetryTimes()+1);
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec(cmd2(phantomThread.getPhantom_params()));
            InputStream inputStream = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sft = new StringBuilder();
            String tmp;
            process.waitFor();
            while ((tmp = reader.readLine()) != null) {
                sft = sft.append(tmp);
            }
            phantomThread.setRes_msg(sft.toString());
            logger.info("[{}] res:[{}]",flag,sft);
        } catch (InterruptedException e) {
            status = ProductConstant.UNFINISHED;
            logger.error(e.getMessage());
        }finally {
            if (process != null) {
                process.destroy();
            }
            if (reader != null){
                reader.close();
            }
        }
        return status;
    }
}