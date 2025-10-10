package org.gnos.netty.lab.common.packet;

import com.google.protobuf.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gnos.netty.lab.common.enums.Protocol;

/**
 * WebSocket数据包
 */
@Data
@AllArgsConstructor
public class WebSocketPacket implements Packet {

    private final Protocol protocol = Protocol.WEBSOCKET;

    private Message message;

}
