package org.gnos.netty.lab.common.codec;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.gnos.netty.lab.common.packet.Packet;
import org.gnos.netty.lab.common.utils.ProtobufUtil;

/**
 * TCP服务端、客户端packet编码器
 */
public class TcpEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {// throws Exception {
        Message message = packet.getMessage();
        short opcode = ProtobufUtil.getOpCode(message);
        byte[] bytes = message.toByteArray();
        int length = bytes.length;
        out.writeShort(opcode);//2
        out.writeInt(length);//4
        out.writeBytes(bytes);
    }
}
