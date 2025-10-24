package org.gnos.netty.lab.common.session;

import io.netty.channel.Channel;
import org.gnos.netty.lab.common.enums.Protocol;
import org.gnos.netty.lab.common.packet.Packet;

/**
 * 客户端连接数据通道
 */
public interface ClientChannel {

    Protocol getProtocol();

    String getClientId();

    Channel getChannel();

    default boolean isActive() {
        return getChannel() != null && getChannel().isActive();
    }

    default void writeAndFlush(Packet packet) {
        if (isActive()) {
            getChannel().writeAndFlush(packet);
        }
    }

    default void close() {
        if (isActive()) {
            getChannel().close();
        }
    }

}
