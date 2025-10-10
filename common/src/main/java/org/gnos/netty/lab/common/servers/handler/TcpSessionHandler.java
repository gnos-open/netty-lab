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
 * TCP会话处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@ConditionalOnProperty(value = "netty.server.tcp.enabled", havingValue = "true")
public class TcpSessionHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    SessionManager sessionManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //open
        sessionManager.onConnected(ctx.channel(), Protocol.TCP);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //close
        sessionManager.onClosed(ctx.channel());
    }
}
