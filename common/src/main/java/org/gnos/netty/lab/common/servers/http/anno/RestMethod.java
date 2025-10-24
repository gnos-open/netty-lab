package org.gnos.netty.lab.common.servers.http.anno;

import org.gnos.netty.lab.common.enums.RestMethodType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rest请求处理方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestMethod {

    String value() default "";

    RestMethodType method();

}
