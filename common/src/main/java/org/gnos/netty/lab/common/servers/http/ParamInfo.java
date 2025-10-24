package org.gnos.netty.lab.common.servers.http;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.Annotation;

@Data
@AllArgsConstructor
public class ParamInfo {

    /*参数名称*/
    private String name;
    /*参数类型*/
    private Class<?> type;
    /*指定解析注解类型*/
    private Class<? extends Annotation> annotation;

}
