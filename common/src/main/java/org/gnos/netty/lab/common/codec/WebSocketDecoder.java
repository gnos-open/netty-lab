package org.gnos.netty.lab.common.codec;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.packet.WebSocketPacket;
import org.gnos.netty.lab.common.utils.ProtobufUtil;

import java.util.List;

/**
 * WebSocket服务端、客户端packet解码器
 */
@Slf4j
@AllArgsConstructor
public class WebSocketDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {

    /*是否服务端，客户端和服务端协议号opcode可以相同*/
    private final boolean serverSide;

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame, List<Object> out) {// throws Exception {
        ByteBuf in = frame.content();
        if (in.readableBytes() < 2 + 4) {
            //丢掉 & 记录错误日志
            log.error("websocket frame decode error, frame={}", frame);
            return;
        }
        short opcode = in.readShort();//2
        int length = in.readInt();//4
        if (in.readableBytes() < length) {
            //丢掉 & 记录错误日志
            log.error("websocket frame decode error, opcode={}", opcode);
            return;
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        //transform to packet
        Class<?> clazz = serverSide ? ProtobufUtil.getReqClass(opcode) : ProtobufUtil.getRespClass(opcode);
        Message message = ProtobufUtil.fromBytes(clazz, bytes);
        out.add(new WebSocketPacket(message));
    }
}
