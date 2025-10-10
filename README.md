# netty-lab

<a href="https://github.com/gnos-open/netty-lab/"><img src="https://img.shields.io/github/stars/gnos-open/netty-lab" ></a>
<a href="https://unlicense.org"><img src="https://img.shields.io/badge/license-unlicense-blue.svg" ></a>

## 项目简介

用netty实现一个简单的tcp/udp/websocket服务器逻辑，传输数据基于protobuf封装。

## 使用技术

- Java17+
- netty
- protobuf
- spring-boot
- hutool
- lombok

## 项目结构

``` 
netty-lab
  ├── client           -- 客户端启动模块
  ├── common           -- 公共模块
  │     ├── clients    -- 客户端封装
  │     ├── codec      -- 编码器/解码器
  │     ├── component  -- 其它组件
  │     ├── enums      -- 枚举定义
  │     ├── packet     -- 数据包封装
  │     ├── servers    -- 服务端封装
  │     ├── session    -- 会话管理
  │     └── utils      -- 工具类
  ├── protocols        -- 数据协议定义
  └── server           -- 服务器启动模块
```

## 设计约定

### 数据协议命名与消息封装

为了方便客户端、服务端数据包解析，Protobuf定义message时，message名字中带上opcode编号，客户端、服务端在收到消息时根据opcode把消息转换成对应的message结构。代码实现示例如下：

```
数据协议命名，设计上支持同一组消息（请求与响应）使用同一个编号，具体扩展时看请求与响应编号可以不一致。

message TestReq_101 {
  ......
}

message TestResp_101 {
  ......
}
```

消息封装示例如下：

```
编码器示例，设计上opcode使用short来存储，消息定义时注意消息opcode编号不要越界，当然也可以修改为int来存储

protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
    Message message = packet.getMessage();
    short opcode = ProtobufUtil.getOpCode(message);
    byte[] bytes = message.toByteArray();
    int length = bytes.length;
    out.writeShort(opcode);//2
    out.writeInt(length);//4
    out.writeBytes(bytes);
}
```

```
解码器示例，与编码器对应

protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {// throws Exception {
    if (in.readableBytes() < 2 + 4) {
        //wait
        return;
    }
    in.markReaderIndex();
    short opcode = in.readShort();//2
    int length = in.readInt();//4
    if (in.readableBytes() < length) {
        in.resetReaderIndex();
        //wait
        return;
    }
    byte[] bytes = new byte[length];
    in.readBytes(bytes);

    //transform to packet
    Class<?> clazz = serverSide ? ProtobufUtil.getReqClass(opcode) : ProtobufUtil.getRespClass(opcode);
    Message message = ProtobufUtil.fromBytes(clazz, bytes);
    out.add(new TcpPacket(message));
}
```

### 登录与认证

本项目只是一个数据包传输与处理模块，应作为完整项目中的一个模块使用。在消息处理时可能会用到当前登录用户信息，需要添加登录逻辑。为了保证TCP服务和WebSocket服务逻辑一致性，推荐以下流程来实现：

1. 从登录服务登录成功获取可以认证用户的token。
2. 添加一个登录Message协议，比如LoginReq_102，附带token进行登录。

另，因为UDP协议无连接，没有对应的Session实现。UDP数据包处理逻辑如果需要用户信息，对应的Message协议中添加用户信息即可。

### SSL/TLS配置

关于生产环境SSL/TLS配置，倾向于交给nginx网关去配置处理。

## 快速开始

1. 获取代码（用你喜欢的方式）
2. 本地编译（用你喜欢的ide，我自己用IDEA社区版）
3. 启动服务端

```
用你喜欢的方式运行服务端入口类：
org.gnos.netty.lab.server.ServerApplication
```

4. 启动客户端

```
用你喜欢的方式运行客户端入口类：
org.gnos.netty.lab.client.ClientApplication
```

5. 输入测试命令

```
在客户端运行终端输入对应命令，可以发送相关测试数据包：
tcp 发送tcp数据包到服务端
udp 发送udp数据包到服务端
websocket 发送websocket数据包到服务端
在服务端/客户端终端查看相关日志打印
```

## 许可

搞不明白各种许可，我本意是想做无任何限制的开源项目，[Unlicense](LICENSE)应该是这个意思。