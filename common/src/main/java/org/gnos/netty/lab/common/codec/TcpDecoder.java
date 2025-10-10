package org.gnos.netty.lab.common.codec;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import org.gnos.netty.lab.common.packet.TcpPacket;
import org.gnos.netty.lab.common.utils.ProtobufUtil;

import java.util.List;

/**
 * TCP服务端、客户端packet解码器
 */
@AllArgsConstructor
public class TcpDecoder extends ByteToMessageDecoder {

    /*是否服务端，客户端和服务端协议号opcode可以相同*/
    private final boolean serverSide;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {// throws Exception {
        if (in.readableBytes() < 2 + 4) {
            //wait
            return;
        }
        in.markReaderIndex();
        short opcode = in.readShort();//2
        int length = in.readInt();//4
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            //wait
            return;
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        //transform to packet
        Class<?> clazz = serverSide ? ProtobufUtil.getReqClass(opcode) : ProtobufUtil.getRespClass(opcode);
        Message message = ProtobufUtil.fromBytes(clazz, bytes);
        out.add(new TcpPacket(message));
    }
}
