package org.gnos.netty.lab.common.clients.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.packet.Packet;

/**
 * 客户端packet处理器
 */
@Slf4j
public class ClientPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {// throws Exception {
        //简单打印，可根据自己的实际需求扩展
        log.info("client receive packet:{}", packet.toLogString());
    }
}
