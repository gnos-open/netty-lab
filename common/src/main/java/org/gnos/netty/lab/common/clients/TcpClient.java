package org.gnos.netty.lab.common.clients;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.clients.handler.ClientPacketHandler;
import org.gnos.netty.lab.common.codec.TcpDecoder;
import org.gnos.netty.lab.common.codec.TcpEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * TCP客户端
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "netty.client.tcp.enabled", havingValue = "true")
public class TcpClient implements Client {

    @Value("${netty.client.tcp.host:127.0.0.1}")
    private String host;
    @Value("${netty.client.tcp.port:8081}")
    private int port;

    @Getter
    private Channel channel;

    @PostConstruct
    void start() {
        try {
            EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new TcpDecoder(false),
                                    new ClientPacketHandler(),
                                    new TcpEncoder()
                            );
                        }
                    });
            b.connect(host, port).addListener(cf -> {
                if (cf.isSuccess()) {
                    channel = ((ChannelFuture) cf).channel();
                    channel.closeFuture().addListener(future -> group.shutdownGracefully());
                } else {
                    log.error("connection failed");
                }
            });

        } catch (Exception e) {
            log.error(e.toString(), e);
            System.exit(-1);
        }
    }
}
