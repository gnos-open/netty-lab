package org.gnos.netty.lab.common.packet;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import org.gnos.netty.lab.common.enums.Protocol;
import org.gnos.netty.lab.common.utils.ProtobufUtil;

import java.net.InetSocketAddress;

/**
 * 数据包封装
 */
public interface Packet {

    Protocol getProtocol();

    default short getOpcode() {
        return ProtobufUtil.getOpCode(getMessage());
    }

    Message getMessage();

    default Class<? extends Message> getMessageClass() {
        if (getMessage() != null) {
            return getMessage().getClass();
        }
        return null;
    }

    default InetSocketAddress getRecipient() {
        return null;
    }

    default InetSocketAddress getSender() {
        return null;
    }

    default String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("protocol=").append(getProtocol()).append(",");
        sb.append("recipient=").append(getRecipient()).append(",");
        sb.append("sender=").append(getSender()).append(",");
        sb.append("message=").append(TextFormat.printer().escapingNonAscii(false).shortDebugString(getMessage()));
        sb.append("]");
        return sb.toString();
    }

}
