package org.gnos.netty.lab.common.servers.http.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.servers.http.ParamInfo;
import org.gnos.netty.lab.common.servers.http.RestResult;
import org.gnos.netty.lab.common.servers.http.anno.*;
import org.gnos.netty.lab.common.servers.http.controller.HttpController;
import org.gnos.netty.lab.common.servers.http.macher.PathMatcher;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RestMethodUtil {

    public static void invoke(ChannelHandlerContext ctx, Map<String, String> header, PathMatcher.MatchResult pathResult, Map<String, Object> params, JSONObject jsonBody, boolean isHEAD) {
        Method method = pathResult.handleMethod().method();
        HttpController handler = pathResult.handleMethod().handler();
        ParamInfo[] paramInfos = pathResult.handleMethod().paramInfos();
        Map<String, String> pvMap = pathResult.pathVariables();
        Object[] objects = new Object[paramInfos.length];

        //处理实参
        for (int i = 0; i < paramInfos.length; i++) {
            ParamInfo pi = paramInfos[i];
            if (HeaderMap.class.equals(pi.getAnnotation())) {
                //header
                objects[i] = header;
            } else if (BodyJson.class.equals(pi.getAnnotation())) {
                //body
                objects[i] = Convert.convert(pi.getType(), jsonBody);
            } else if (QueryModel.class.equals(pi.getAnnotation())) {
                //model
                objects[i] = Convert.convert(pi.getType(), params);
            } else if (QueryVar.class.equals(pi.getAnnotation())) {
                //var
                Object var = params.get(pi.getName());
                objects[i] = Convert.convert(pi.getType(), var);
            } else if (PathVar.class.equals(pi.getAnnotation())) {
                //path var
                String var = pvMap.get(pi.getName());
                objects[i] = Convert.convert(pi.getType(), var);
            }
        }

        CompletableFuture.supplyAsync(() -> invoke(method, handler, objects))
                .thenAccept(result -> {
                    HttpResponseUtil.writeResponse(ctx, HttpResponseStatus.OK, result, isHEAD);
                });
    }

    static RestResult invoke(Method method, HttpController handler, Object[] objects) {
        log.error("invoke method:{}, handler:{}, objects:{}", method.getName(), handler.getClass().getSimpleName(), JSONUtil.toJsonStr(objects));
        try {
            return (RestResult) method.invoke(handler, objects);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return RestResult.fail(-1, e.toString());
        }
    }

}
