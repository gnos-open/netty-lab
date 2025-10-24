package org.gnos.netty.lab.common.session;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.enums.Protocol;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 会话管理器
 */
@Slf4j
@Component
public class SessionManager {

    Map<String, ClientSession> idSessionMap = Maps.newConcurrentMap();
    Map<Channel, ClientSession> channelSessionMap = Maps.newConcurrentMap();

    public ClientSession getSession(Channel channel) {
        return channelSessionMap.getOrDefault(channel, null);
    }

    public ClientSession getSessionOrClose(Channel channel) {
        ClientSession session = getSession(channel);
        if (session == null) {
            channel.close();
        }
        return session;
    }

    public void onConnected(Channel channel, Protocol protocol) {
        log.info("onConnected:{}", channel);
        channelSessionMap.put(channel, new ClientSession().setProtocol(protocol).setChannel(channel));
    }

    public void onLogin(String clientId, Channel channel) {
        log.info("onLogin:{}, {}", clientId, channel);
        ClientSession session = getSessionOrClose(channel);
        if (session != null) {
            session.setClientId(clientId);
            idSessionMap.put(clientId, session);
        }
    }

    public void onReconnect(Channel channel) {
        //待扩展
    }

    public void onLogout(Channel channel) {
        onClosed(channel);
    }

    public void onClosed(Channel channel) {
        log.info("onClosed:{}", channel);
        ClientSession session = channelSessionMap.getOrDefault(channel, null);
        if (session != null) {
            String clientId = session.getClientId();
            if (clientId != null) {
                idSessionMap.remove(clientId);
                channelSessionMap.remove(channel);
            }
        }
    }

}
