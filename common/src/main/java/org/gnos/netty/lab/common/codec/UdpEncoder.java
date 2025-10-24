package org.gnos.netty.lab.common.codec;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.AllArgsConstructor;
import org.gnos.netty.lab.common.packet.UdpPacket;
import org.gnos.netty.lab.common.utils.ProtobufUtil;

import java.util.List;

/**
 * UDP服务端、客户端packet编码器
 */
@AllArgsConstructor
public class UdpEncoder extends MessageToMessageEncoder<UdpPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UdpPacket packet, List<Object> out) {// throws Exception {
        Message message = packet.getMessage();
        short opcode = ProtobufUtil.getOpCode(message);
        byte[] bytes = message.toByteArray();
        int length = bytes.length;
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(opcode);//2
        buf.writeInt(length);//4
        buf.writeBytes(bytes);
        out.add(new DatagramPacket(buf, packet.getRecipient()));
    }
}
