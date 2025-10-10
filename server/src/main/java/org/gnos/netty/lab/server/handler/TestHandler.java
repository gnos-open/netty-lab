package org.gnos.netty.lab.server.handler;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.packet.Packet;
import org.gnos.netty.lab.common.packet.handler.AbstractHandler;
import org.gnos.netty.lab.common.packet.handler.HandleMethod;
import org.gnos.netty.lab.common.session.ClientSession;
import org.gnos.netty.lab.proto.client.message.TestReq_101;
import org.gnos.netty.lab.proto.client.message.TestResp_101;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestHandler extends AbstractHandler {

    @HandleMethod(onClass = TestReq_101.class)
    public TestResp_101 testReq(ClientSession session, Channel channel, Packet packet) {
        log.info("testReq:session:{},channel:{},packet:{}", session, channel, packet.toLogString());
        return TestResp_101.newBuilder().setResult("消息已收到").setMessage("你好啊").build();
    }

}
