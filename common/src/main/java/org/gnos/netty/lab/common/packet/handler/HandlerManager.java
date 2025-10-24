package org.gnos.netty.lab.common.packet.handler;

import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.packet.Packet;
import org.gnos.netty.lab.common.packet.TcpPacket;
import org.gnos.netty.lab.common.packet.UdpPacket;
import org.gnos.netty.lab.common.packet.WebSocketPacket;
import org.gnos.netty.lab.common.session.ClientSession;
import org.gnos.netty.lab.common.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 包处理管理器
 */
@Slf4j
@Component
public class HandlerManager {

    /*指定Message类型的Packet由Method处理*/
    static final Map<Class<? extends Message>, Method> methodMap = Maps.newConcurrentMap();
    /*指定Message类型的Packet由Handler处理*/
    static final Map<Class<? extends Message>, Handler> handlerMap = Maps.newConcurrentMap();

    @Autowired
    SessionManager sessionManager;

    Object invoke(Method method, Handler handler, Channel channel, Packet packet) {
        //sessison可能为空，udp无session
        ClientSession session = sessionManager.getSession(channel);
        try {
            //handlerd的method参数必须是(Session, Channel, Packet)
            return method.invoke(handler, new Object[]{session, channel, packet});
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }

    public void registerMethod(Class<? extends Message> clazz, Method method, Handler handler) {
        methodMap.put(clazz, method);
        handlerMap.put(clazz, handler);
    }

    public void handle(Channel channel, Packet packet) {
        Class<? extends Message> clazz = packet.getMessageClass();
        Handler handler = handlerMap.get(clazz);
        if (handler == null) {
            log.error("handler empty for clazz:{}", clazz.getSimpleName());
            return;
        }
        Method method = methodMap.get(clazz);
        if (method == null) {
            log.error("handle method empty for clazz:{}", clazz.getSimpleName());
            return;
        }
        try {
            CompletableFuture.supplyAsync(() -> invoke(method, handler, channel, packet))
                    .thenAccept(result -> {
                        if (result instanceof Message) {
                            switch (packet.getProtocol()) {
                                case TCP -> channel.writeAndFlush(new TcpPacket((Message) result));
                                case UDP ->
                                        channel.writeAndFlush(new UdpPacket((Message) result, packet.getSender(), packet.getRecipient()));
                                case WEBSOCKET -> channel.writeAndFlush(new WebSocketPacket((Message) result));
                            }
                        }
                    });
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }
}
