package org.gnos.netty.lab.common.servers.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.enums.Protocol;
import org.gnos.netty.lab.common.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * WebSocket会话处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@ConditionalOnProperty(value = "netty.server.websocket.enabled", havingValue = "true")
public class WebSocketSessionHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    SessionManager sessionManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //open
        sessionManager.onConnected(ctx.channel(), Protocol.WEBSOCKET);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //close
        sessionManager.onClosed(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        super.exceptionCaught(ctx, e);
        log.error(e.toString(), e);
    }
}
