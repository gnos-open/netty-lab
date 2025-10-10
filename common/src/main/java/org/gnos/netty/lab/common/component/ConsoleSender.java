package org.gnos.netty.lab.common.component;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.clients.TcpClient;
import org.gnos.netty.lab.common.clients.UdpClient;
import org.gnos.netty.lab.common.clients.WebSocketClient;
import org.gnos.netty.lab.common.packet.TcpPacket;
import org.gnos.netty.lab.common.packet.UdpPacket;
import org.gnos.netty.lab.common.packet.WebSocketPacket;
import org.gnos.netty.lab.proto.client.message.ChatReq_103;
import org.gnos.netty.lab.proto.client.message.LoginReq_102;
import org.gnos.netty.lab.proto.client.message.TestReq_101;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 控制台输入处理器，方便本地调试
 */
@Slf4j
@Component
public class ConsoleSender {

    @Autowired(required = false)
    TcpClient tcpClient;
    @Autowired(required = false)
    UdpClient udpClient;
    @Autowired(required = false)
    WebSocketClient webSocketClient;

    @PostConstruct
    void init() {
        new Thread(() -> {
            try {
                Message message = TestReq_101.newBuilder().setContent("收到请回复").setType(5).build();
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String msg = console.readLine();
                    if (msg == null) {
                        break;
                    } else if ("tcp".equalsIgnoreCase(msg)) {
                        tcpClient.getChannel().writeAndFlush(new TcpPacket(message)).addListener(cf -> {
                            if (cf.isSuccess()) {
                                log.info("tcp packet sent successfully");
                            } else {
                                log.error("tcp packet send failed:{}", cf);
                            }
                        });
                    } else if ("udp".equalsIgnoreCase(msg)) {
                        udpClient.getChannel().writeAndFlush(new UdpPacket(message, udpClient.getRecipient(), udpClient.getSender())).addListener(cf -> {
                            if (cf.isSuccess()) {
                                log.info("udp packet sent successfully");
                            } else {
                                log.error("udp packet send failed:{}", cf);
                            }
                        });
                    } else if ("websocket".equalsIgnoreCase(msg)) {
                        webSocketClient.getChannel().writeAndFlush(new WebSocketPacket(message)).addListener(cf -> {
                            if (cf.isSuccess()) {
                                log.info("websocket packet sent successfully");
                            } else {
                                log.error("websocket packet send failed:{}", cf);
                            }
                        });
                    } else if ("tcpLogin".equalsIgnoreCase(msg)) {
                        message = LoginReq_102.newBuilder().setToken("test-token").build();
                        tcpClient.getChannel().writeAndFlush(new TcpPacket(message)).addListener(cf -> {
                            if (cf.isSuccess()) {
                                log.info("tcp packet sent successfully");
                            } else {
                                log.error("tcp packet send failed:{}", cf);
                            }
                        });
                    } else if ("tcpChat".equalsIgnoreCase(msg)) {
                        message = ChatReq_103.newBuilder().setChannel("group").setMessage("发个聊天消息").build();
                        tcpClient.getChannel().writeAndFlush(new TcpPacket(message)).addListener(cf -> {
                            if (cf.isSuccess()) {
                                log.info("tcp packet sent successfully");
                            } else {
                                log.error("tcp packet send failed:{}", cf);
                            }
                        });
                    } else if ("print".equalsIgnoreCase(msg)) {
                        ByteBuf buf = Unpooled.buffer();
                        buf.writeShort(101);//2
                        buf.writeInt(message.toByteArray().length);//4
                        buf.writeBytes(message.toByteArray());
                        System.out.println(ByteBufUtil.hexDump(buf));
                    } else {
                        log.error("action {} not supported", msg);
                    }
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }).start();
    }


}
