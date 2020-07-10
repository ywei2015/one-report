package com.nokia.export.pojo;

import java.util.HashMap;
import java.util.Map;

public class R  extends HashMap<String, Object>{
    public R() {
        put("code", 2000);
    }

    public static R ok() {
        return new R();
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R error() {
        return error(5000, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(5000, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    /**
     * 重写put方法，返回当前对象
     * @param key   键
     * @param value 值
     * @return 当前对象
     */
    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
    public  boolean isError(){
        if ((Integer)this.get("code") == 5000){
            return true;
        }else {
            return false;
        }
    }

    public void setMessage(String msg){
        super.put("msg",msg);
    }

    public void setData(String data) {
        super.put("data",data);
    }
}