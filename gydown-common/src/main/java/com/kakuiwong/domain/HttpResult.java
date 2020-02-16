package com.kakuiwong.domain;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class HttpResult<T> extends HashMap<String, Object> {
    private Integer code;
    private String message;
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public HttpResult() {
    }

    public HttpResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.put("code", code);
        this.put("message", message);
        this.put("data", data);
    }


    public static HttpResult ok(String message, Object data) {
        HttpResult httpResult = new HttpResult(200, message, data);
        return httpResult;
    }

    public static HttpResult notOk(Integer code, String message) {
        HttpResult httpResult = new HttpResult(code, message, null);
        return httpResult;
    }

    public static String okJson(String message, Object data) {
        return JSON.toJSONString(ok(message, data));
    }

    public static String notOkJson(Integer code, String message) {
        return JSON.toJSONString(notOk(code, message));
    }
}
