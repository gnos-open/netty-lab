package org.gnos.netty.lab.common.servers;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.codec.TcpDecoder;
import org.gnos.netty.lab.common.codec.TcpEncoder;
import org.gnos.netty.lab.common.servers.handler.PacketHandler;
import org.gnos.netty.lab.common.servers.handler.TcpSessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * TCP服务端
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "netty.server.tcp.enabled", havingValue = "true")
public class TcpServer {

    @Value("${netty.server.tcp.port}")
    int PORT;

    @Autowired
    TcpSessionHandler sessionHandler;
    @Autowired
    PacketHandler packetHandler;

    @PostConstruct
    void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));

                            pipeline.addLast(sessionHandler);
                            pipeline.addLast(new TcpDecoder(true));
                            pipeline.addLast(packetHandler);
                            pipeline.addLast(new TcpEncoder());
                        }
                    });

            Channel ch = b.bind(PORT).sync().channel();
            log.info("tcp server started at socket://localhost:{}/", PORT);
            //ch.closeFuture().sync();//卡主线程，换成异步
            ch.closeFuture().addListener(future -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error(e.toString(), e);
            System.exit(-1);
        }
    }
}
