package org.gnos.netty.lab.common.packet.handler;


import com.google.protobuf.Message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Packet处理方法注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleMethod {

    /*对应处理的Message类*/
    Class<? extends Message> onClass();

}
