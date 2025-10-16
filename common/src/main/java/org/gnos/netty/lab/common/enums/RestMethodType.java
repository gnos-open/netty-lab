package org.gnos.netty.lab.common.enums;

import io.netty.handler.codec.http.HttpMethod;

public enum RestMethodType {
    GET,//
    POST,//
    PUT,//
    PATCH,//
    DELETE,//
    HEAD,//
    OPTIONS,//
    //
    ;

    public static RestMethodType fromHttpMethod(HttpMethod method) {
        switch (method.name()) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            case "PUT":
                return PUT;
            case "PATCH":
                return PATCH;
            case "DELETE":
                return DELETE;
            case "HEAD":
                return HEAD;
            case "OPTIONS":
                return OPTIONS;
        }
        return null;
    }
}
