package org.gnos.netty.lab.common.codec;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.gnos.netty.lab.common.packet.Packet;
import org.gnos.netty.lab.common.utils.ProtobufUtil;

import java.util.List;

/**
 * WebSocket服务端、客户端packet编码器
 */
public class WebSocketEncoder extends MessageToMessageEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) {// throws Exception {
        Message message = packet.getMessage();
        short opcode = ProtobufUtil.getOpCode(message);
        byte[] bytes = message.toByteArray();
        int length = bytes.length;
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(opcode);//2
        buf.writeInt(length);//4
        buf.writeBytes(bytes);
        out.add(new BinaryWebSocketFrame(buf));
    }
}
