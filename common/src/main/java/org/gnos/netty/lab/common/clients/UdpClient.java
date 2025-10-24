package org.gnos.netty.lab.common.clients;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.clients.handler.ClientPacketHandler;
import org.gnos.netty.lab.common.codec.UdpDecoder;
import org.gnos.netty.lab.common.codec.UdpEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * UDP客户端
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "netty.client.udp.enabled", havingValue = "true")
public class UdpClient implements Client {

    @Getter
    InetSocketAddress recipient;
    @Getter
    InetSocketAddress sender;
    @Value("${netty.client.udp.host:127.0.0.1}")
    private String host;
    @Value("${netty.client.udp.port:8082}")
    private int port;
    @Getter
    private Channel channel;

    @PostConstruct
    void start() {
        try {
            EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ch.pipeline().addLast(
                                    new UdpDecoder(false),
                                    new ClientPacketHandler(),
                                    new UdpEncoder()
                            );
                        }
                    });

            channel = b.bind(0).sync().channel();
            log.info("udp client started at socket://localhost:{}/", ((InetSocketAddress) channel.localAddress()).getPort());
            channel.closeFuture().addListener(future -> group.shutdownGracefully());
            recipient = new InetSocketAddress(host, port);
            sender = (InetSocketAddress) channel.localAddress();
        } catch (Exception e) {
            log.error(e.toString(), e);
            System.exit(-1);
        }
    }
}
