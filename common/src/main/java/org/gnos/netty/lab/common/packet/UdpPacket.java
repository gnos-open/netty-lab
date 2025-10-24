package org.gnos.netty.lab.common.packet;

import com.google.protobuf.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gnos.netty.lab.common.enums.Protocol;

import java.net.InetSocketAddress;

/**
 * UDP数据包
 */
@Data
@AllArgsConstructor
public class UdpPacket implements Packet {

    private final Protocol protocol = Protocol.UDP;

    private Message message;

    private InetSocketAddress recipient;

    private InetSocketAddress sender;

}
