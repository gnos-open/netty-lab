package org.gnos.netty.lab.common.packet;

import com.google.protobuf.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gnos.netty.lab.common.enums.Protocol;

/**
 * TCP数据包
 */
@Data
@AllArgsConstructor
public class TcpPacket implements Packet {

    private final Protocol protocol = Protocol.TCP;

    private Message message;

}
