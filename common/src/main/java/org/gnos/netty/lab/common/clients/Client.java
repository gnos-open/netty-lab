package org.gnos.netty.lab.common.clients;

import io.netty.channel.Channel;

/**
 * 客户端接口
 */
public interface Client {

    /*获取通道*/
    Channel getChannel();

}
