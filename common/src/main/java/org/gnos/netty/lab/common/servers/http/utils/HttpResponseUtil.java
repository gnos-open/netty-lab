package org.gnos.netty.lab.common.servers.http.utils;

import cn.hutool.json.JSONUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.gnos.netty.lab.common.servers.http.RestResult;

import java.util.Set;
import java.util.stream.Collectors;

public class HttpResponseUtil {

    public static void writeOptionsResponse(ChannelHandlerContext ctx, Set<String> options) {
        // 构建HTTP响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK
        );
        // 设置响应头
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, options.stream().collect(Collectors.joining(",")));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "86400");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        // 发送响应
        ctx.writeAndFlush(response);
    }

    public static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, RestResult result, boolean isHEAD) {
        // 构建HTTP响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(JSONUtil.toJsonStr(result), CharsetUtil.UTF_8)
        );
        // 设置响应头
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        // 发送响应

        if (isHEAD) {
            //HEAD请求去掉body
            response.content().clear();
        }

        ctx.writeAndFlush(response);
    }

}
