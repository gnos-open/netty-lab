package org.gnos.netty.lab.common.servers.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.packet.Packet;
import org.gnos.netty.lab.common.packet.handler.HandlerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据包处理入口
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class PacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Autowired
    HandlerManager handlerManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {// throws Exception {
        log.info("receive packet:{}", packet.toLogString());
        handlerManager.handle(ctx.channel(), packet);
    }
}
