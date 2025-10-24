package org.gnos.netty.lab.common.servers;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.codec.UdpDecoder;
import org.gnos.netty.lab.common.codec.UdpEncoder;
import org.gnos.netty.lab.common.servers.handler.PacketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * UDP服务端
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "netty.server.udp.enabled", havingValue = "true")
public class UdpServer {

    @Value("${netty.server.udp.port}")
    int PORT;

    @Autowired
    PacketHandler packetHandler;

    @PostConstruct
    void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        try {
            Bootstrap b = new Bootstrap()
                    .group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));

                            pipeline.addLast(new UdpDecoder(true));
                            pipeline.addLast(packetHandler);
                            pipeline.addLast(new UdpEncoder());
                        }
                    });

            Channel ch = b.bind(PORT).sync().channel();
            log.info("udp server started at socket://localhost:{}/", PORT);
            //ch.closeFuture().sync();//卡主线程，换成异步
            ch.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error(e.toString(), e);
            System.exit(-1);
        }
    }
}
