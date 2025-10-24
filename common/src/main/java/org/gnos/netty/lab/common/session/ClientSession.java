package org.gnos.netty.lab.common.session;

import io.netty.channel.Channel;
import lombok.Data;
import org.gnos.netty.lab.common.enums.Protocol;

/**
 * 客户端会话
 */
@Data
public class ClientSession implements ClientChannel {

    private Protocol protocol;
    /*客户端Id，可用作记录当前登录用户*/
    private String clientId;
    private Channel channel;

}
