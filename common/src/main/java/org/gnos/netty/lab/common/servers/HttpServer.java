package org.gnos.netty.lab.common.servers;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.servers.handler.HttpServerHandler;
import org.gnos.netty.lab.common.servers.http.manager.RestHandlerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(value = "netty.server.http.enabled", havingValue = "true")
public class HttpServer {

    @Value("${netty.server.http.port}")
    int PORT;

    @Autowired
    RestHandlerManager restHandlerManager;

    private final CorsConfig corsConfig = CorsConfig
            .withAnyOrigin()
            .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS)
            .allowedRequestHeaders("Content-Type")//非简单头、自定义头需要允许
            .allowCredentials()
            .maxAge(3600)
            .build();

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
                            // 添加日志处理器
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                            // HTTP编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // HTTP消息聚合器
                            pipeline.addLast(new HttpObjectAggregator(65536));

                            //CORS
                            pipeline.addLast(new CorsHandler(corsConfig));

                            // 自定义HTTP请求处理器
                            pipeline.addLast(new HttpServerHandler(restHandlerManager));
                        }
                    });

            Channel ch = b.bind(PORT).sync().channel();
            log.info("http server started at http://localhost:{}/", PORT);
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
