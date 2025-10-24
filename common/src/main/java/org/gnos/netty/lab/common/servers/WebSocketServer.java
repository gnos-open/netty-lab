package org.gnos.netty.lab.common.servers;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.codec.WebSocketDecoder;
import org.gnos.netty.lab.common.codec.WebSocketEncoder;
import org.gnos.netty.lab.common.servers.handler.PacketHandler;
import org.gnos.netty.lab.common.servers.handler.WebSocketSessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * WebSocket服务端
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "netty.server.websocket.enabled", havingValue = "true")
public class WebSocketServer {

    @Value("${netty.server.websocket.port}")
    int PORT;

    @Autowired
    WebSocketSessionHandler sessionHandler;
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
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));

                            pipeline.addLast(sessionHandler);
                            pipeline.addLast(new WebSocketDecoder(true));
                            pipeline.addLast(packetHandler);
                            pipeline.addLast(new WebSocketEncoder());
                        }
                    });

            Channel ch = b.bind(PORT).sync().channel();
            log.info("websocket server started at ws://localhost:{}/", PORT);
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
