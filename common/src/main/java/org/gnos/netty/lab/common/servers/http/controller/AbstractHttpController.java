package org.gnos.netty.lab.common.servers.http.controller;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.enums.RestMethodType;
import org.gnos.netty.lab.common.servers.http.anno.RestController;
import org.gnos.netty.lab.common.servers.http.anno.RestMethod;
import org.gnos.netty.lab.common.servers.http.manager.RestHandlerManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

@Slf4j
public abstract class AbstractHttpController implements HttpController {

    @Autowired
    RestHandlerManager restHandlerManager;

    @PostConstruct
    void register() {
        String handlerPath = null;
        RestController restController = getClass().getAnnotation(RestController.class);
        if (restController != null) {
            handlerPath = restController.value();
        }
        //注册当前包处理器处理哪些包，同一个Message Class对应的Packet只能在一个handler中处理，重复定义会覆盖
        Method[] methods = getClass().getDeclaredMethods();
        for (Method method : methods) {
            RestMethod restMethod = method.getAnnotation(RestMethod.class);
            if (restMethod != null) {
                restHandlerManager.registerHandlerMethod(restMethod.method(), buildPath(handlerPath, restMethod.value(), method), method, this);
                if (restMethod.method().equals(RestMethodType.GET)) {
                    restHandlerManager.registerHandlerMethod(RestMethodType.HEAD, buildPath(handlerPath, restMethod.value(), method), method, this);
                }
            }
        }
    }

    String buildPath(String handlerPath, String methodPath, Method method) {
        String path = StrUtil.join("",
                StrUtil.isBlank(handlerPath) ? "" : handlerPath,
                StrUtil.isBlank(methodPath) ? "" : methodPath);
        if (StrUtil.isBlank(path)) {
            log.error("handler path empty : {}, {}", getClass().getSimpleName(), method.getName());
            System.exit(-1);
        }
        return path;
    }

}
