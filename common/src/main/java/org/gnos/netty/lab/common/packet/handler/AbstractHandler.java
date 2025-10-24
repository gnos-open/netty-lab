package org.gnos.netty.lab.common.packet.handler;

import com.google.protobuf.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * 抽象的服务端、客户端包处理器
 */
public abstract class AbstractHandler implements Handler {

    @Autowired
    HandlerManager handlerManager;

    @PostConstruct
    void register() {
        //注册当前包处理器处理哪些包，同一个Message Class对应的Packet只能在一个handler中处理，重复定义会覆盖
        Method[] methods = getClass().getDeclaredMethods();
        for (Method method : methods) {
            HandleMethod annotation = method.getAnnotation(HandleMethod.class);
            if (annotation != null) {
                Class<? extends Message> clazz = annotation.onClass();
                handlerManager.registerMethod(clazz, method, this);
            }
        }
    }
}
