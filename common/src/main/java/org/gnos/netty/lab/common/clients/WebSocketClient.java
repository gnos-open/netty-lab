package org.gnos.netty.lab.common.clients;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.clients.handler.ClientPacketHandler;
import org.gnos.netty.lab.common.codec.WebSocketDecoder;
import org.gnos.netty.lab.common.codec.WebSocketEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * WebSocket客户端
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "netty.client.websocket.enabled", havingValue = "true")
public class WebSocketClient implements Client {

    static final int MAX_CONTENT_LENGTH = 8192;

    @Value("${netty.client.websocket.host:127.0.0.1}")
    private String host;
    @Value("${netty.client.websocket.port:8083}")
    private int port;

    @Getter
    private Channel channel;

    @PostConstruct
    void start() {
        try {
            EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            URI uri = new URI("ws://" + host + ":" + port);
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(MAX_CONTENT_LENGTH),
                                    new WebSocketClientCompressionHandler(MAX_CONTENT_LENGTH),
                                    //握手
                                    new WebSocketClientProtocolHandler(
                                            WebSocketClientHandshakerFactory.newHandshaker(
                                                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders())),

                                    new WebSocketDecoder(false),
                                    new ClientPacketHandler(),
                                    new WebSocketEncoder()
                            );
                        }
                    });
            b.connect(uri.getHost(), port).addListener(cf -> {
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
