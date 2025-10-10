package org.gnos.netty.lab.server.handler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.packet.Packet;
import org.gnos.netty.lab.common.packet.handler.AbstractHandler;
import org.gnos.netty.lab.common.packet.handler.HandleMethod;
import org.gnos.netty.lab.common.session.ClientSession;
import org.gnos.netty.lab.common.session.SessionManager;
import org.gnos.netty.lab.proto.client.message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestHandler extends AbstractHandler {

    @Autowired
    SessionManager sessionManager;

    @HandleMethod(onClass = TestReq_101.class)
    public TestResp_101 testReq(ClientSession session, Channel channel, Packet packet) {
        log.info("testReq:session:{},channel:{},packet:{}", session, channel, packet.toLogString());
        return TestResp_101.newBuilder().setResult("消息已收到").setMessage("你好啊").build();
    }

    @HandleMethod(onClass = LoginReq_102.class)
    public LoginResp_102 loginReq(ClientSession session, Channel channel, Packet packet) {
        String openId = IdUtil.fastSimpleUUID();
        //设置session
        sessionManager.onLogin(openId, channel);
        log.info("loginSucc:{}", openId);
        return LoginResp_102.newBuilder().setOpenId(openId).build();
    }

    @HandleMethod(onClass = ChatReq_103.class)
    public ChatResp_103 chat(ClientSession session, Channel channel, Packet packet) {
        String error = null;
        String message = ((ChatReq_103) packet.getMessage()).getMessage();
        if (session == null) {
            error = "找不到用户会话！";
        } else {
            if (StrUtil.isBlank(session.getClientId())) {
                error = "用户未登录！";
            }
        }
        if (StrUtil.isNotBlank(error)) {
            return ChatResp_103.newBuilder().setError(error).build();
        } else {
            return ChatResp_103.newBuilder().setMessage(message).build();
        }
    }

}
