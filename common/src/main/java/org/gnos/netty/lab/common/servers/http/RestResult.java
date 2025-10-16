package org.gnos.netty.lab.common.servers.http;

import lombok.Data;

@Data
public class RestResult<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> RestResult success(T t) {
        return new RestResult().setCode(0).setData(t);
    }

    public static <T> RestResult fail(int code, String msg) {
        return new RestResult().setCode(code).setMsg(msg);
    }
}
