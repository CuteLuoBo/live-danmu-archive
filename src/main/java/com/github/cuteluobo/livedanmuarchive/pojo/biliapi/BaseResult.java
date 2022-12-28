package com.github.cuteluobo.livedanmuarchive.pojo.biliapi;

import java.util.List;

/**
 * 基础返回数据
 * @author CuteLuoBo
 * @date 2022/12/28 13:51
 */
public class BaseResult<T> {
    public static int OK_CODE = 0;
    private int code;
    private String message;
    private int ttl;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
