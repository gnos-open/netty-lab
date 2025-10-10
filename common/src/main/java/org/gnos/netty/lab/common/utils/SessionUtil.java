package org.gnos.netty.lab.common.utils;

import com.google.protobuf.Message;
import org.gnos.netty.lab.common.packet.Packet;
import org.gnos.netty.lab.common.packet.TcpPacket;
import org.gnos.netty.lab.common.packet.UdpPacket;
import org.gnos.netty.lab.common.packet.WebSocketPacket;
import org.gnos.netty.lab.common.session.ClientSession;

/**
 * 会话工具类封装
 */
public class SessionUtil {

    public static void send(ClientSession session, Packet packet) {
        session.writeAndFlush(packet);
    }

    public static void send(ClientSession session, Message message) {
        switch (session.getProtocol()) {
            case TCP -> session.writeAndFlush(new TcpPacket(message));
            case UDP -> session.writeAndFlush(new UdpPacket(message, null, null));
            case WEBSOCKET -> session.writeAndFlush(new WebSocketPacket(message));
        }
    }

}
