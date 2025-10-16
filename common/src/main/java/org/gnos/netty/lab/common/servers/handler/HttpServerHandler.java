package org.gnos.netty.lab.common.servers.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.enums.RestMethodType;
import org.gnos.netty.lab.common.servers.http.RestResult;
import org.gnos.netty.lab.common.servers.http.macher.PathMatcher;
import org.gnos.netty.lab.common.servers.http.manager.RestHandlerManager;
import org.gnos.netty.lab.common.servers.http.utils.HttpResponseUtil;
import org.gnos.netty.lab.common.servers.http.utils.RestMethodUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@AllArgsConstructor
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final RestHandlerManager restHandlerManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
// 获取请求方法和URI
        HttpMethod method = request.method();
        String uri = request.uri();
        String uriRaw = uri.split("\\?", 2)[0];
        log.info("收到请求：uri={}, uriRaw={}", uri, uriRaw);

        //匹配handler
        RestMethodType restMethodType = RestMethodType.fromHttpMethod(method);
        if (restMethodType == null) {
            //method not impl
            HttpResponseUtil.writeResponse(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, RestResult.fail(0, "不支持的方法"), false);
            return;
        }
        //OPTIONS请求（解析path获取方法列表）
        if (restMethodType == RestMethodType.OPTIONS) {
            Set<String> options = restHandlerManager.match(uriRaw);
            if (CollUtil.isEmpty(options)) {
                //404
                HttpResponseUtil.writeResponse(ctx, HttpResponseStatus.NOT_FOUND, RestResult.fail(0, "不支持的请求路径"), false);
                return;
            }
            HttpResponseUtil.writeOptionsResponse(ctx, options);
            return;
        }

        //PathVar
        PathMatcher.MatchResult pathResult = restHandlerManager.match(restMethodType, uriRaw);
        log.info("pathVars:{}", pathResult != null ? pathResult.pathVariables() : pathResult);
        if (pathResult == null) {
            //404
            HttpResponseUtil.writeResponse(ctx, HttpResponseStatus.NOT_FOUND, RestResult.fail(0, "不支持的请求路径"), false);
            return;
        }

        //解析header备用
        HttpHeaders headers = request.headers();
        Set<String> names = headers.names();
        Map<String, String> header = new HashMap<>();
        names.forEach(name -> header.put(name, headers.get(name)));

        log.info("headers:{}", header);

        //QueryVars
        Map<String, Object> params = new HashMap<>();
        //JSON body
        JSONObject jsonBody = null;

        if (restMethodType == RestMethodType.GET || restMethodType == RestMethodType.HEAD) {
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            decoder.parameters().entrySet().forEach(entry -> {
                // entry.getValue()是一个List, 只取第一个元素
                params.put(entry.getKey(), entry.getValue().get(0));
            });
            log.info("params:{}", params);
        } else if (restMethodType == RestMethodType.POST) {
            //约定post方法只能带json body或multipart-data（暂不处理文件上传）
            jsonBody = toJSON(request);
        } else if (restMethodType == RestMethodType.DELETE) {
            //什么都不做，约定delete方法不带body
        } else {//PUT、PATCH
            //约定put/patch方法只能带json body
            jsonBody = toJSON(request);
        }

        log.info("body:{}", jsonBody);

        //异步响应
        RestMethodUtil.invoke(ctx, header, pathResult, params, jsonBody, restMethodType == RestMethodType.HEAD);
    }

    JSONObject toJSON(FullHttpRequest request) {
        String jsonBody = request.content().toString(CharsetUtil.UTF_8);
        return JSONUtil.parseObj(jsonBody);
    }

}
