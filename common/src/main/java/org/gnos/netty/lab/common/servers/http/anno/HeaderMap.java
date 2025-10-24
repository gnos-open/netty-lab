package org.gnos.netty.lab.common.servers.http.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Header类型，约定参数：Map<String,String> header
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HeaderMap {

    String value() default "";

}
